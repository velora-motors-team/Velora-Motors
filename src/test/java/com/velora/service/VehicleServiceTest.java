package com.velora.service;

import com.velora.observer.VehicleAvailabilitySubject;
import com.velora.repository.VehicleRepository;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VehicleServiceTest {

    private Vehicle createVehicle(
            String id,
            String brand,
            String model,
            VehicleStatus status
    ) {
        return Vehicle.create(
                id,
                brand,
                model,
                VehicleType.CAR,
                status,
                200.0
        );
    }

    @Test
    public void testDefaultConstructor() {
        try (
                MockedConstruction<VehicleRepository> repositoryMock =
                        mockConstruction(VehicleRepository.class);
                MockedConstruction<VehicleAvailabilitySubject> subjectMock =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService();

            assertNotNull(service);
            assertEquals(1, repositoryMock.constructed().size());
            assertEquals(1, subjectMock.constructed().size());
        }
    }

    @Test
    public void testGetAllVehicles() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle vehicle1 = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.AVAILABLE
        );

        Vehicle vehicle2 = createVehicle(
                "V002",
                "Toyota",
                "Corolla",
                VehicleStatus.RENTED
        );

        List<Vehicle> vehicles = List.of(vehicle1, vehicle2);

        when(repository.findAll()).thenReturn(vehicles);

        try (
                MockedConstruction<VehicleAvailabilitySubject> ignored =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            List<Vehicle> result = service.getAllVehicles();

            assertSame(vehicles, result);
            verify(repository).findAll();
        }
    }

    @Test
    public void testGetAvailableVehicles() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle available1 = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.AVAILABLE
        );

        Vehicle rented = createVehicle(
                "V002",
                "Toyota",
                "Corolla",
                VehicleStatus.RENTED
        );

        Vehicle maintenance = createVehicle(
                "V003",
                "Honda",
                "Civic",
                VehicleStatus.MAINTENANCE
        );

        Vehicle available2 = createVehicle(
                "V004",
                "Tesla",
                "Model 3",
                VehicleStatus.AVAILABLE
        );

        when(repository.findAll()).thenReturn(
                List.of(
                        available1,
                        rented,
                        maintenance,
                        available2
                )
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> ignored =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            List<Vehicle> result = service.getAvailableVehicles();

            assertEquals(2, result.size());
            assertTrue(result.contains(available1));
            assertTrue(result.contains(available2));
            assertFalse(result.contains(rented));
            assertFalse(result.contains(maintenance));
        }
    }

    @Test
    public void testCountAvailableVehicles() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle available1 = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.AVAILABLE
        );

        Vehicle rented = createVehicle(
                "V002",
                "Toyota",
                "Corolla",
                VehicleStatus.RENTED
        );

        Vehicle available2 = createVehicle(
                "V003",
                "Tesla",
                "Model 3",
                VehicleStatus.AVAILABLE
        );

        when(repository.findAll()).thenReturn(
                List.of(available1, rented, available2)
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> ignored =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            int result = service.countAvailableVehicles();

            assertEquals(2, result);
        }
    }

    @Test
    public void testSaveVehicles() {
        VehicleRepository repository = mock(VehicleRepository.class);

        try (
                MockedConstruction<VehicleAvailabilitySubject> ignored =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            service.saveVehicles();

            verify(repository).saveAll();
        }
    }

    @Test
    public void testChangeStatusNullVehicle() {
        VehicleRepository repository = mock(VehicleRepository.class);

        try (
                MockedConstruction<VehicleAvailabilitySubject> ignored =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            int result = service.changeStatus(
                    null,
                    VehicleStatus.AVAILABLE
            );

            assertEquals(0, result);
            verifyNoInteractions(repository);
        }
    }

    @Test
    public void testChangeStatusNullNewStatus() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.RENTED
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> ignored =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            int result = service.changeStatus(vehicle, null);

            assertEquals(0, result);
            assertEquals(VehicleStatus.RENTED, vehicle.getStatus());
            verifyNoInteractions(repository);
        }
    }

    @Test
    public void testChangeStatusRentedToAvailableNotifiesObservers() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.RENTED
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> mocked =
                        mockConstruction(
                                VehicleAvailabilitySubject.class,
                                (mock, context) -> when(
                                        mock.notifyObservers(vehicle)
                                ).thenReturn(3)
                        )
        ) {
            VehicleService service = new VehicleService(repository);

            int result = service.changeStatus(
                    vehicle,
                    VehicleStatus.AVAILABLE
            );

            VehicleAvailabilitySubject subject =
                    mocked.constructed().get(0);

            assertEquals(3, result);
            assertEquals(
                    VehicleStatus.AVAILABLE,
                    vehicle.getStatus()
            );

            verify(repository).saveAll();
            verify(subject).notifyObservers(vehicle);
        }
    }

    @Test
    public void testChangeStatusAvailableToAvailableDoesNotNotify() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.AVAILABLE
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> mocked =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            int result = service.changeStatus(
                    vehicle,
                    VehicleStatus.AVAILABLE
            );

            VehicleAvailabilitySubject subject =
                    mocked.constructed().get(0);

            assertEquals(0, result);
            assertEquals(
                    VehicleStatus.AVAILABLE,
                    vehicle.getStatus()
            );

            verify(repository).saveAll();
            verify(subject, never()).notifyObservers(any());
        }
    }

    @Test
    public void testChangeStatusRentedToMaintenanceDoesNotNotify() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.RENTED
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> mocked =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            int result = service.changeStatus(
                    vehicle,
                    VehicleStatus.MAINTENANCE
            );

            VehicleAvailabilitySubject subject =
                    mocked.constructed().get(0);

            assertEquals(0, result);
            assertEquals(
                    VehicleStatus.MAINTENANCE,
                    vehicle.getStatus()
            );

            verify(repository).saveAll();
            verify(subject, never()).notifyObservers(any());
        }
    }

    @Test
    public void testNotifyWaitlistsForAvailableVehicles() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle available1 = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.AVAILABLE
        );

        Vehicle rented = createVehicle(
                "V002",
                "Toyota",
                "Corolla",
                VehicleStatus.RENTED
        );

        Vehicle available2 = createVehicle(
                "V003",
                "Tesla",
                "Model 3",
                VehicleStatus.AVAILABLE
        );

        when(repository.findAll()).thenReturn(
                List.of(available1, rented, available2)
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> mocked =
                        mockConstruction(
                                VehicleAvailabilitySubject.class,
                                (mock, context) -> {
                                    when(
                                            mock.notifyObservers(available1)
                                    ).thenReturn(2);

                                    when(
                                            mock.notifyObservers(available2)
                                    ).thenReturn(4);
                                }
                        )
        ) {
            VehicleService service = new VehicleService(repository);

            int result =
                    service.notifyWaitlistsForAvailableVehicles();

            VehicleAvailabilitySubject subject =
                    mocked.constructed().get(0);

            assertEquals(6, result);

            verify(subject).notifyObservers(available1);
            verify(subject).notifyObservers(available2);
            verify(subject, never()).notifyObservers(rented);
        }
    }

    @Test
    public void testNotifyWaitlistsNoAvailableVehicles() {
        VehicleRepository repository = mock(VehicleRepository.class);

        Vehicle rented = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleStatus.RENTED
        );

        Vehicle maintenance = createVehicle(
                "V002",
                "Toyota",
                "Corolla",
                VehicleStatus.MAINTENANCE
        );

        when(repository.findAll()).thenReturn(
                List.of(rented, maintenance)
        );

        try (
                MockedConstruction<VehicleAvailabilitySubject> mocked =
                        mockConstruction(VehicleAvailabilitySubject.class)
        ) {
            VehicleService service = new VehicleService(repository);

            int result =
                    service.notifyWaitlistsForAvailableVehicles();

            VehicleAvailabilitySubject subject =
                    mocked.constructed().get(0);

            assertEquals(0, result);
            verifyNoInteractions(subject);
        }
    }
}