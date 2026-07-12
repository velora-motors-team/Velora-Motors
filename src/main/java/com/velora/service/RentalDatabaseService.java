package com.velora.service;

import com.velora.authentication.Customer;
import com.velora.repository.LoyaltyTransactionRepository;
import com.velora.repository.NotificationRepository;
import com.velora.repository.PaymentRepository;
import com.velora.repository.RentalRepository;
import com.velora.repository.WalletTransactionRepository;
import com.velora.vehicle.Vehicle;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public final class RentalDatabaseService {

    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final LoyaltyTransactionRepository loyaltyRepository;
    private final NotificationRepository notificationRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public RentalDatabaseService() {
        this(
                new RentalRepository(),
                new PaymentRepository(),
                new LoyaltyTransactionRepository(),
                new NotificationRepository(),
                new WalletTransactionRepository()
        );
    }

    public RentalDatabaseService(
            RentalRepository rentalRepository,
            PaymentRepository paymentRepository,
            LoyaltyTransactionRepository loyaltyRepository,
            NotificationRepository notificationRepository,
            WalletTransactionRepository walletTransactionRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.paymentRepository = paymentRepository;
        this.loyaltyRepository = loyaltyRepository;
        this.notificationRepository = notificationRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public RentalRepository.RentalRecord recordSuccessfulRental(
            Customer customer,
            Vehicle vehicle,
            int rentalDays,
            String invoiceId,
            double baseAmount,
            double totalAmount,
            String paymentMethod,
            double walletBalanceAfter
    ) {
        LocalDateTime start = LocalDateTime.now();

        return recordSuccessfulRental(
                customer,
                vehicle,
                rentalDays,
                invoiceId,
                baseAmount,
                totalAmount,
                paymentMethod,
                walletBalanceAfter,
                start,
                start.plusDays(rentalDays)
        );
    }

    public RentalRepository.RentalRecord recordSuccessfulRental(
            Customer customer,
            Vehicle vehicle,
            int rentalDays,
            String invoiceId,
            double baseAmount,
            double totalAmount,
            String paymentMethod,
            double walletBalanceAfter,
            LocalDateTime start,
            LocalDateTime expectedReturn
    ) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }

        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be greater than zero.");
        }

        if (start == null || expectedReturn == null || !expectedReturn.isAfter(start)) {
            throw new IllegalArgumentException("A valid rental period is required.");
        }

        String email = customer.getEmail();
        String customerName = customer.getFullName();
        String vehicleName = vehicle.getDisplayName();

        RentalRepository.RentalRecord rental = rentalRepository.create(
                email,
                customerName,
                vehicle.getId(),
                vehicleName,
                start,
                expectedReturn,
                rentalDays,
                vehicle.getDailyPrice(),
                baseAmount,
                invoiceId,
                "PAID"
        );

        paymentRepository.create(
                invoiceId,
                rental.rentalId(),
                email,
                totalAmount,
                paymentMethod,
                "COMPLETED"
        );

        int earnedPoints = Math.max(1, (int) Math.round(totalAmount / 20.0));

        loyaltyRepository.add(
                email,
                earnedPoints,
                "EARNED",
                vehicleName + " rental completed",
                rental.rentalId()
        );

        walletTransactionRepository.add(
                email,
                "PAYMENT",
                -totalAmount,
                walletBalanceAfter,
                invoiceId
        );

        notificationRepository.create(
                email,
                "RENTAL_CONFIRMED",
                "Rental confirmed",
                "Your " + vehicleName + " rental is confirmed until "
                        + expectedReturn.toLocalDate() + "."
        );

        return rental;
    }

    public boolean markReturnDue(String rentalId) {
        Optional<RentalRepository.RentalRecord> current = rentalRepository.findById(rentalId);

        if (current.isEmpty()) {
            return false;
        }

        String status = normalizeStatus(current.get().status());

        if ("RETURN_DUE".equals(status)) {
            return true;
        }

        if (!"ACTIVE".equals(status)) {
            return false;
        }

        boolean updated = rentalRepository.updateTimingStatus(
                rentalId,
                "RETURN_DUE",
                current.get().lateFee()
        );

        if (updated) {
            notificationRepository.create(
                    current.get().customerEmail(),
                    "RETURN_DUE",
                    "Vehicle return is due",
                    "Your " + current.get().vehicleName()
                            + " rental has ended. You now have 30 minutes of grace time to return it."
            );
        }

        return updated;
    }

    public boolean recordLateFeeCharge(
            Customer customer,
            RentalRepository.RentalRecord rental,
            double calculatedLateFee,
            double chargedNow,
            double walletBalanceAfter
    ) {
        if (rental == null) {
            return false;
        }

        Optional<RentalRepository.RentalRecord> current = rentalRepository.findById(rental.rentalId());

        if (current.isEmpty()) {
            return false;
        }

        String oldStatus = normalizeStatus(current.get().status());
        double safeLateFee = Math.max(0.0, calculatedLateFee);

        boolean needsRentalUpdate =
                !"OVERDUE".equals(oldStatus)
                        || Math.abs(current.get().lateFee() - safeLateFee) > 0.001;

        boolean updated = !needsRentalUpdate || rentalRepository.updateTimingStatus(
                rental.rentalId(),
                "OVERDUE",
                safeLateFee
        );

        if (!updated) {
            return false;
        }

        if (!"OVERDUE".equals(oldStatus)) {
            notificationRepository.create(
                    rental.customerEmail(),
                    "RENTAL_OVERDUE",
                    "Rental overdue",
                    "Your " + rental.vehicleName()
                            + " is overdue. A late fee is now being calculated automatically."
            );
        }

        if (chargedNow > 0.001) {
            String email = customer == null
                    ? rental.customerEmail()
                    : customer.getEmail();

            walletTransactionRepository.add(
                    email,
                    "LATE_FEE",
                    -chargedNow,
                    walletBalanceAfter,
                    rental.invoiceId()
            );

            notificationRepository.create(
                    email,
                    "LATE_FEE_CHARGED",
                    "Late fee charged",
                    String.format(
                            Locale.US,
                            "$%,.2f was deducted from your wallet for late return of %s.",
                            chargedNow,
                            rental.vehicleName()
                    )
            );
        }

        return true;
    }

    public boolean markRentalReturned(
            Customer customer,
            String rentalId,
            double finalLateFee
    ) {
        Optional<RentalRepository.RentalRecord> current = rentalRepository.findById(rentalId);

        if (current.isEmpty()) {
            return false;
        }

        boolean updated = rentalRepository.markReturned(
                rentalId,
                LocalDateTime.now(),
                Math.max(0.0, finalLateFee)
        );

        if (updated) {
            String email = customer == null
                    ? current.get().customerEmail()
                    : customer.getEmail();

            notificationRepository.create(
                    email,
                    "VEHICLE_RETURNED",
                    "Vehicle returned",
                    "Your " + current.get().vehicleName()
                            + " return has been completed successfully."
            );
        }

        return updated;
    }

    private static String normalizeStatus(String status) {
        return status == null
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);
    }
}
