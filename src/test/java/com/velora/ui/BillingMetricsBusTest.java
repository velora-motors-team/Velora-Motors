package com.velora.ui;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BillingMetricsBusTest {

    @BeforeEach
    public void setUp() throws Exception {
        clearListeners();
        BillingMetricsBus.setAdminMetrics(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        clearListeners();
        BillingMetricsBus.setAdminMetrics(null);
    }

    @SuppressWarnings("unchecked")
    private void clearListeners() throws Exception {
        Field field = BillingMetricsBus.class.getDeclaredField("LISTENERS");
        field.setAccessible(true);
        ((List<BillingMetricsBus.ChangeListener>) field.get(null)).clear();
    }

    private BillingMetricsBus.Metrics getAdminMetrics() throws Exception {
        Field field = BillingMetricsBus.class.getDeclaredField("adminMetrics");
        field.setAccessible(true);
        return (BillingMetricsBus.Metrics) field.get(null);
    }

    @Test
    public void testEmptyMetrics() {
        BillingMetricsBus.Metrics metrics = BillingMetricsBus.Metrics.EMPTY;

        assertEquals(0, metrics.invoiceCount);
        assertEquals(0, metrics.paidInvoices);
        assertEquals(0, metrics.pendingPayments);
        assertEquals(0, metrics.overdueInvoices);
        assertEquals(0.0, metrics.paidRevenue, 0.0001);
        assertEquals(0.0, metrics.outstandingBalance, 0.0001);
        assertEquals(0.0, metrics.lateFees, 0.0001);
        assertEquals(0.0, metrics.grossTotal, 0.0001);
        assertEquals(0.0, metrics.cardRevenue, 0.0001);
        assertEquals(0.0, metrics.cashRevenue, 0.0001);
        assertEquals(0.0, metrics.bankTransferRevenue, 0.0001);
        assertEquals(0.0, metrics.otherRevenue, 0.0001);
    }

    @Test
    public void testMetricsPlus() {
        BillingMetricsBus.Metrics first = new BillingMetricsBus.Metrics(
                2, 1, 1, 0,
                100.0, 50.0, 10.0, 160.0,
                70.0, 20.0, 5.0, 5.0
        );

        BillingMetricsBus.Metrics second = new BillingMetricsBus.Metrics(
                3, 2, 1, 1,
                200.0, 80.0, 20.0, 300.0,
                120.0, 40.0, 20.0, 20.0
        );

        BillingMetricsBus.Metrics result = first.plus(second);

        assertEquals(5, result.invoiceCount);
        assertEquals(3, result.paidInvoices);
        assertEquals(2, result.pendingPayments);
        assertEquals(1, result.overdueInvoices);
        assertEquals(300.0, result.paidRevenue, 0.0001);
        assertEquals(130.0, result.outstandingBalance, 0.0001);
        assertEquals(30.0, result.lateFees, 0.0001);
        assertEquals(460.0, result.grossTotal, 0.0001);
        assertEquals(190.0, result.cardRevenue, 0.0001);
        assertEquals(60.0, result.cashRevenue, 0.0001);
        assertEquals(25.0, result.bankTransferRevenue, 0.0001);
        assertEquals(25.0, result.otherRevenue, 0.0001);
    }

    @Test
    public void testMetricsPlusNullReturnsSameObject() {
        BillingMetricsBus.Metrics metrics = new BillingMetricsBus.Metrics(
                1, 1, 0, 0,
                100.0, 0.0, 0.0, 100.0,
                100.0, 0.0, 0.0, 0.0
        );

        assertSame(metrics, metrics.plus(null));
    }

    @Test
    public void testSetAdminMetrics() throws Exception {
        BillingMetricsBus.Metrics metrics = new BillingMetricsBus.Metrics(
                1, 1, 0, 0,
                100.0, 0.0, 0.0, 100.0,
                100.0, 0.0, 0.0, 0.0
        );

        BillingMetricsBus.setAdminMetrics(metrics);

        assertSame(metrics, getAdminMetrics());
    }

    @Test
    public void testSetAdminMetricsNullUsesEmpty() throws Exception {
        BillingMetricsBus.setAdminMetrics(null);

        assertSame(BillingMetricsBus.Metrics.EMPTY, getAdminMetrics());
    }

    @Test
    public void testListenerCalledOnceAndDuplicateIgnored() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        BillingMetricsBus.ChangeListener listener = calls::incrementAndGet;

        BillingMetricsBus.addChangeListener(listener);
        BillingMetricsBus.addChangeListener(listener);
        BillingMetricsBus.addChangeListener(null);

        BillingMetricsBus.fireChanged();

        SwingUtilities.invokeAndWait(() -> {
        });

        assertEquals(1, calls.get());
    }

    @Test
    public void testSnapshotIsNotNull() {
        assertNotNull(BillingMetricsBus.snapshot());
    }
}