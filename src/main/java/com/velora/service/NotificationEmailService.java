package com.velora.service;

import com.velora.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Email service adapter that saves email reminders as application notifications.
 */
public final class NotificationEmailService implements EmailService {

    private static final String REMINDER_TYPE = "RENTAL_EXPIRY_REMINDER";
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final NotificationRepository notificationRepository;

    /**
     * Creates the service using the default notification repository.
     */
    public NotificationEmailService() {
        this(new NotificationRepository());
    }

    /**
     * Creates the service with a supplied notification repository.
     *
     * @param notificationRepository notification storage
     */
    public NotificationEmailService(NotificationRepository notificationRepository) {
        if (notificationRepository == null) {
            throw new IllegalArgumentException("Notification repository is required.");
        }
        this.notificationRepository = notificationRepository;
    }

    @Override
    public boolean hasRentalExpiryReminder(String customerEmail, String rentalId) {
        if (rentalId == null || rentalId.isBlank()) {
            return false;
        }

        String rentalMarker = "Rental ID: " + rentalId.trim();

        return notificationRepository.findByCustomerEmail(customerEmail).stream()
                .anyMatch(notification ->
                        REMINDER_TYPE.equalsIgnoreCase(notification.type())
                                && notification.message() != null
                                && notification.message().contains(rentalMarker)
                );
    }

    @Override
    public void sendRentalExpiryReminder(
            String customerEmail,
            String rentalId,
            String vehicleName,
            LocalDateTime expectedReturn
    ) {
        if (expectedReturn == null) {
            throw new IllegalArgumentException("Expected return time is required.");
        }

        String safeVehicleName = vehicleName == null || vehicleName.isBlank()
                ? "vehicle"
                : vehicleName.trim();

        notificationRepository.create(
                customerEmail,
                REMINDER_TYPE,
                "Rental expiry reminder",
                "Reminder: Your " + safeVehicleName
                        + " rental expires on " + expectedReturn.format(DATE_TIME_FORMAT)
                        + ". Please return or extend the vehicle. Rental ID: "
                        + rentalId + "."
        );
    }
}
