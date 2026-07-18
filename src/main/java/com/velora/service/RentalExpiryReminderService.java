package com.velora.service;

import com.velora.repository.RentalRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Finds active rentals that will expire soon and sends one reminder per rental.
 */
public final class RentalExpiryReminderService {

    private static final Duration DEFAULT_REMINDER_WINDOW = Duration.ofHours(24);

    private final RentalRepository rentalRepository;
    private final EmailService emailService;
    private final Clock clock;

    /**
     * Creates the service with the project's default repositories and system clock.
     */
    public RentalExpiryReminderService() {
        this(
                new RentalRepository(),
                new NotificationEmailService(),
                Clock.systemDefaultZone()
        );
    }

    /**
     * Creates the service with injected dependencies for testing.
     *
     * @param rentalRepository rental storage
     * @param emailService reminder sender
     * @param clock date and time source
     */
    public RentalExpiryReminderService(
            RentalRepository rentalRepository,
            EmailService emailService,
            Clock clock
    ) {
        if (rentalRepository == null || emailService == null || clock == null) {
            throw new IllegalArgumentException("Reminder service dependencies are required.");
        }

        this.rentalRepository = rentalRepository;
        this.emailService = emailService;
        this.clock = clock;
    }

    /**
     * Sends reminders for the customer's rentals expiring within the next 24 hours.
     *
     * @param customerEmail customer email address
     * @return number of reminders sent
     */
    public int sendUpcomingReminders(String customerEmail) {
        return sendUpcomingReminders(customerEmail, DEFAULT_REMINDER_WINDOW);
    }

    /**
     * Sends reminders for active rentals expiring inside the supplied time window.
     *
     * @param customerEmail customer email address
     * @param reminderWindow time before expiry in which reminders are sent
     * @return number of reminders sent
     */
    public int sendUpcomingReminders(
            String customerEmail,
            Duration reminderWindow
    ) {
        if (customerEmail == null || customerEmail.isBlank()) {
            return 0;
        }

        if (reminderWindow == null
                || reminderWindow.isZero()
                || reminderWindow.isNegative()) {
            throw new IllegalArgumentException("Reminder window must be positive.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reminderLimit = now.plus(reminderWindow);
        int remindersSent = 0;

        for (RentalRepository.RentalRecord rental
                : rentalRepository.findByCustomerEmail(customerEmail)) {

            if (!"ACTIVE".equals(normalizeStatus(rental.status()))) {
                continue;
            }

            LocalDateTime expectedReturn = parseDateTime(rental.expectedReturnDateTime());

            if (expectedReturn == null
                    || !expectedReturn.isAfter(now)
                    || expectedReturn.isAfter(reminderLimit)) {
                continue;
            }

            if (emailService.hasRentalExpiryReminder(
                    rental.customerEmail(),
                    rental.rentalId()
            )) {
                continue;
            }

            emailService.sendRentalExpiryReminder(
                    rental.customerEmail(),
                    rental.rentalId(),
                    rental.vehicleName(),
                    expectedReturn
            );
            remindersSent++;
        }

        return remindersSent;
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String normalizeStatus(String status) {
        return status == null
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);
    }
}
