package com.velora.service;

import com.velora.repository.RentalRepository;
import com.velora.repository.RentalRepository.RentalRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RentalExpiryReminderServiceTest {

    private RentalRepository rentalRepository;
    private EmailService emailService;
    private RentalExpiryReminderService service;

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-07-18T10:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        rentalRepository = mock(RentalRepository.class);
        emailService = mock(EmailService.class);
        service = new RentalExpiryReminderService(
                rentalRepository,
                emailService,
                fixedClock
        );
    }

    @Test
    void sendsReminderForActiveRentalExpiringWithinTwentyFourHours() {
        RentalRecord rental = createRental(
                "ACTIVE",
                LocalDateTime.of(2026, 7, 19, 9, 0)
        );

        when(rentalRepository.findByCustomerEmail("customer@velora.com"))
                .thenReturn(List.of(rental));
        when(emailService.hasRentalExpiryReminder(
                "customer@velora.com",
                "RNT-1"
        )).thenReturn(false);

        int sent = service.sendUpcomingReminders("customer@velora.com");

        assertEquals(1, sent);
        verify(emailService).sendRentalExpiryReminder(
                "customer@velora.com",
                "RNT-1",
                "BMW X5",
                LocalDateTime.of(2026, 7, 19, 9, 0)
        );
    }

    @Test
    void doesNotSendDuplicateReminder() {
        RentalRecord rental = createRental(
                "ACTIVE",
                LocalDateTime.of(2026, 7, 19, 9, 0)
        );

        when(rentalRepository.findByCustomerEmail("customer@velora.com"))
                .thenReturn(List.of(rental));
        when(emailService.hasRentalExpiryReminder(
                "customer@velora.com",
                "RNT-1"
        )).thenReturn(true);

        assertEquals(0, service.sendUpcomingReminders("customer@velora.com"));

        verify(emailService, never()).sendRentalExpiryReminder(
                "customer@velora.com",
                "RNT-1",
                "BMW X5",
                LocalDateTime.of(2026, 7, 19, 9, 0)
        );
    }

    @Test
    void skipsExpiredInactiveAndFarFutureRentals() {
        RentalRecord expired = createRental(
                "ACTIVE",
                LocalDateTime.of(2026, 7, 18, 9, 0)
        );
        RentalRecord returned = createRental(
                "RETURNED",
                LocalDateTime.of(2026, 7, 19, 9, 0)
        );
        RentalRecord farFuture = createRental(
                "ACTIVE",
                LocalDateTime.of(2026, 7, 20, 11, 0)
        );

        when(rentalRepository.findByCustomerEmail("customer@velora.com"))
                .thenReturn(List.of(expired, returned, farFuture));

        assertEquals(0, service.sendUpcomingReminders("customer@velora.com"));
        verify(emailService, never()).sendRentalExpiryReminder(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        );
    }

    @Test
    void rejectsInvalidReminderWindow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendUpcomingReminders(
                        "customer@velora.com",
                        Duration.ZERO
                )
        );
    }

    private RentalRecord createRental(
            String status,
            LocalDateTime expectedReturn
    ) {
        return new RentalRecord(
                "RNT-1",
                "customer@velora.com",
                "Test Customer",
                "VEH-1",
                "BMW X5",
                LocalDateTime.of(2026, 7, 17, 10, 0).toString(),
                expectedReturn.toString(),
                "",
                status,
                2,
                250.0,
                500.0,
                0.0,
                "INV-1",
                "PAID"
        );
    }
}
