package com.velora.ui;

import com.velora.authentication.Customer;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAccountStateTest {

    private static final String TEST_EMAIL =
            "customer-account-state-test@velora.com";

    private static final Path TEST_FILE = Path.of(
            System.getProperty("user.dir"),
            "data",
            "customer-accounts",
            "customer-account-state-test_velora.com.tsv"
    );

    @BeforeEach
    public void setUp() throws Exception {
        clearStaticState();
        Files.deleteIfExists(TEST_FILE);
    }

    @AfterEach
    public void tearDown() throws Exception {
        clearStaticState();
        Files.deleteIfExists(TEST_FILE);
    }

    @SuppressWarnings("unchecked")
    private void clearStaticState() throws Exception {
        Field statesField =
                CustomerAccountState.class.getDeclaredField("STATES");
        statesField.setAccessible(true);

        Map<String, CustomerAccountState> states =
                (Map<String, CustomerAccountState>) statesField.get(null);

        states.clear();

        Field loadedField =
                CustomerAccountState.class.getDeclaredField(
                        "persistedStatesLoaded"
                );
        loadedField.setAccessible(true);
        loadedField.setBoolean(null, false);
    }

    private Customer createCustomer() {
        return new Customer(
                "Hamada Ahmad",
                TEST_EMAIL,
                "0599999999"
        );
    }

    private CustomerAccountState createState() {
        return CustomerAccountState.forCustomer(createCustomer());
    }

    private CustomerAccountState.CustomerInvoice createInvoice(
            String id,
            double base,
            double lateFee,
            String status,
            String paymentMethod
    ) {
        return new CustomerAccountState.CustomerInvoice(
                id,
                "Hamada Ahmad",
                "BMW X5",
                2,
                base,
                lateFee,
                status,
                paymentMethod,
                "14 Jul 2026 10:00 AM",
                "16 Jul 2026 10:00 AM"
        );
    }

    @Test
    public void testCustomerInvoiceCalculations() {
        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        20.0,
                        "Paid",
                        "Card"
                );

        assertEquals(12.0, invoice.taxAmount(), 0.0001);
        assertEquals(132.0, invoice.totalAmount(), 0.0001);
        assertEquals(20.0, invoice.lateFeeCharged, 0.0001);
    }

    @Test
    public void testNegativeLateFeeBecomesZero() {
        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        -50.0,
                        "Pending",
                        "Cash"
                );

        assertEquals(0.0, invoice.lateFee, 0.0001);
        assertEquals(10.0, invoice.taxAmount(), 0.0001);
        assertEquals(110.0, invoice.totalAmount(), 0.0001);
    }

    @Test
    public void testForCustomerReturnsSameStateForSameCustomer() {
        Customer customer = createCustomer();

        CustomerAccountState first =
                CustomerAccountState.forCustomer(customer);

        CustomerAccountState second =
                CustomerAccountState.forCustomer(customer);

        assertSame(first, second);
    }

    @Test
    public void testResolveCustomerName() {
        assertEquals(
                "Current Customer",
                CustomerAccountState.resolveCustomerName(null)
        );

        Customer withName = new Customer(
                "  Hamada Ahmad  ",
                "hamada@email.com",
                ""
        );

        assertEquals(
                "Hamada Ahmad",
                CustomerAccountState.resolveCustomerName(withName)
        );

        Customer withEmailOnly = new Customer(
                "",
                "  EMAIL@TEST.COM  ",
                ""
        );

        assertEquals(
                "email@test.com",
                CustomerAccountState.resolveCustomerName(withEmailOnly)
        );

        Customer empty = new Customer("", "", "");

        assertEquals(
                "Current Customer",
                CustomerAccountState.resolveCustomerName(empty)
        );
    }

    @Test
    public void testAddWalletFundsAndCanAfford() {
        CustomerAccountState state = createState();

        assertEquals(0.0, state.getWalletBalance(), 0.0001);

        state.addWalletFunds(500.0);

        assertEquals(500.0, state.getWalletBalance(), 0.0001);
        assertTrue(state.canAfford(500.0));
        assertTrue(state.canAfford(499.99));
        assertFalse(state.canAfford(600.0));
        assertFalse(state.canAfford(-1.0));
    }

    @Test
    public void testAddWalletFundsIgnoresZeroAndNegative() {
        CustomerAccountState state = createState();

        state.addWalletFunds(0.0);
        state.addWalletFunds(-100.0);

        assertEquals(0.0, state.getWalletBalance(), 0.0001);
    }

    @Test
    public void testAdjustWalletBalanceSuccess() {
        CustomerAccountState state = createState();

        assertTrue(state.adjustWalletBalance(100.0));
        assertEquals(100.0, state.getWalletBalance(), 0.0001);

        assertTrue(state.adjustWalletBalance(-40.0));
        assertEquals(60.0, state.getWalletBalance(), 0.0001);
    }

    @Test
    public void testAdjustWalletBalanceRejectsNegativeResult() {
        CustomerAccountState state = createState();

        state.addWalletFunds(50.0);

        assertFalse(state.adjustWalletBalance(-60.0));
        assertEquals(50.0, state.getWalletBalance(), 0.0001);
    }

    @Test
    public void testAddInvoiceAndRentalCounts() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice paid =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Paid",
                        "Card"
                );

        CustomerAccountState.CustomerInvoice pending =
                createInvoice(
                        "INV-2",
                        200.0,
                        10.0,
                        "Pending",
                        "Cash"
                );

        CustomerAccountState.CustomerInvoice overdue =
                createInvoice(
                        "INV-3",
                        300.0,
                        20.0,
                        "Overdue",
                        "Bank Transfer"
                );

        state.addInvoice(null);
        state.addInvoice(paid);
        state.addInvoice(pending);
        state.addInvoice(overdue);

        assertEquals(3, state.getTotalRentals());
        assertEquals(2, state.getActiveRentals());
        assertEquals(1, state.getCompletedRentals());
        assertEquals(1, state.getLateReturns());
        assertEquals(1, state.getPaidInvoices());
        assertEquals(2, state.getPendingPayments());
        assertEquals(30.0, state.getLateFees(), 0.0001);
    }

    @Test
    public void testTotalSpentAndOutstandingBalance() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice paid =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Paid",
                        "Card"
                );

        CustomerAccountState.CustomerInvoice pending =
                createInvoice(
                        "INV-2",
                        200.0,
                        10.0,
                        "Pending",
                        "Cash"
                );

        state.addInvoice(paid);
        state.addInvoice(pending);

        assertEquals(
                paid.totalAmount(),
                state.getTotalSpent(),
                0.0001
        );

        assertEquals(
                pending.totalAmount(),
                state.getOutstandingBalance(),
                0.0001
        );
    }

    @Test
    public void testRecordPaymentNullInvoice() {
        CustomerAccountState state = createState();

        assertFalse(state.recordPayment(null, "Card"));
    }

    @Test
    public void testRecordPaymentAlreadyPaid() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Paid",
                        "Card"
                );

        assertFalse(state.recordPayment(invoice, "Cash"));
    }

    @Test
    public void testRecordPaymentInsufficientBalance() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Pending",
                        "Card"
                );

        assertFalse(state.recordPayment(invoice, "Cash"));
        assertEquals("Pending", invoice.status);
    }

    @Test
    public void testRecordPaymentSuccessAndDefaultCard() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Pending",
                        "Cash"
                );

        state.addWalletFunds(200.0);

        assertTrue(state.recordPayment(invoice, "   "));
        assertEquals("Paid", invoice.status);
        assertEquals("Card", invoice.paymentMethod);
        assertEquals(
                200.0 - invoice.totalAmount(),
                state.getWalletBalance(),
                0.0001
        );
    }

    @Test
    public void testAddPaidInvoiceSuccess() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Pending",
                        "Cash"
                );

        state.addWalletFunds(200.0);

        assertTrue(state.addPaidInvoice(invoice, "Bank Transfer"));
        assertEquals("Paid", invoice.status);
        assertEquals("Bank Transfer", invoice.paymentMethod);
        assertTrue(state.getInvoices().contains(invoice));
    }

    @Test
    public void testAddPaidInvoiceNullOrInsufficientFunds() {
        CustomerAccountState state = createState();

        assertFalse(state.addPaidInvoice(null, "Card"));

        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Pending",
                        "Card"
                );

        assertFalse(state.addPaidInvoice(invoice, "Card"));
        assertTrue(state.getInvoices().isEmpty());
    }

    @Test
    public void testApplyLateFeeBlankOrMissingInvoice() {
        CustomerAccountState state = createState();

        CustomerAccountState.LateFeeChargeResult blank =
                state.applyLateFee("   ", 25.0);

        assertEquals(0.0, blank.chargedNow(), 0.0001);
        assertEquals(0.0, blank.remainingAmount(), 0.0001);
        assertEquals(25.0, blank.totalLateFee(), 0.0001);

        CustomerAccountState.LateFeeChargeResult missing =
                state.applyLateFee("INV-999", -10.0);

        assertEquals(0.0, missing.chargedNow(), 0.0001);
        assertEquals(0.0, missing.remainingAmount(), 0.0001);
        assertEquals(0.0, missing.totalLateFee(), 0.0001);
    }

    @Test
    public void testApplyLateFeeFullyChargedFromWallet() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Paid",
                        "Card"
                );

        state.addInvoice(invoice);
        state.addWalletFunds(50.0);

        CustomerAccountState.LateFeeChargeResult result =
                state.applyLateFee("inv-1", 20.0);

        assertEquals(20.0, result.chargedNow(), 0.0001);
        assertEquals(0.0, result.remainingAmount(), 0.0001);
        assertEquals(20.0, result.totalLateFee(), 0.0001);
        assertEquals("Paid", invoice.status);
        assertEquals(30.0, state.getWalletBalance(), 0.0001);
        assertTrue(state.isInvoiceFullyPaid("INV-1"));
    }

    @Test
    public void testApplyLateFeePartiallyChargedBecomesOverdue() {
        CustomerAccountState state = createState();

        CustomerAccountState.CustomerInvoice invoice =
                createInvoice(
                        "INV-1",
                        100.0,
                        0.0,
                        "Paid",
                        "Card"
                );

        state.addInvoice(invoice);
        state.addWalletFunds(5.0);

        CustomerAccountState.LateFeeChargeResult result =
                state.applyLateFee("INV-1", 20.0);

        assertEquals(5.0, result.chargedNow(), 0.0001);
        assertEquals(15.0, result.remainingAmount(), 0.0001);
        assertEquals(20.0, result.totalLateFee(), 0.0001);
        assertEquals("Overdue", invoice.status);
        assertEquals(0.0, state.getWalletBalance(), 0.0001);
        assertFalse(state.isInvoiceFullyPaid("INV-1"));
    }

    @Test
    public void testIsInvoiceFullyPaidBlankAndMissingReturnTrue() {
        CustomerAccountState state = createState();

        assertTrue(state.isInvoiceFullyPaid(null));
        assertTrue(state.isInvoiceFullyPaid("   "));
        assertTrue(state.isInvoiceFullyPaid("INV-999"));
    }

    @Test
    public void testRedeemRewardSuccessAndFailure() {
        CustomerAccountState state = createState();

        assertFalse(state.redeemReward(0));
        assertFalse(state.redeemReward(-10));
        assertFalse(state.redeemReward(5000));

        assertTrue(state.redeemReward(100));
        assertEquals(750, state.getLoyaltyPoints());
        assertEquals(13, state.getRewardsUsed());
    }

    @Test
    public void testTierNamesAndNextTierData() {
        CustomerAccountState state = createState();

        assertEquals("Silver", state.getTierName());
        assertEquals(650, state.getPointsToNextTier());
        assertEquals("Gold", state.getNextTierName());

        assertTrue(state.redeemReward(400));

        assertEquals("Bronze", state.getTierName());
        assertEquals(50, state.getPointsToNextTier());
        assertEquals("Silver", state.getNextTierName());
    }

    @Test
    public void testGoldAndPlatinumTiers() {
        CustomerAccountState goldState = createState();

        CustomerAccountState.CustomerInvoice goldInvoice =
                createInvoice(
                        "INV-GOLD",
                        13000.0,
                        0.0,
                        "Paid",
                        "Card"
                );

        goldState.addInvoice(goldInvoice);

        assertEquals("Gold", goldState.getTierName());
        assertEquals("Platinum", goldState.getNextTierName());
        assertTrue(goldState.getPointsToNextTier() > 0);

        clearStaticStateSilently();
        FilesDeleteSilently(TEST_FILE);

        CustomerAccountState platinumState = createState();

        CustomerAccountState.CustomerInvoice platinumInvoice =
                createInvoice(
                        "INV-PLATINUM",
                        43000.0,
                        0.0,
                        "Paid",
                        "Card"
                );

        platinumState.addInvoice(platinumInvoice);

        assertEquals("Platinum", platinumState.getTierName());
        assertEquals(0, platinumState.getPointsToNextTier());
        assertEquals("Platinum", platinumState.getNextTierName());
    }

    private void clearStaticStateSilently() {
        try {
            clearStaticState();
        } catch (Exception ex) {
            fail(ex);
        }
    }

    private void FilesDeleteSilently(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ex) {
            fail(ex);
        }
    }

    @Test
    public void testCreateInvoiceWithExplicitDates() {
        Customer customer = createCustomer();

        java.time.LocalDateTime start =
                java.time.LocalDateTime.of(
                        2026, 7, 14, 10, 0
                );

        java.time.LocalDateTime end =
                java.time.LocalDateTime.of(
                        2026, 7, 17, 10, 0
                );

        CustomerAccountState.CustomerInvoice invoice =
                CustomerAccountState.createInvoice(
                        customer,
                        5,
                        "BMW X5",
                        3,
                        750.0,
                        20.0,
                        "Pending",
                        "Cash",
                        start,
                        end
                );

        assertEquals("INV-1006", invoice.invoiceId);
        assertEquals("Hamada Ahmad", invoice.customerName);
        assertEquals("BMW X5", invoice.vehicleName);
        assertEquals(3, invoice.rentalDays);
        assertEquals("14 Jul 2026 10:00 AM", invoice.startDate);
        assertEquals("17 Jul 2026 10:00 AM", invoice.endDate);
    }

    @Test
    public void testCreateInvoiceHandlesNullDates() {
        CustomerAccountState.CustomerInvoice invoice =
                CustomerAccountState.createInvoice(
                        createCustomer(),
                        0,
                        "BMW X5",
                        2,
                        500.0,
                        0.0,
                        "Pending",
                        "Card",
                        null,
                        null
                );

        assertEquals("INV-1001", invoice.invoiceId);
        assertNotNull(invoice.startDate);
        assertNotNull(invoice.endDate);
    }

    @Test
    public void testStatePersistsAndReloads() throws Exception {
        CustomerAccountState first = createState();

        first.addWalletFunds(500.0);
        first.addInvoice(
                createInvoice(
                        "INV-PERSIST",
                        100.0,
                        0.0,
                        "Paid",
                        "Card"
                )
        );

        assertTrue(Files.exists(TEST_FILE));

        clearStaticState();

        CustomerAccountState reloaded =
                CustomerAccountState.forCustomer(createCustomer());

        assertEquals(500.0, reloaded.getWalletBalance(), 0.0001);
        assertEquals(1, reloaded.getInvoices().size());
        assertEquals(
                "INV-PERSIST",
                reloaded.getInvoices().get(0).invoiceId
        );
    }

   @Test
public void testBillingMetricsSnapshot() {

    BillingMetricsBus.Metrics before =
            CustomerAccountState.billingMetricsSnapshot();

    CustomerAccountState state = createState();

    state.addInvoice(
            createInvoice(
                    "INV-1",
                    100.0,
                    0.0,
                    "Paid",
                    "Card"
            )
    );

    state.addInvoice(
            createInvoice(
                    "INV-2",
                    200.0,
                    10.0,
                    "Pending",
                    "Cash"
            )
    );

    BillingMetricsBus.Metrics after =
            CustomerAccountState.billingMetricsSnapshot();

    assertEquals(
            before.invoiceCount + 2,
            after.invoiceCount
    );

    assertEquals(
            before.paidInvoices + 1,
            after.paidInvoices
    );

    assertEquals(
            before.pendingPayments + 1,
            after.pendingPayments
    );

    assertEquals(
            before.overdueInvoices,
            after.overdueInvoices
    );

    assertTrue(
            after.paidRevenue > before.paidRevenue
    );

    assertTrue(
            after.outstandingBalance > before.outstandingBalance
    );

    assertEquals(
            before.lateFees + 10.0,
            after.lateFees,
            0.0001
    );
}
}