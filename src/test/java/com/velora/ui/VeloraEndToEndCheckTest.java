package com.velora.ui;

import com.velora.authentication.Customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VeloraEndToEndCheckTest {

    private Customer createCustomer() {
        return new Customer(
                "Velora E2E Customer",
                "velora.e2e.customer@velora.com",
                "0599001122"
        );
    }

    @Test
    public void testPassedReturnsTrueWhenAllChecksPass() {
        VeloraEndToEndCheck.EndToEndResult result =
                new VeloraEndToEndCheck.EndToEndResult(
                        createCustomer(),
                        true,
                        "INV-1",
                        "BMW X5",
                        true,
                        1000.0,
                        500.0,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        "MSG-1"
                );

        assertTrue(result.passed());
    }

    @Test
    public void testPassedReturnsFalseWhenOneCheckFails() {
        VeloraEndToEndCheck.EndToEndResult result =
                new VeloraEndToEndCheck.EndToEndResult(
                        createCustomer(),
                        false,
                        "INV-1",
                        "BMW X5",
                        true,
                        1000.0,
                        500.0,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        "MSG-1"
                );

        assertFalse(result.passed());
    }

    @Test
    public void testPassedReturnsFalseWhenWalletDidNotDecrease() {
        VeloraEndToEndCheck.EndToEndResult result =
                new VeloraEndToEndCheck.EndToEndResult(
                        createCustomer(),
                        true,
                        "INV-1",
                        "BMW X5",
                        true,
                        500.0,
                        500.0,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        "MSG-1"
                );

        assertFalse(result.passed());
    }

    @Test
    public void testSummaryForPassedScenario() {
        VeloraEndToEndCheck.EndToEndResult result =
                new VeloraEndToEndCheck.EndToEndResult(
                        createCustomer(),
                        true,
                        "INV-1",
                        "BMW X5",
                        true,
                        1000.0,
                        500.0,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        "MSG-1"
                );

        String summary = result.summary();

        assertTrue(summary.contains(
                "VELORA END-TO-END CUSTOMER/ADMIN CHECK"
        ));
        assertTrue(summary.contains("Status: PASSED"));
        assertTrue(summary.contains(
                "Customer created/authenticated: Velora E2E Customer"
        ));
        assertTrue(summary.contains("Admin login works: true"));
        assertTrue(summary.contains("Vehicle rented: BMW X5"));
        assertTrue(summary.contains("Invoice: INV-1"));
        assertTrue(summary.contains("Wallet before: $1,000.00"));
        assertTrue(summary.contains("Wallet after: $500.00"));
        assertTrue(summary.contains("Support message: MSG-1"));
    }

    @Test
    public void testSummaryForFailedScenario() {
        VeloraEndToEndCheck.EndToEndResult result =
                new VeloraEndToEndCheck.EndToEndResult(
                        createCustomer(),
                        true,
                        "INV-1",
                        "BMW X5",
                        false,
                        1000.0,
                        500.0,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        "MSG-1"
                );

        assertTrue(result.summary().contains("Status: FAILED"));
    }
}