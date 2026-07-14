package com.velora.observer;

import com.velora.repository.NotificationRepository;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerAvailabilityObserverTest {

    @Test
    public void testVehicleAvailableCreatesTwoNotifications() {
        NotificationRepository repository = mock(NotificationRepository.class);

        CustomerAvailabilityObserver observer =
                new CustomerAvailabilityObserver(
                        "Hamada",
                        "HAMADA@EMAIL.COM",
                        repository
                );

        Vehicle vehicle = new Vehicle(
                "V001",
                "BMW",
                "X5",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                250.0
        );

        observer.onVehicleAvailable(vehicle);

        verify(repository).create(
                "hamada@email.com",
                "VEHICLE_AVAILABLE",
                "Vehicle Available",
                "BMW X5 is now available for rent."
        );

        verify(repository).create(
                "hamada@email.com",
                "ADMIN_EMAIL",
                "Your requested vehicle is ready",
                "From: Velora Motors Administration\n\nHello Hamada,\n"
                        + "BMW X5 is now available and ready for rent."
        );

        verifyNoMoreInteractions(repository);
    }

    @Test
    public void testBlankCustomerNameUsesDefaultName() {
        NotificationRepository repository = mock(NotificationRepository.class);

        CustomerAvailabilityObserver observer =
                new CustomerAvailabilityObserver(
                        "   ",
                        "customer@email.com",
                        repository
                );

        Vehicle vehicle = new Vehicle(
                "V002",
                "Tesla",
                "Model 3",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                300.0
        );

        observer.onVehicleAvailable(vehicle);

        verify(repository).create(
                "customer@email.com",
                "ADMIN_EMAIL",
                "Your requested vehicle is ready",
                "From: Velora Motors Administration\n\nHello Customer,\n"
                        + "Tesla Model 3 is now available and ready for rent."
        );
    }

    @Test
    public void testNullCustomerNameUsesDefaultName() {
        NotificationRepository repository = mock(NotificationRepository.class);

        CustomerAvailabilityObserver observer =
                new CustomerAvailabilityObserver(
                        null,
                        "customer@email.com",
                        repository
                );

        Vehicle vehicle = new Vehicle(
                "V003",
                "Toyota",
                "Corolla",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                200.0
        );

        observer.onVehicleAvailable(vehicle);

        verify(repository).create(
                "customer@email.com",
                "ADMIN_EMAIL",
                "Your requested vehicle is ready",
                "From: Velora Motors Administration\n\nHello Customer,\n"
                        + "Toyota Corolla is now available and ready for rent."
        );
    }

    @Test
    public void testEmailIsTrimmedAndLowercase() {
        NotificationRepository repository = mock(NotificationRepository.class);

        CustomerAvailabilityObserver observer =
                new CustomerAvailabilityObserver(
                        "Hamada",
                        "   HAMADA@EMAIL.COM   ",
                        repository
                );

        Vehicle vehicle = new Vehicle(
                "V004",
                "BMW",
                "i4",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                320.0
        );

        observer.onVehicleAvailable(vehicle);

        verify(repository, times(2)).create(
                eq("hamada@email.com"),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    public void testNullEmailBecomesEmptyString() {
        NotificationRepository repository = mock(NotificationRepository.class);

        CustomerAvailabilityObserver observer =
                new CustomerAvailabilityObserver(
                        "Hamada",
                        null,
                        repository
                );

        Vehicle vehicle = new Vehicle(
                "V005",
                "Honda",
                "CBR",
                VehicleType.MOTORCYCLE,
                VehicleStatus.AVAILABLE,
                150.0
        );

        observer.onVehicleAvailable(vehicle);

        verify(repository, times(2)).create(
                eq(""),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    public void testNullRepositoryThrowsException() {
        assertThrows(
                NullPointerException.class,
                () -> new CustomerAvailabilityObserver(
                        "Hamada",
                        "hamada@email.com",
                        null
                )
        );
    }
}