package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;

import javax.swing.SwingUtilities;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
        final double lateFee;
        final double tax;
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
            this.lateFee = lateFee;
            this.tax = (baseAmount + lateFee) * 0.10;
            this.status = status;
            this.paymentMethod = paymentMethod;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        double totalAmount() {
            return baseAmount + lateFee + tax;
        }
    }

    private static final Map<String, CustomerAccountState> STATES = new HashMap<>();
    private static final int STARTING_POINTS = 850;
    private static final int STARTING_REWARDS_USED = 12;

    private final List<CustomerInvoice> invoices = new ArrayList<>();
    private final List<ChangeListener> listeners = new ArrayList<>();
    private int redeemedPoints;
    private int redeemedRewards;

    private CustomerAccountState(Customer customer) {
        loadDemoInvoices(customer);
    }

    static CustomerAccountState forCustomer(Customer customer) {
        String key = customerKey(customer);
        return STATES.computeIfAbsent(key, ignored -> new CustomerAccountState(customer));
    }

    void addChangeListener(ChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    List<CustomerInvoice> getInvoices() {
        return invoices;
    }

    void addInvoice(CustomerInvoice invoice) {
        if (invoice != null) {
            invoices.add(invoice);
            notifyChanged();
        }
    }

    boolean recordPayment(CustomerInvoice invoice, String paymentMethod) {
        if (invoice == null || "Paid".equals(invoice.status)) {
            return false;
        }
        invoice.status = "Paid";
        invoice.paymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "Card" : paymentMethod;
        notifyChanged();
        return true;
    }

    boolean redeemReward(int cost) {
        if (cost <= 0 || getLoyaltyPoints() < cost) {
            return false;
        }
        redeemedPoints += cost;
        redeemedRewards++;
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
    }

    private void loadDemoInvoices(Customer customer) {
        VehicleService vehicleService = new VehicleService();
        String currentCustomerName = resolveCustomerName(customer);
        String[] statuses = {"Paid", "Pending", "Paid", "Overdue", "Paid", "Pending"};
        String[] methods = {"Card", "Cash", "Bank Transfer"};
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        int invoiceCount = Math.min(12, vehicles.size());

        for (int i = 0; i < invoiceCount; i++) {
            Vehicle vehicle = vehicles.get(i);
            int days = 2 + (i % 7);
            double base = vehicle.getDailyPrice() * days;
            String status = statuses[i % statuses.length];
            double late = "Overdue".equals(status) ? 50 + (i % 4) * 35 : 0;
            int startDay = 1 + (i % 9);
            int endDay = startDay + days;

            invoices.add(new CustomerInvoice(
                    "INV-" + String.format("%04d", 1001 + i),
                    currentCustomerName,
                    FleetUiData.displayName(vehicle),
                    days,
                    base,
                    late,
                    status,
                    methods[i % methods.length],
                    String.format("%02d Jul 2026 10:00 AM", startDay),
                    String.format("%02d Jul 2026 10:00 AM", endDay)
            ));
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

    static CustomerInvoice createInvoice(Customer customer, int existingInvoiceCount, String vehicle,
                                         int days, double base, double late, String status,
                                         String paymentMethod) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.ENGLISH);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(days);
        return new CustomerInvoice(
                "INV-" + String.format("%04d", 1001 + existingInvoiceCount),
                resolveCustomerName(customer),
                vehicle,
                days,
                base,
                late,
                status,
                paymentMethod,
                start.format(formatter),
                end.format(formatter)
        );
    }
}
