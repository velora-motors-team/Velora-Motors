package com.velora.observer;

import com.velora.authentication.Customer;
import com.velora.repository.NotificationRepository;
import com.velora.repository.VehicleWaitlistRepository;
import com.velora.repository.VehicleWaitlistRepository.WaitlistRecord;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class VehicleAvailabilitySubjectTest {

    private Vehicle createVehicle(String id, VehicleStatus status) {
        return Vehicle.create(
                id,
                "BMW",
                "X5",
                VehicleType.SUV,
                status,
                250.0
        );
    }

    private Customer createCustomer(String name, String email) {
        return new Customer(name, email, "0599999999");
    }

    @Test
    public void testDefaultConstructor() {
        try (MockedConstruction<VehicleWaitlistRepository> mocked =
                     mockConstruction(VehicleWaitlistRepository.class)) {

            VehicleAvailabilitySubject subject =
                    new VehicleAvailabilitySubject();

            assertNotNull(subject);
            assertEquals(1, mocked.constructed().size());
        }
    }

    @Test
    public void testNullRepositoryThrowsException() {
        assertThrows(
                NullPointerException.class,
                () -> new VehicleAvailabilitySubject(null)
        );
    }

    @Test
    public void testAddObserverSuccess() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        Customer customer =
                createCustomer("Hamada", "hamada@email.com");

        when(repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        )).thenReturn(true);

        boolean result = subject.addObserver(vehicle, customer);

        assertTrue(result);

        verify(repository).subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );
    }

    @Test
    public void testAddObserverNullCustomer() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        assertFalse(subject.addObserver(vehicle, null));

        verifyNoInteractions(repository);
    }

    @Test
    public void testAddObserverBlankEmail() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        Customer customer =
                createCustomer("Hamada", "   ");

        assertFalse(subject.addObserver(vehicle, customer));

        verifyNoInteractions(repository);
    }

    @Test
    public void testRemoveObserverSuccess() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        Customer customer =
                createCustomer("Hamada", "hamada@email.com");

        when(repository.unsubscribe(
                "V001",
                "hamada@email.com"
        )).thenReturn(true);

        boolean result = subject.removeObserver(vehicle, customer);

        assertTrue(result);

        verify(repository).unsubscribe(
                "V001",
                "hamada@email.com"
        );
    }

    @Test
    public void testRemoveObserverNullCustomer() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        assertFalse(subject.removeObserver(vehicle, null));

        verifyNoInteractions(repository);
    }

    @Test
    public void testRemoveObserverBlankEmail() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        Customer customer =
                createCustomer("Hamada", "   ");

        assertFalse(subject.removeObserver(vehicle, customer));

        verifyNoInteractions(repository);
    }

    @Test
    public void testHasObserverTrue() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        Customer customer =
                createCustomer("Hamada", "hamada@email.com");

        when(repository.isSubscribed(
                "V001",
                "hamada@email.com"
        )).thenReturn(true);

        assertTrue(subject.hasObserver(vehicle, customer));

        verify(repository).isSubscribed(
                "V001",
                "hamada@email.com"
        );
    }

    @Test
    public void testHasObserverFalseWhenRepositoryReturnsFalse() {
        VehicleWaitlistRepository repository = mock(VehicleWaitlistRepository.class);
        VehicleAvailabilitySubject subject = new VehicleAvailabilitySubject(repository);
        Vehicle vehicle = createVehicle("V001", VehicleStatus.RENTED);
        Customer customer = createCustomer("Hamada", "hamada@email.com");

        when(repository.isSubscribed("V001", "hamada@email.com"))
                .thenReturn(false);

        assertFalse(subject.hasObserver(vehicle, customer));
    }

    @Test
    public void testHasObserverNullVehicle() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Customer customer =
                createCustomer("Hamada", "hamada@email.com");

        assertFalse(subject.hasObserver(null, customer));

        verifyNoInteractions(repository);
    }

    @Test
    public void testHasObserverNullCustomer() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        assertFalse(subject.hasObserver(vehicle, null));

        verifyNoInteractions(repository);
    }

    @Test
    public void testNotifyObserversVehicleNotAvailable() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.RENTED);

        int result = subject.notifyObservers(vehicle);

        assertEquals(0, result);

        verifyNoInteractions(repository);
    }

    @Test
    public void testNotifyObserversNoSubscriptions() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.AVAILABLE);

        when(repository.findByVehicleId("V001"))
                .thenReturn(List.of());

        int result = subject.notifyObservers(vehicle);

        assertEquals(0, result);

        verify(repository).findByVehicleId("V001");
        verify(repository, never()).removeByVehicleId(anyString());
    }

    @Test
    public void testNotifyObserversWithSubscriptions() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("V001", VehicleStatus.AVAILABLE);

        List<WaitlistRecord> subscriptions = List.of(
                new WaitlistRecord(
                        "V001",
                        "Hamada",
                        "hamada@email.com",
                        "2026-07-14T10:00:00"
                ),
                new WaitlistRecord(
                        "V001",
                        "Ahmad",
                        "ahmad@email.com",
                        "2026-07-14T11:00:00"
                )
        );

        when(repository.findByVehicleId("V001"))
                .thenReturn(subscriptions);

        try (MockedConstruction<NotificationRepository> mocked =
                     mockConstruction(NotificationRepository.class)) {

            int result = subject.notifyObservers(vehicle);

            assertEquals(2, result);
            assertEquals(2, mocked.constructed().size());

            verify(repository).findByVehicleId("V001");
            verify(repository).removeByVehicleId("V001");

            NotificationRepository firstNotificationRepository =
                    mocked.constructed().get(0);

            verify(firstNotificationRepository).create(
                    "hamada@email.com",
                    "VEHICLE_AVAILABLE",
                    "Vehicle Available",
                    "BMW X5 is now available for rent."
            );

            verify(firstNotificationRepository).create(
                    eq("hamada@email.com"),
                    eq("ADMIN_EMAIL"),
                    eq("Your requested vehicle is ready"),
                    contains("Hello Hamada")
            );

            NotificationRepository secondNotificationRepository =
                    mocked.constructed().get(1);

            verify(secondNotificationRepository).create(
                    "ahmad@email.com",
                    "VEHICLE_AVAILABLE",
                    "Vehicle Available",
                    "BMW X5 is now available for rent."
            );

            verify(secondNotificationRepository).create(
                    eq("ahmad@email.com"),
                    eq("ADMIN_EMAIL"),
                    eq("Your requested vehicle is ready"),
                    contains("Hello Ahmad")
            );
        }
    }

    @Test
    public void testNullVehicleThrowsException() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        assertThrows(
                NullPointerException.class,
                () -> subject.addObserver(null, createCustomer(
                        "Hamada",
                        "hamada@email.com"
                ))
        );
    }

    @Test
    public void testVehicleWithNullIdThrowsException() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle(null, VehicleStatus.RENTED);

        assertThrows(
                IllegalArgumentException.class,
                () -> subject.addObserver(
                        vehicle,
                        createCustomer("Hamada", "hamada@email.com")
                )
        );
    }

    @Test
    public void testVehicleWithBlankIdThrowsException() {
        VehicleWaitlistRepository repository =
                mock(VehicleWaitlistRepository.class);

        VehicleAvailabilitySubject subject =
                new VehicleAvailabilitySubject(repository);

        Vehicle vehicle =
                createVehicle("   ", VehicleStatus.RENTED);

        assertThrows(
                IllegalArgumentException.class,
                () -> subject.removeObserver(
                        vehicle,
                        createCustomer("Hamada", "hamada@email.com")
                )
        );
    }
}
