package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class CustomerAccountState {

    interface ChangeListener {
        void accountStateChanged();
    }

    static final class CustomerInvoice {
        final String invoiceId;
        final String customerName;
        final String vehicleName;
        final int rentalDays;
        final double baseAmount;
        double lateFee;
        double lateFeeCharged;
        double tax;
        String status;
        String paymentMethod;
        final String startDate;
        final String endDate;

        CustomerInvoice(String invoiceId, String customerName, String vehicleName, int rentalDays,
                        double baseAmount, double lateFee, String status, String paymentMethod,
                        String startDate, String endDate) {
            this.invoiceId = invoiceId;
            this.customerName = customerName;
            this.vehicleName = vehicleName;
            this.rentalDays = rentalDays;
            this.baseAmount = baseAmount;
            this.lateFee = Math.max(0.0, lateFee);
            this.lateFeeCharged = "Paid".equalsIgnoreCase(status) ? this.lateFee : 0.0;
            this.tax = (baseAmount + this.lateFee) * 0.10;
            this.status = status;
            this.paymentMethod = paymentMethod;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        double taxAmount() {
            return tax;
        }

        double totalAmount() {
            return baseAmount + lateFee + tax;
        }
    }

    private static final Map<String, CustomerAccountState> STATES = new HashMap<>();
    private static final int STARTING_POINTS = 850;
    private static final int STARTING_REWARDS_USED = 12;
    private static final double STARTING_WALLET_BALANCE = 0.0;
    private static final Path STORE_DIR = Path.of(System.getProperty("user.dir"), "data", "customer-accounts");
    private static boolean persistedStatesLoaded;

    private final List<CustomerInvoice> invoices = new ArrayList<>();
    private final List<ChangeListener> listeners = new ArrayList<>();
    private final String storageKey;
    private int redeemedPoints;
    private int redeemedRewards;
    private double walletBalance = STARTING_WALLET_BALANCE;

 private CustomerAccountState(Customer customer, String key) {
    storageKey = key;

    if (!loadSavedState()) {
        invoices.clear();
        redeemedPoints = 0;
        redeemedRewards = 0;
        walletBalance = STARTING_WALLET_BALANCE;
        saveState();
    }
}

    private CustomerAccountState(String key) {
        storageKey = key;
        loadSavedState();
    }

    static CustomerAccountState forCustomer(Customer customer) {
        String key = customerKey(customer);
        return STATES.computeIfAbsent(key, ignored -> new CustomerAccountState(customer, key));
    }

    static BillingMetricsBus.Metrics billingMetricsSnapshot() {
        loadPersistedCustomerStates();

        int invoiceCount = 0;
        long paidInvoices = 0;
        long pendingPayments = 0;
        long overdueInvoices = 0;
        double paidRevenue = 0;
        double outstandingBalance = 0;
        double lateFees = 0;
        double grossTotal = 0;
        double cardRevenue = 0;
        double cashRevenue = 0;
        double bankTransferRevenue = 0;
        double otherRevenue = 0;

        for (CustomerAccountState state : STATES.values()) {
            for (CustomerInvoice invoice : state.invoices) {
                invoiceCount++;
                double total = invoice.totalAmount();
                grossTotal += total;
                lateFees += invoice.lateFee;
                if ("Paid".equals(invoice.status)) {
                    paidInvoices++;
                    paidRevenue += total;
                    if ("Card".equals(invoice.paymentMethod)) {
                        cardRevenue += total;
                    } else if ("Cash".equals(invoice.paymentMethod)) {
                        cashRevenue += total;
                    } else if ("Bank Transfer".equals(invoice.paymentMethod)) {
                        bankTransferRevenue += total;
                    } else {
                        otherRevenue += total;
                    }
                } else {
                    pendingPayments++;
                    outstandingBalance += total;
                    if ("Overdue".equals(invoice.status)) {
                        overdueInvoices++;
                    }
                }
            }
        }

        return new BillingMetricsBus.Metrics(
                invoiceCount,
                paidInvoices,
                pendingPayments,
                overdueInvoices,
                paidRevenue,
                outstandingBalance,
                lateFees,
                grossTotal,
                cardRevenue,
                cashRevenue,
                bankTransferRevenue,
                otherRevenue
        );
    }

    void addChangeListener(ChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    List<CustomerInvoice> getInvoices() {
        return invoices;
    }

    LateFeeChargeResult applyLateFee(String invoiceId, double calculatedLateFee) {
        if (invoiceId == null || invoiceId.isBlank()) {
            return new LateFeeChargeResult(0.0, 0.0, Math.max(0.0, calculatedLateFee));
        }

        CustomerInvoice invoice = invoices.stream()
                .filter(item -> item.invoiceId.equalsIgnoreCase(invoiceId))
                .findFirst()
                .orElse(null);

        if (invoice == null) {
            return new LateFeeChargeResult(0.0, 0.0, Math.max(0.0, calculatedLateFee));
        }

        double previousLateFee = invoice.lateFee;
        double previousLateFeeCharged = invoice.lateFeeCharged;
        double previousWalletBalance = walletBalance;
        String previousStatus = invoice.status;

        double targetLateFee = Math.max(invoice.lateFee, Math.max(0.0, calculatedLateFee));
        invoice.lateFee = targetLateFee;
        invoice.tax = (invoice.baseAmount + invoice.lateFee) * 0.10;

        double outstanding = Math.max(0.0, invoice.lateFee - invoice.lateFeeCharged);
        double chargedNow = Math.min(walletBalance, outstanding);

        if (chargedNow > 0.001) {
            walletBalance -= chargedNow;
            invoice.lateFeeCharged += chargedNow;
        }

        double remaining = Math.max(0.0, invoice.lateFee - invoice.lateFeeCharged);

        if (remaining > 0.001) {
            invoice.status = "Overdue";
        } else {
            invoice.status = "Paid";
        }

        boolean changed = Math.abs(previousLateFee - invoice.lateFee) > 0.001
                || Math.abs(previousLateFeeCharged - invoice.lateFeeCharged) > 0.001
                || Math.abs(previousWalletBalance - walletBalance) > 0.001
                || !java.util.Objects.equals(previousStatus, invoice.status);

        if (changed) {
            saveState();
            notifyChanged();
        }

        return new LateFeeChargeResult(
                chargedNow,
                remaining,
                invoice.lateFee
        );
    }

    boolean isInvoiceFullyPaid(String invoiceId) {
        if (invoiceId == null || invoiceId.isBlank()) {
            return true;
        }

        return invoices.stream()
                .filter(item -> item.invoiceId.equalsIgnoreCase(invoiceId))
                .findFirst()
                .map(invoice -> invoice.lateFeeCharged + 0.001 >= invoice.lateFee)
                .orElse(true);
    }

    record LateFeeChargeResult(
            double chargedNow,
            double remainingAmount,
            double totalLateFee
    ) {
    }

    void addInvoice(CustomerInvoice invoice) {
        if (invoice != null) {
            invoices.add(invoice);
            saveState();
            notifyChanged();
        }
    }
    
    boolean adjustWalletBalance(double amount) {
    double newBalance = walletBalance + amount;

    if (newBalance < 0) {
        return false;
    }

    walletBalance = newBalance;
    saveState();
    notifyChanged();
    return true;
}

    boolean recordPayment(CustomerInvoice invoice, String paymentMethod) {
        if (invoice == null || "Paid".equals(invoice.status)) {
            return false;
        }
        double amount = invoice.totalAmount();
        if (!canAfford(amount)) {
            return false;
        }
        walletBalance -= amount;
        invoice.status = "Paid";
        invoice.paymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "Card" : paymentMethod;
        saveState();
        notifyChanged();
        return true;
    }

    boolean addPaidInvoice(CustomerInvoice invoice, String paymentMethod) {
        if (invoice == null) {
            return false;
        }
        double amount = invoice.totalAmount();
        if (!canAfford(amount)) {
            return false;
        }
        walletBalance -= amount;
        invoice.status = "Paid";
        invoice.paymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "Card" : paymentMethod;
        invoices.add(invoice);
        saveState();
        notifyChanged();
        return true;
    }

    double getWalletBalance() {
        return walletBalance;
    }

    boolean canAfford(double amount) {
        return amount >= 0 && walletBalance + 0.001 >= amount;
    }

    void addWalletFunds(double amount) {
        if (amount <= 0) {
            return;
        }
        walletBalance += amount;
        saveState();
        notifyChanged();
    }

    boolean redeemReward(int cost) {
        if (cost <= 0 || getLoyaltyPoints() < cost) {
            return false;
        }
        redeemedPoints += cost;
        redeemedRewards++;
        saveState();
        notifyChanged();
        return true;
    }

    int getTotalRentals() {
        return invoices.size();
    }

    int getActiveRentals() {
        return (int) invoices.stream().filter(i -> !"Paid".equals(i.status)).count();
    }

    int getCompletedRentals() {
        return (int) invoices.stream().filter(i -> "Paid".equals(i.status)).count();
    }

    int getLateReturns() {
        return (int) invoices.stream().filter(i -> "Overdue".equals(i.status)).count();
    }

    long getPaidInvoices() {
        return invoices.stream().filter(i -> "Paid".equals(i.status)).count();
    }

    long getPendingPayments() {
        return invoices.stream().filter(i -> !"Paid".equals(i.status)).count();
    }

    double getTotalSpent() {
        return invoices.stream()
                .filter(i -> "Paid".equals(i.status))
                .mapToDouble(CustomerInvoice::totalAmount)
                .sum();
    }

    double getOutstandingBalance() {
        return invoices.stream()
                .filter(i -> !"Paid".equals(i.status))
                .mapToDouble(CustomerInvoice::totalAmount)
                .sum();
    }

    double getLateFees() {
        return invoices.stream().mapToDouble(i -> i.lateFee).sum();
    }

    int getLoyaltyPoints() {
        return Math.max(0, STARTING_POINTS + (int) Math.floor(getTotalSpent() / 20.0) - redeemedPoints);
    }

    int getRewardsUsed() {
        return STARTING_REWARDS_USED + redeemedRewards;
    }

    String getTierName() {
        int points = getLoyaltyPoints();
        if (points < 500) {
            return "Bronze";
        }
        if (points < 1500) {
            return "Silver";
        }
        if (points < 3000) {
            return "Gold";
        }
        return "Platinum";
    }

    int getPointsToNextTier() {
        int points = getLoyaltyPoints();
        if (points < 500) {
            return 500 - points;
        }
        if (points < 1500) {
            return 1500 - points;
        }
        if (points < 3000) {
            return 3000 - points;
        }
        return 0;
    }

    String getNextTierName() {
        int points = getLoyaltyPoints();
        if (points < 500) {
            return "Silver";
        }
        if (points < 1500) {
            return "Gold";
        }
        if (points < 3000) {
            return "Platinum";
        }
        return "Platinum";
    }

    private void notifyChanged() {
        List<ChangeListener> snapshot = new ArrayList<>(listeners);
        SwingUtilities.invokeLater(() -> snapshot.forEach(ChangeListener::accountStateChanged));
        BillingMetricsBus.fireChanged();
    }

    

    private boolean loadSavedState() {
        Path file = storagePath(storageKey);
        if (!Files.exists(file)) {
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            invoices.clear();
            redeemedPoints = 0;
            redeemedRewards = 0;
            walletBalance = STARTING_WALLET_BALANCE;

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\t", -1);
                if (parts.length >= 4 && "STATE".equals(parts[0])) {
                    redeemedPoints = parseInt(parts[2], 0);
                    redeemedRewards = parseInt(parts[3], 0);
                    if (parts.length >= 5) {
                        walletBalance = parseDouble(parts[4], STARTING_WALLET_BALANCE);
                    }
                } else if (parts.length >= 11 && "INVOICE".equals(parts[0])) {
                    CustomerInvoice invoice = new CustomerInvoice(
                            decode(parts[1]),
                            decode(parts[2]),
                            decode(parts[3]),
                            parseInt(parts[4], 1),
                            parseDouble(parts[5], 0),
                            parseDouble(parts[6], 0),
                            decode(parts[7]),
                            decode(parts[8]),
                            decode(parts[9]),
                            decode(parts[10])
                    );

                    if (parts.length >= 12) {
                        invoice.lateFeeCharged = parseDouble(parts[11], 0);
                    } else if ("Paid".equalsIgnoreCase(invoice.status)) {
                        invoice.lateFeeCharged = invoice.lateFee;
                    }

                    invoices.add(invoice);
                }
            }
            return !invoices.isEmpty() || !lines.isEmpty();
        } catch (IOException | IllegalArgumentException ex) {
            invoices.clear();
            redeemedPoints = 0;
            redeemedRewards = 0;
            walletBalance = STARTING_WALLET_BALANCE;
            return false;
        }
    }

    private void saveState() {
        try {
            Files.createDirectories(STORE_DIR);
            List<String> lines = new ArrayList<>();
            lines.add(String.join(
                    "\t",
                    "STATE",
                    encode(storageKey),
                    String.valueOf(redeemedPoints),
                    String.valueOf(redeemedRewards),
                    String.valueOf(walletBalance)
            ));
            for (CustomerInvoice invoice : invoices) {
                lines.add(String.join(
                        "\t",
                        "INVOICE",
                        encode(invoice.invoiceId),
                        encode(invoice.customerName),
                        encode(invoice.vehicleName),
                        String.valueOf(invoice.rentalDays),
                        String.valueOf(invoice.baseAmount),
                        String.valueOf(invoice.lateFee),
                        encode(invoice.status),
                        encode(invoice.paymentMethod),
                        encode(invoice.startDate),
                        encode(invoice.endDate),
                        String.valueOf(invoice.lateFeeCharged)
                ));
            }
            Files.write(storagePath(storageKey), lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // UI actions should continue even if the local persistence file is unavailable.
        }
    }

    private static void loadPersistedCustomerStates() {
        if (persistedStatesLoaded) {
            return;
        }
        persistedStatesLoaded = true;
        if (!Files.isDirectory(STORE_DIR)) {
            return;
        }
        try {
            try (var files = Files.list(STORE_DIR)) {
                files.filter(path -> path.getFileName().toString().endsWith(".tsv"))
                        .forEach(CustomerAccountState::loadPersistedCustomerState);
            }
        } catch (IOException ignored) {
            // The dashboard can still use already-open states.
        }
    }

    private static void loadPersistedCustomerState(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return;
            }
            String[] header = lines.get(0).split("\t", -1);
            if (header.length < 2 || !"STATE".equals(header[0])) {
                return;
            }
            String key = decode(header[1]);
            if (!key.isBlank() && !STATES.containsKey(key)) {
                STATES.put(key, new CustomerAccountState(key));
            }
        } catch (IOException | IllegalArgumentException ignored) {
            // Ignore malformed saved files instead of blocking the UI.
        }
    }

    static String resolveCustomerName(Customer currentCustomer) {
        if (currentCustomer == null) {
            return "Current Customer";
        }
        if (currentCustomer.getFullName() != null && !currentCustomer.getFullName().isBlank()) {
            return currentCustomer.getFullName().trim();
        }
        if (currentCustomer.getEmail() != null && !currentCustomer.getEmail().isBlank()) {
            return currentCustomer.getEmail().trim();
        }
        return "Current Customer";
    }

    private static String customerKey(Customer customer) {
        if (customer == null) {
            return "guest";
        }
        String email = customer.getEmail();
        if (email != null && !email.isBlank()) {
            return email.trim().toLowerCase(Locale.ROOT);
        }
        String name = customer.getFullName();
        if (name != null && !name.isBlank()) {
            return name.trim().toLowerCase(Locale.ROOT);
        }
        return "guest";
    }

    private static Path storagePath(String key) {
        return STORE_DIR.resolve(safeFileName(key) + ".tsv");
    }

    private static String safeFileName(String key) {
        String safe = key == null ? "guest" : key.trim().toLowerCase(Locale.ROOT);
        safe = safe.replaceAll("[^a-z0-9._-]", "_");
        return safe.isBlank() ? "guest" : safe;
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value == null ? "" : value), StandardCharsets.UTF_8);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    static CustomerInvoice createInvoice(Customer customer, int existingInvoiceCount, String vehicle,
                                         int days, double base, double late, String status,
                                         String paymentMethod) {
        LocalDateTime start = LocalDateTime.now();
        return createInvoice(
                customer,
                existingInvoiceCount,
                vehicle,
                days,
                base,
                late,
                status,
                paymentMethod,
                start,
                start.plusDays(days)
        );
    }

    static CustomerInvoice createInvoice(
            Customer customer,
            int existingInvoiceCount,
            String vehicle,
            int days,
            double base,
            double late,
            String status,
            String paymentMethod,
            LocalDateTime start,
            LocalDateTime end
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.ENGLISH);

        LocalDateTime safeStart = start == null ? LocalDateTime.now() : start;
        LocalDateTime safeEnd = end == null ? safeStart.plusDays(days) : end;

        return new CustomerInvoice(
                "INV-" + String.format("%04d", 1001 + existingInvoiceCount),
                resolveCustomerName(customer),
                vehicle,
                days,
                base,
                late,
                status,
                paymentMethod,
                safeStart.format(formatter),
                safeEnd.format(formatter)
        );
    }
}
