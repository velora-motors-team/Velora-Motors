package com.velora.ui;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
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

    private BillingMetricsBus() {
    }

    static void addChangeListener(ChangeListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    static void setAdminMetrics(Metrics metrics) {
        adminMetrics = metrics == null ? Metrics.EMPTY : metrics;
    }

    static Metrics snapshot() {
        return CustomerAccountState.billingMetricsSnapshot();
    }

    static void fireChanged() {
        List<ChangeListener> snapshot = new ArrayList<>(LISTENERS);
        SwingUtilities.invokeLater(() -> snapshot.forEach(ChangeListener::metricsChanged));
    }
}