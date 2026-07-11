package com.velora.ui;

import com.velora.authentication.Customer;
import com.velora.repository.RatingRepository;
import com.velora.service.AuthenticationService;
import com.velora.service.VehicleService;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class VeloraEndToEndCheck {

    private static final String CUSTOMER_NAME = "Velora E2E Customer";
    private static final String CUSTOMER_EMAIL = "velora.e2e.customer@velora.com";
    private static final String CUSTOMER_PHONE = "0599001122";
    private static final char[] CUSTOMER_PASSWORD = "Velora@123".toCharArray();
    private static final String ADMIN_EMAIL = "admin@velora.com";
    private static final char[] ADMIN_PASSWORD = "Admin@123".toCharArray();

    private VeloraEndToEndCheck() {
    }

    public static void main(String[] args) {
        EndToEndResult result = runScenario();
        System.out.println(result.summary());
        if (!result.passed()) {
            System.exit(1);
        }
    }

    static EndToEndResult runScenario() {
        AuthenticationService authenticationService = new AuthenticationService();
        VehicleService vehicleService = new VehicleService();
        RatingRepository ratingRepository = new RatingRepository();
        SupportMessageRepository supportRepository = new SupportMessageRepository();

        Customer customer = ensureCustomer(authenticationService);
        Customer authenticatedCustomer = authenticateCustomer(authenticationService);
        Optional<Customer> authenticatedAdmin = authenticationService.authenticateCustomer(ADMIN_EMAIL, ADMIN_PASSWORD)
                .filter(Customer::isManager);

        CustomerAccountState accountState = CustomerAccountState.forCustomer(authenticatedCustomer);
        double walletBefore = accountState.getWalletBalance();
        BillingMetricsBus.Metrics metricsBefore = BillingMetricsBus.snapshot();

        Vehicle rentedVehicle = firstVehicle(vehicleService.getAllVehicles());
        String vehicleName = FleetUiData.displayName(rentedVehicle);
        CustomerAccountState.CustomerInvoice invoice = createPaidRentalInvoice(
                authenticatedCustomer,
                rentedVehicle,
                nextInvoiceId(accountState.getInvoices().size())
        );

        if (!accountState.canAfford(invoice.totalAmount())) {
            accountState.addWalletFunds(invoice.totalAmount() + 1000.0);
            walletBefore = accountState.getWalletBalance();
        }

        boolean invoiceSaved = accountState.addPaidInvoice(invoice, "Card");
        rentedVehicle.setStatus(VehicleStatus.RENTED);
        vehicleService.saveVehicles();

        String ratingComment = vehicleName + " | End-to-end customer rating from the full binding check.";
        ratingRepository.saveRating(authenticatedCustomer, 5, ratingComment);

        SupportMessage customerMessage = new SupportMessage(
                "E2E-" + System.currentTimeMillis(),
                authenticatedCustomer.getFullName(),
                authenticatedCustomer.getEmail(),
                SupportMessage.MessageType.BILLING_ISSUE,
                "Rental payment confirmation",
                "Customer completed a rental, paid the invoice, rated the vehicle, and needs admin confirmation."
        );
        supportRepository.saveMessage(customerMessage);

        SupportMessage adminVisibleMessage = findSupportMessage(
                supportRepository.loadMessages(),
                customerMessage.getMessageId()
        );
        adminVisibleMessage.setRead(true);
        adminVisibleMessage.setStatus(SupportMessage.MessageStatus.RESOLVED);
        adminVisibleMessage.setAdminReply(
                "Admin confirmed the rental, payment, vehicle status, rating, and billing record successfully."
        );
        adminVisibleMessage.setReplyDateTime(LocalDateTime.now());
        boolean adminReplySaved = supportRepository.updateMessage(adminVisibleMessage);

        BillingMetricsBus.Metrics metricsAfter = BillingMetricsBus.snapshot();
        boolean accountVisibleToAdmin = authenticationService.getAllAccounts().stream()
                .anyMatch(account -> CUSTOMER_EMAIL.equals(account.getEmail()));
        boolean billingVisibleToAdmin = metricsAfter.paidInvoices > metricsBefore.paidInvoices
                && metricsAfter.paidRevenue >= metricsBefore.paidRevenue + invoice.totalAmount() - 0.01;
        boolean ratingVisibleToAdmin = ratingRepository.countForEmail(CUSTOMER_EMAIL) > 0
                && ratingRepository.averageForEmail(CUSTOMER_EMAIL).orElse(0) >= 5.0;
        boolean supportVisibleToAdmin = supportRepository.loadMessages().stream()
                .anyMatch(message -> customerMessage.getMessageId().equals(message.getMessageId())
                        && message.hasAdminReply()
                        && message.getStatus() == SupportMessage.MessageStatus.RESOLVED);
        boolean customerCanSeeAdminReply = supportRepository.loadMessages().stream()
                .anyMatch(message -> customerMessage.getMessageId().equals(message.getMessageId())
                        && CUSTOMER_EMAIL.equals(message.getCustomerEmail())
                        && message.hasAdminReply());
        boolean vehicleStatusSaved = vehicleService.getAllVehicles().stream()
                .anyMatch(vehicle -> rentedVehicle.getId().equals(vehicle.getId())
                        && vehicle.getStatus() == VehicleStatus.RENTED);

        return new EndToEndResult(
                customer,
                authenticatedAdmin.isPresent(),
                invoice.invoiceId,
                vehicleName,
                invoiceSaved,
                walletBefore,
                accountState.getWalletBalance(),
                accountVisibleToAdmin,
                billingVisibleToAdmin,
                ratingVisibleToAdmin,
                supportVisibleToAdmin,
                customerCanSeeAdminReply,
                adminReplySaved,
                vehicleStatusSaved,
                customerMessage.getMessageId()
        );
    }

    private static Customer ensureCustomer(AuthenticationService authenticationService) {
        if (!authenticationService.emailExists(CUSTOMER_EMAIL)) {
            return authenticationService.registerCustomer(
                    CUSTOMER_NAME,
                    CUSTOMER_EMAIL,
                    CUSTOMER_PHONE,
                    CUSTOMER_PASSWORD
            );
        }

        authenticationService.updateProfile(CUSTOMER_EMAIL, CUSTOMER_NAME, CUSTOMER_PHONE);
        Optional<Customer> existing = authenticationService.authenticateCustomer(
                CUSTOMER_EMAIL,
                CUSTOMER_PASSWORD
        );

        if (existing.isPresent()) {
            return existing.get();
        }

        authenticationService.resetPassword(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        return authenticateCustomer(authenticationService);
    }

    private static Customer authenticateCustomer(AuthenticationService authenticationService) {
        return authenticationService.authenticateCustomer(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)
                .orElseThrow(() -> new IllegalStateException("Unable to authenticate the E2E customer."));
    }

    private static Vehicle firstVehicle(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) {
            throw new IllegalStateException("No vehicles are available in the fleet.");
        }
        return vehicles.stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE)
                .findFirst()
                .orElse(vehicles.get(0));
    }

    private static CustomerAccountState.CustomerInvoice createPaidRentalInvoice(
            Customer customer,
            Vehicle vehicle,
            String invoiceId
    ) {
        int rentalDays = 3;
        return new CustomerAccountState.CustomerInvoice(
                invoiceId,
                CustomerAccountState.resolveCustomerName(customer),
                FleetUiData.displayName(vehicle),
                rentalDays,
                vehicle.getDailyPrice() * rentalDays,
                0,
                "Paid",
                "Card",
                "11 Jul 2026 10:00 AM",
                "14 Jul 2026 10:00 AM"
        );
    }

    private static String nextInvoiceId(int existingInvoices) {
        return "E2E-INV-" + (existingInvoices + 1) + "-" + System.currentTimeMillis();
    }

    private static SupportMessage findSupportMessage(List<SupportMessage> messages, String messageId) {
        return messages.stream()
                .filter(message -> messageId.equals(message.getMessageId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Admin could not see the customer support message."));
    }

    record EndToEndResult(
            Customer customer,
            boolean adminLoginWorks,
            String invoiceId,
            String vehicleName,
            boolean invoiceSaved,
            double walletBefore,
            double walletAfter,
            boolean accountVisibleToAdmin,
            boolean billingVisibleToAdmin,
            boolean ratingVisibleToAdmin,
            boolean supportVisibleToAdmin,
            boolean customerCanSeeAdminReply,
            boolean adminReplySaved,
            boolean vehicleStatusSaved,
            String supportMessageId
    ) {
        boolean passed() {
            return adminLoginWorks
                    && invoiceSaved
                    && walletAfter < walletBefore
                    && accountVisibleToAdmin
                    && billingVisibleToAdmin
                    && ratingVisibleToAdmin
                    && supportVisibleToAdmin
                    && customerCanSeeAdminReply
                    && adminReplySaved
                    && vehicleStatusSaved;
        }

        String summary() {
            return String.join(System.lineSeparator(),
                    "VELORA END-TO-END CUSTOMER/ADMIN CHECK",
                    "Status: " + (passed() ? "PASSED" : "FAILED"),
                    "Customer created/authenticated: " + customer.getFullName() + " <" + customer.getEmail() + ">",
                    "Admin login works: " + adminLoginWorks,
                    "Vehicle rented: " + vehicleName,
                    "Invoice: " + invoiceId,
                    "Wallet before: $" + String.format("%,.2f", walletBefore),
                    "Wallet after: $" + String.format("%,.2f", walletAfter),
                    "Support message: " + supportMessageId,
                    "Checks:",
                    "- Customer account visible to admin: " + accountVisibleToAdmin,
                    "- Billing/analytics metrics updated: " + billingVisibleToAdmin,
                    "- Rating visible to admin: " + ratingVisibleToAdmin,
                    "- Support message visible/resolved by admin: " + supportVisibleToAdmin,
                    "- Customer can see admin reply: " + customerCanSeeAdminReply,
                    "- Admin reply saved: " + adminReplySaved,
                    "- Vehicle status saved as rented: " + vehicleStatusSaved
            );
        }
    }
}
