package com.velora.ui;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

final class BillingMetricsBus {

    interface ChangeListener {
        void metricsChanged();
    }

    static final class Metrics {
        static final Metrics EMPTY = new Metrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        final int invoiceCount;
        final long paidInvoices;
        final long pendingPayments;
        final long overdueInvoices;
        final double paidRevenue;
        final double outstandingBalance;
        final double lateFees;
        final double grossTotal;
        final double cardRevenue;
        final double cashRevenue;
        final double bankTransferRevenue;
        final double otherRevenue;

        Metrics(int invoiceCount, long paidInvoices, long pendingPayments, long overdueInvoices,
                double paidRevenue, double outstandingBalance, double lateFees, double grossTotal,
                double cardRevenue, double cashRevenue, double bankTransferRevenue, double otherRevenue) {
            this.invoiceCount = invoiceCount;
            this.paidInvoices = paidInvoices;
            this.pendingPayments = pendingPayments;
            this.overdueInvoices = overdueInvoices;
            this.paidRevenue = paidRevenue;
            this.outstandingBalance = outstandingBalance;
            this.lateFees = lateFees;
            this.grossTotal = grossTotal;
            this.cardRevenue = cardRevenue;
            this.cashRevenue = cashRevenue;
            this.bankTransferRevenue = bankTransferRevenue;
            this.otherRevenue = otherRevenue;
        }

        Metrics plus(Metrics other) {
            if (other == null) {
                return this;
            }
            return new Metrics(
                    invoiceCount + other.invoiceCount,
                    paidInvoices + other.paidInvoices,
                    pendingPayments + other.pendingPayments,
                    overdueInvoices + other.overdueInvoices,
                    paidRevenue + other.paidRevenue,
                    outstandingBalance + other.outstandingBalance,
                    lateFees + other.lateFees,
                    grossTotal + other.grossTotal,
                    cardRevenue + other.cardRevenue,
                    cashRevenue + other.cashRevenue,
                    bankTransferRevenue + other.bankTransferRevenue,
                    otherRevenue + other.otherRevenue
            );
        }
    }

    private static final List<ChangeListener> LISTENERS = new ArrayList<>();
    private static Metrics adminMetrics = Metrics.EMPTY;
    private static boolean adminMetricsLoaded;

    private BillingMetricsBus() {
    }

    static void addChangeListener(ChangeListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    static void setAdminMetrics(Metrics metrics) {
        adminMetrics = metrics == null ? Metrics.EMPTY : metrics;
        adminMetricsLoaded = true;
    }

    static Metrics snapshot() {
        loadAdminMetricsFromDisk();
        return adminMetrics.plus(CustomerAccountState.billingMetricsSnapshot());
    }

    static void fireChanged() {
        List<ChangeListener> snapshot = new ArrayList<>(LISTENERS);
        SwingUtilities.invokeLater(() -> snapshot.forEach(ChangeListener::metricsChanged));
    }

    private static void loadAdminMetricsFromDisk() {
        if (adminMetricsLoaded) {
            return;
        }
        adminMetricsLoaded = true;
        if (!Files.exists(BillingPanel.ADMIN_INVOICE_FILE)) {
            return;
        }

        try {
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

            for (String line : Files.readAllLines(BillingPanel.ADMIN_INVOICE_FILE, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\t", -1);
                if (parts.length < 11 || !"INVOICE".equals(parts[0])) {
                    continue;
                }

                double baseAmount = parseDouble(parts[5]);
                double lateFee = parseDouble(parts[6]);
                double tax = (baseAmount + lateFee) * 0.05;
                double total = baseAmount + lateFee + tax;
                String status = decode(parts[7]);
                String method = decode(parts[8]);

                invoiceCount++;
                grossTotal += total;
                lateFees += lateFee;

                if ("Paid".equals(status)) {
                    paidInvoices++;
                    paidRevenue += total;
                    if ("Card".equals(method)) {
                        cardRevenue += total;
                    } else if ("Cash".equals(method)) {
                        cashRevenue += total;
                    } else if ("Bank Transfer".equals(method)) {
                        bankTransferRevenue += total;
                    } else {
                        otherRevenue += total;
                    }
                } else {
                    pendingPayments++;
                    outstandingBalance += total;
                    if ("Overdue".equals(status)) {
                        overdueInvoices++;
                    }
                }
            }

            adminMetrics = new Metrics(
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
        } catch (IOException | IllegalArgumentException ignored) {
            adminMetrics = Metrics.EMPTY;
        }
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value == null ? "" : value), StandardCharsets.UTF_8);
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
