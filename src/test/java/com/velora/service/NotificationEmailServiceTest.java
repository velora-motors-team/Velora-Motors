package com.velora.service;

import com.velora.repository.NotificationRepository;
import com.velora.repository.NotificationRepository.NotificationRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationEmailServiceTest {

    @Test
    void defaultConstructorCreatesService() {
        assertNotNull(new NotificationEmailService());
    }

    @Test
    void rejectsNullRepository() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new NotificationEmailService(null)
        );

        assertEquals(
                "Notification repository is required.",
                exception.getMessage()
        );
    }

    @Test
    void blankOrNullRentalIdHasNoReminder() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailService service = new NotificationEmailService(repository);

        assertFalse(service.hasRentalExpiryReminder("customer@velora.com", null));
        assertFalse(service.hasRentalExpiryReminder("customer@velora.com", "   "));
        verifyNoInteractions(repository);
    }

    @Test
    void findsExistingReminderIgnoringTypeCase() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailService service = new NotificationEmailService(repository);
        when(repository.findByCustomerEmail("customer@velora.com"))
                .thenReturn(List.of(notification(
                        "rental_expiry_reminder",
                        "Please return the vehicle. Rental ID: RNT-42."
                )));

        assertTrue(service.hasRentalExpiryReminder(
                "customer@velora.com", "  RNT-42  "
        ));
    }

    @Test
    void ignoresOtherTypesNullMessagesAndOtherRentalIds() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailService service = new NotificationEmailService(repository);
        when(repository.findByCustomerEmail("customer@velora.com"))
                .thenReturn(List.of(
                        notification("OTHER", "Rental ID: RNT-42."),
                        notification("RENTAL_EXPIRY_REMINDER", null),
                        notification("RENTAL_EXPIRY_REMINDER", "Rental ID: RNT-99.")
                ));

        assertFalse(service.hasRentalExpiryReminder(
                "customer@velora.com", "RNT-42"
        ));
    }

    @Test
    void sendsFormattedReminder() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailService service = new NotificationEmailService(repository);
        LocalDateTime expectedReturn = LocalDateTime.of(2026, 7, 19, 21, 30);

        service.sendRentalExpiryReminder(
                "customer@velora.com",
                "RNT-42",
                "  BMW X5  ",
                expectedReturn
        );

        verify(repository).create(
                "customer@velora.com",
                "RENTAL_EXPIRY_REMINDER",
                "Rental expiry reminder",
                "Reminder: Your BMW X5 rental expires on 19 Jul 2026, 09:30 PM. "
                        + "Please return or extend the vehicle. Rental ID: RNT-42."
        );
    }

    @Test
    void blankOrNullVehicleNameUsesFallback() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailService service = new NotificationEmailService(repository);
        LocalDateTime expectedReturn = LocalDateTime.of(2026, 7, 19, 9, 0);

        service.sendRentalExpiryReminder(
                "customer@velora.com", "RNT-1", "   ", expectedReturn
        );
        service.sendRentalExpiryReminder(
                "customer@velora.com", "RNT-2", null, expectedReturn
        );

        verify(repository, times(2)).create(
                eq("customer@velora.com"),
                eq("RENTAL_EXPIRY_REMINDER"),
                eq("Rental expiry reminder"),
                startsWith("Reminder: Your vehicle rental expires on")
        );
    }

    @Test
    void rejectsNullExpectedReturn() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailService service = new NotificationEmailService(repository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.sendRentalExpiryReminder(
                        "customer@velora.com", "RNT-1", "BMW X5", null
                )
        );

        assertEquals("Expected return time is required.", exception.getMessage());
        verifyNoInteractions(repository);
    }

    private static NotificationRecord notification(String type, String message) {
        return new NotificationRecord(
                "NOT-1",
                "customer@velora.com",
                type,
                "Title",
                message,
                "2026-07-18T10:00:00",
                false
        );
    }
}
