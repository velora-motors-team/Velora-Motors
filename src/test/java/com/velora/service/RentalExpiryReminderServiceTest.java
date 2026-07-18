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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @Test
    void defaultConstructorCreatesService() {
        assertNotNull(new RentalExpiryReminderService());
    }

    @Test
    void rejectsEachNullDependency() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RentalExpiryReminderService(null, emailService, fixedClock)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new RentalExpiryReminderService(rentalRepository, null, fixedClock)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new RentalExpiryReminderService(rentalRepository, emailService, null)
        );
    }

    @Test
    void blankCustomerEmailReturnsZeroWithoutRepositoryCall() {
        assertEquals(0, service.sendUpcomingReminders(null));
        assertEquals(0, service.sendUpcomingReminders("   "));
        verifyNoInteractions(rentalRepository, emailService);
    }

    @Test
    void rejectsNullAndNegativeReminderWindows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendUpcomingReminders("customer@velora.com", null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendUpcomingReminders(
                        "customer@velora.com", Duration.ofMinutes(-1)
                )
        );
    }

    @Test
    void skipsNullBlankAndMalformedExpectedReturnTimes() {
        RentalRecord nullDate = createRentalWithRawDate("ACTIVE", null);
        RentalRecord blankDate = createRentalWithRawDate(" ACTIVE ", "   ");
        RentalRecord malformedDate = createRentalWithRawDate("active", "not-a-date");

        when(rentalRepository.findByCustomerEmail("customer@velora.com"))
                .thenReturn(List.of(nullDate, blankDate, malformedDate));

        assertEquals(0, service.sendUpcomingReminders("customer@velora.com"));
        verify(emailService, never()).hasRentalExpiryReminder(anyString(), anyString());
        verify(emailService, never()).sendRentalExpiryReminder(
                anyString(), anyString(), anyString(), any(LocalDateTime.class)
        );
    }

    @Test
    void skipsNullStatusAndAcceptsReturnExactlyAtWindowLimit() {
        RentalRecord nullStatus = createRentalWithRawDate(
                null, LocalDateTime.of(2026, 7, 18, 11, 0).toString()
        );
        RentalRecord atLimit = createRentalWithRawDate(
                " active ", LocalDateTime.of(2026, 7, 18, 12, 0).toString()
        );

        when(rentalRepository.findByCustomerEmail("customer@velora.com"))
                .thenReturn(List.of(nullStatus, atLimit));
        when(emailService.hasRentalExpiryReminder(
                "customer@velora.com", "RNT-1"
        )).thenReturn(false);

        assertEquals(1, service.sendUpcomingReminders(
                "customer@velora.com", Duration.ofHours(2)
        ));
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

    private RentalRecord createRentalWithRawDate(String status, String expectedReturn) {
        return new RentalRecord(
                "RNT-1",
                "customer@velora.com",
                "Test Customer",
                "VEH-1",
                "BMW X5",
                "2026-07-17T10:00:00",
                expectedReturn,
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
