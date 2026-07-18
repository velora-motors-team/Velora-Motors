package com.velora.service;

import java.time.LocalDateTime;

/**
 * Sends rental-related email notifications.
 * The current implementation stores the message in the notification repository.
 */
public interface EmailService {

    /**
     * Checks whether an expiry reminder was already sent for a rental.
     *
     * @param customerEmail customer email address
     * @param rentalId rental identifier
     * @return true when the reminder already exists
     */
    boolean hasRentalExpiryReminder(String customerEmail, String rentalId);

    /**
     * Sends a reminder before a rental expires.
     *
     * @param customerEmail customer email address
     * @param rentalId rental identifier
     * @param vehicleName rented vehicle name
     * @param expectedReturn expected return date and time
     */
    void sendRentalExpiryReminder(
            String customerEmail,
            String rentalId,
            String vehicleName,
            LocalDateTime expectedReturn
    );
}
