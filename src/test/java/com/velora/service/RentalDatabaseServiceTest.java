package com.velora.service;

import com.velora.authentication.Customer;
import com.velora.repository.LoyaltyTransactionRepository;
import com.velora.repository.NotificationRepository;
import com.velora.repository.PaymentRepository;
import com.velora.repository.RentalRepository;
import com.velora.repository.RentalRepository.RentalRecord;
import com.velora.repository.WalletTransactionRepository;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RentalDatabaseServiceTest {

    private RentalRepository rentalRepository;
    private PaymentRepository paymentRepository;
    private LoyaltyTransactionRepository loyaltyRepository;
    private NotificationRepository notificationRepository;
    private WalletTransactionRepository walletTransactionRepository;
    private RentalDatabaseService service;

    @BeforeEach
    public void setUp() {
        rentalRepository = mock(RentalRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        loyaltyRepository = mock(LoyaltyTransactionRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        walletTransactionRepository = mock(WalletTransactionRepository.class);

        service = new RentalDatabaseService(
                rentalRepository,
                paymentRepository,
                loyaltyRepository,
                notificationRepository,
                walletTransactionRepository
        );
    }

    private Customer createCustomer() {
        return new Customer(
                "Hamada Ahmad",
                "HAMADA@EMAIL.COM",
                "0599999999"
        );
    }

    private Vehicle createVehicle() {
        return new Vehicle(
                "V001",
                "BMW",
                "X5",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                250.0
        );
    }

    private RentalRecord createRental(
            String status,
            double lateFee
    ) {
        return new RentalRecord(
                "RNT-1",
                "hamada@email.com",
                "Hamada Ahmad",
                "V001",
                "BMW X5",
                "2026-07-14T10:00:00",
                "2026-07-16T10:00:00",
                "",
                status,
                2,
                250.0,
                500.0,
                lateFee,
                "INV-1",
                "PAID"
        );
    }

    @Test
    public void testRecordSuccessfulRental() {
        Customer customer = createCustomer();
        Vehicle vehicle = createVehicle();

        LocalDateTime start = LocalDateTime.of(
                2026, 7, 14, 10, 0
        );

        LocalDateTime expectedReturn = start.plusDays(2);

        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.create(
                "hamada@email.com",
                "Hamada Ahmad",
                "V001",
                "BMW X5",
                start,
                expectedReturn,
                2,
                250.0,
                500.0,
                "INV-1",
                "PAID"
        )).thenReturn(rental);

        RentalRecord result = service.recordSuccessfulRental(
                customer,
                vehicle,
                2,
                "INV-1",
                500.0,
                550.0,
                "CARD",
                1000.0,
                start,
                expectedReturn
        );

        assertSame(rental, result);

        verify(rentalRepository).create(
                "hamada@email.com",
                "Hamada Ahmad",
                "V001",
                "BMW X5",
                start,
                expectedReturn,
                2,
                250.0,
                500.0,
                "INV-1",
                "PAID"
        );

        verify(paymentRepository).create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                550.0,
                "CARD",
                "COMPLETED"
        );

        verify(loyaltyRepository).add(
                "hamada@email.com",
                28,
                "EARNED",
                "BMW X5 rental completed",
                "RNT-1"
        );

        verify(walletTransactionRepository).add(
                "hamada@email.com",
                "PAYMENT",
                -550.0,
                1000.0,
                "INV-1"
        );

        verify(notificationRepository).create(
                "hamada@email.com",
                "RENTAL_CONFIRMED",
                "Rental confirmed",
                "Your BMW X5 rental is confirmed until 2026-07-16."
        );
    }

    @Test
    public void testRecordSuccessfulRentalMinimumOnePoint() {
        Customer customer = createCustomer();
        Vehicle vehicle = createVehicle();

        LocalDateTime start = LocalDateTime.of(
                2026, 7, 14, 10, 0
        );

        LocalDateTime expectedReturn = start.plusDays(1);

        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.create(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt(),
                anyDouble(),
                anyDouble(),
                anyString(),
                anyString()
        )).thenReturn(rental);

        service.recordSuccessfulRental(
                customer,
                vehicle,
                1,
                "INV-1",
                5.0,
                5.0,
                "CASH",
                995.0,
                start,
                expectedReturn
        );

        verify(loyaltyRepository).add(
                "hamada@email.com",
                1,
                "EARNED",
                "BMW X5 rental completed",
                "RNT-1"
        );
    }

    @Test
    public void testRecordSuccessfulRentalAutomaticDates() {
        Customer customer = createCustomer();
        Vehicle vehicle = createVehicle();

        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.create(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt(),
                anyDouble(),
                anyDouble(),
                anyString(),
                anyString()
        )).thenReturn(rental);

        service.recordSuccessfulRental(
                customer,
                vehicle,
                3,
                "INV-1",
                750.0,
                750.0,
                "CARD",
                250.0
        );

        ArgumentCaptor<LocalDateTime> startCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        ArgumentCaptor<LocalDateTime> expectedCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(rentalRepository).create(
                eq("hamada@email.com"),
                eq("Hamada Ahmad"),
                eq("V001"),
                eq("BMW X5"),
                startCaptor.capture(),
                expectedCaptor.capture(),
                eq(3),
                eq(250.0),
                eq(750.0),
                eq("INV-1"),
                eq("PAID")
        );

        assertEquals(
                3,
                Duration.between(
                        startCaptor.getValue(),
                        expectedCaptor.getValue()
                ).toDays()
        );
    }

    @Test
    public void testRecordSuccessfulRentalNullCustomer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        null,
                        createVehicle(),
                        2,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(2)
                )
        );

        verifyNoInteractions(rentalRepository);
    }

    @Test
    public void testRecordSuccessfulRentalNullVehicle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        createCustomer(),
                        null,
                        2,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(2)
                )
        );

        verifyNoInteractions(rentalRepository);
    }

    @Test
    public void testRecordSuccessfulRentalZeroDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        createCustomer(),
                        createVehicle(),
                        0,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(2)
                )
        );

        verifyNoInteractions(rentalRepository);
    }

    @Test
    public void testRecordSuccessfulRentalNegativeDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        createCustomer(),
                        createVehicle(),
                        -1,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(2)
                )
        );
    }

    @Test
    public void testRecordSuccessfulRentalNullStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        createCustomer(),
                        createVehicle(),
                        2,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        null,
                        LocalDateTime.now().plusDays(2)
                )
        );
    }

    @Test
    public void testRecordSuccessfulRentalNullExpectedReturn() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        createCustomer(),
                        createVehicle(),
                        2,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        LocalDateTime.now(),
                        null
                )
        );
    }

    @Test
    public void testRecordSuccessfulRentalExpectedReturnEqualsStart() {
        LocalDateTime start = LocalDateTime.of(
                2026, 7, 14, 10, 0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        createCustomer(),
                        createVehicle(),
                        2,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        start,
                        start
                )
        );
    }

    @Test
    public void testRecordSuccessfulRentalExpectedReturnBeforeStart() {
        LocalDateTime start = LocalDateTime.of(
                2026, 7, 14, 10, 0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordSuccessfulRental(
                        createCustomer(),
                        createVehicle(),
                        2,
                        "INV-1",
                        500.0,
                        550.0,
                        "CARD",
                        1000.0,
                        start,
                        start.minusHours(1)
                )
        );
    }

    @Test
    public void testMarkReturnDueRentalNotFound() {
        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.empty());

        assertFalse(service.markReturnDue("RNT-1"));

        verify(rentalRepository, never()).updateTimingStatus(
                anyString(),
                anyString(),
                anyDouble()
        );
    }

    @Test
    public void testMarkReturnDueAlreadyReturnDue() {
        RentalRecord rental = createRental(
                "  return_due  ",
                0.0
        );

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        assertTrue(service.markReturnDue("RNT-1"));

        verify(rentalRepository, never()).updateTimingStatus(
                anyString(),
                anyString(),
                anyDouble()
        );
    }

    @Test
    public void testMarkReturnDueNonActiveStatus() {
        RentalRecord rental = createRental(
                "RETURNED",
                0.0
        );

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        assertFalse(service.markReturnDue("RNT-1"));
    }

    @Test
    public void testMarkReturnDueNullStatus() {
        RentalRecord rental = createRental(
                null,
                0.0
        );

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        assertFalse(service.markReturnDue("RNT-1"));
    }

    @Test
    public void testMarkReturnDueUpdateFails() {
        RentalRecord rental = createRental(
                " active ",
                0.0
        );

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.updateTimingStatus(
                "RNT-1",
                "RETURN_DUE",
                0.0
        )).thenReturn(false);

        assertFalse(service.markReturnDue("RNT-1"));

        verifyNoInteractions(notificationRepository);
    }

    @Test
    public void testMarkReturnDueSuccess() {
        RentalRecord rental = createRental(
                " active ",
                0.0
        );

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.updateTimingStatus(
                "RNT-1",
                "RETURN_DUE",
                0.0
        )).thenReturn(true);

        assertTrue(service.markReturnDue("RNT-1"));

        verify(notificationRepository).create(
                "hamada@email.com",
                "RETURN_DUE",
                "Vehicle return is due",
                "Your BMW X5 rental has ended. "
                        + "You now have 30 minutes of grace time to return it."
        );
    }

    @Test
    public void testRecordLateFeeChargeNullRental() {
        assertFalse(service.recordLateFeeCharge(
                createCustomer(),
                null,
                20.0,
                20.0,
                500.0
        ));

        verifyNoInteractions(rentalRepository);
    }

    @Test
    public void testRecordLateFeeChargeRentalNotFound() {
        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.empty());

        assertFalse(service.recordLateFeeCharge(
                createCustomer(),
                rental,
                20.0,
                20.0,
                500.0
        ));
    }

    @Test
    public void testRecordLateFeeChargeUpdateFails() {
        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.updateTimingStatus(
                "RNT-1",
                "OVERDUE",
                20.0
        )).thenReturn(false);

        assertFalse(service.recordLateFeeCharge(
                createCustomer(),
                rental,
                20.0,
                20.0,
                500.0
        ));

        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    public void testRecordLateFeeChargeSuccessWithCustomer() {
        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.updateTimingStatus(
                "RNT-1",
                "OVERDUE",
                20.0
        )).thenReturn(true);

        boolean result = service.recordLateFeeCharge(
                createCustomer(),
                rental,
                20.0,
                15.5,
                484.5
        );

        assertTrue(result);

        verify(notificationRepository).create(
                "hamada@email.com",
                "RENTAL_OVERDUE",
                "Rental overdue",
                "Your BMW X5 is overdue. "
                        + "A late fee is now being calculated automatically."
        );

        verify(walletTransactionRepository).add(
                "hamada@email.com",
                "LATE_FEE",
                -15.5,
                484.5,
                "INV-1"
        );

        verify(notificationRepository).create(
                "hamada@email.com",
                "LATE_FEE_CHARGED",
                "Late fee charged",
                "$15.50 was deducted from your wallet "
                        + "for late return of BMW X5."
        );
    }

    @Test
    public void testRecordLateFeeChargeNullCustomerUsesRentalEmail() {
        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.updateTimingStatus(
                "RNT-1",
                "OVERDUE",
                0.0
        )).thenReturn(true);

        boolean result = service.recordLateFeeCharge(
                null,
                rental,
                -50.0,
                10.0,
                490.0
        );

        assertTrue(result);

        verify(rentalRepository).updateTimingStatus(
                "RNT-1",
                "OVERDUE",
                0.0
        );

        verify(walletTransactionRepository).add(
                "hamada@email.com",
                "LATE_FEE",
                -10.0,
                490.0,
                "INV-1"
        );
    }

    @Test
    public void testRecordLateFeeChargeAlreadyOverdueSameFee() {
        RentalRecord rental = createRental(
                "  overdue  ",
                20.0
        );

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        boolean result = service.recordLateFeeCharge(
                createCustomer(),
                rental,
                20.0,
                0.001,
                500.0
        );

        assertTrue(result);

        verify(rentalRepository, never()).updateTimingStatus(
                anyString(),
                anyString(),
                anyDouble()
        );

        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    public void testRecordLateFeeChargeAlreadyOverdueFeeChanged() {
        RentalRecord rental = createRental(
                "OVERDUE",
                10.0
        );

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.updateTimingStatus(
                "RNT-1",
                "OVERDUE",
                20.0
        )).thenReturn(true);

        assertTrue(service.recordLateFeeCharge(
                createCustomer(),
                rental,
                20.0,
                0.0,
                500.0
        ));

        verify(rentalRepository).updateTimingStatus(
                "RNT-1",
                "OVERDUE",
                20.0
        );

        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(walletTransactionRepository);
    }

    @Test
    public void testMarkRentalReturnedRentalNotFound() {
        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.empty());

        assertFalse(service.markRentalReturned(
                createCustomer(),
                "RNT-1",
                20.0
        ));

        verify(rentalRepository, never()).markReturned(
                anyString(),
                any(LocalDateTime.class),
                anyDouble()
        );
    }

    @Test
    public void testMarkRentalReturnedUpdateFails() {
        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.markReturned(
                eq("RNT-1"),
                any(LocalDateTime.class),
                eq(20.0)
        )).thenReturn(false);

        assertFalse(service.markRentalReturned(
                createCustomer(),
                "RNT-1",
                20.0
        ));

        verifyNoInteractions(notificationRepository);
    }

    @Test
    public void testMarkRentalReturnedSuccessWithCustomer() {
        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.markReturned(
                eq("RNT-1"),
                any(LocalDateTime.class),
                eq(20.0)
        )).thenReturn(true);

        assertTrue(service.markRentalReturned(
                createCustomer(),
                "RNT-1",
                20.0
        ));

        verify(notificationRepository).create(
                "hamada@email.com",
                "VEHICLE_RETURNED",
                "Vehicle returned",
                "Your BMW X5 return has been completed successfully."
        );
    }

    @Test
    public void testMarkRentalReturnedNullCustomerAndNegativeLateFee() {
        RentalRecord rental = createRental("ACTIVE", 0.0);

        when(rentalRepository.findById("RNT-1"))
                .thenReturn(Optional.of(rental));

        when(rentalRepository.markReturned(
                eq("RNT-1"),
                any(LocalDateTime.class),
                eq(0.0)
        )).thenReturn(true);

        assertTrue(service.markRentalReturned(
                null,
                "RNT-1",
                -10.0
        ));

        verify(rentalRepository).markReturned(
                eq("RNT-1"),
                any(LocalDateTime.class),
                eq(0.0)
        );

        verify(notificationRepository).create(
                "hamada@email.com",
                "VEHICLE_RETURNED",
                "Vehicle returned",
                "Your BMW X5 return has been completed successfully."
        );
    }
}