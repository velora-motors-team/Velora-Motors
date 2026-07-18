package com.velora.vehicle;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class VehiclePolymorphismTest {

    @Test
    public void vehicleIsAbstract() {
        assertTrue(Modifier.isAbstract(Vehicle.class.getModifiers()));
    }

    @Test
    public void carCanBeRentedWithoutSpecialRequirements() {
        Vehicle car = new Car(
                "C-1", "BMW", "M3",
                VehicleStatus.AVAILABLE, 300.0
        );

        assertTrue(car.canBeRentedBy(18, false));
    }

    @Test
    public void truckRequiresSpecialLicense() {
        Vehicle truck = new Truck(
                "T-1", "Volvo", "FH",
                VehicleStatus.AVAILABLE, 500.0
        );

        assertFalse(truck.canBeRentedBy(30, false));
        assertTrue(truck.canBeRentedBy(30, true));
    }

    @Test
    public void motorcycleRequiresMinimumAge() {
        Vehicle motorcycle = new Motorcycle(
                "M-1", "Yamaha", "MT-07",
                VehicleStatus.AVAILABLE, 120.0
        );

        assertFalse(motorcycle.canBeRentedBy(
                Motorcycle.MINIMUM_DRIVER_AGE - 1, false
        ));
        assertTrue(motorcycle.canBeRentedBy(
                Motorcycle.MINIMUM_DRIVER_AGE, false
        ));
    }

    @Test
    public void electricVehicleRequiresEnoughBattery() {
        ElectricVehicle lowBattery = new ElectricVehicle(
                "E-1", "Tesla", "Model 3",
                VehicleStatus.AVAILABLE, 250.0, 29
        );
        ElectricVehicle enoughBattery = new ElectricVehicle(
                "E-2", "Tesla", "Model Y",
                VehicleStatus.AVAILABLE, 280.0, 30
        );
        ElectricVehicle unknownBattery = new ElectricVehicle(
                "E-3", "Tesla", "Model S",
                VehicleStatus.AVAILABLE, 350.0, null
        );

        assertFalse(lowBattery.hasEnoughBattery());
        assertFalse(lowBattery.canBeRentedBy(30, true));
        assertTrue(enoughBattery.hasEnoughBattery());
        assertTrue(enoughBattery.canBeRentedBy(18, false));
        assertFalse(unknownBattery.hasEnoughBattery());
    }

    @Test
    public void electricMotorcycleRequiresMinimumAgeAndEnoughBattery() {
        ElectricMotorcycle bike = new ElectricMotorcycle(
                "EB-1", "BMW", "CE 04",
                VehicleStatus.AVAILABLE, 120.0, 80
        );
        ElectricMotorcycle lowBattery = new ElectricMotorcycle(
                "EB-2", "BMW", "CE 04",
                VehicleStatus.AVAILABLE, 120.0, 29
        );

        assertFalse(bike.canBeRentedBy(20, false));
        assertTrue(bike.canBeRentedBy(21, false));
        assertFalse(lowBattery.hasEnoughBattery());
        assertFalse(lowBattery.canBeRentedBy(21, false));
    }

    @Test
    public void factoryCreatesMatchingConcreteTypes() {
        assertInstanceOf(Car.class, Vehicle.create(
                "C", "B", "M", VehicleType.CAR,
                VehicleStatus.AVAILABLE, 1.0
        ));
        assertInstanceOf(Truck.class, Vehicle.create(
                "T", "B", "M", VehicleType.TRUCK,
                VehicleStatus.AVAILABLE, 1.0
        ));
        assertInstanceOf(Motorcycle.class, Vehicle.create(
                "M", "B", "M", VehicleType.MOTORCYCLE,
                VehicleStatus.AVAILABLE, 1.0
        ));
        assertInstanceOf(ElectricVehicle.class, Vehicle.create(
                "E", "B", "M", VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE, 1.0, 80
        ));
    }

    @Test
    public void defaultSubtypeConstructorsSetTheirTypes() {
        assertEquals(VehicleType.CAR, new Car().getType());
        assertEquals(VehicleType.TRUCK, new Truck().getType());
        assertEquals(VehicleType.MOTORCYCLE, new Motorcycle().getType());
        assertEquals(
                VehicleType.ELECTRIC_VEHICLE,
                new ElectricVehicle().getType()
        );
        assertEquals(VehicleType.ELECTRIC_BIKE, new ElectricMotorcycle().getType());
    }

    @Test
    public void protectedVehicleConstructorWithoutBatteryIsUsableBySubtype() {
        Vehicle vehicle = new Vehicle(
                "C-2", "BMW", "M4", VehicleType.CAR,
                VehicleStatus.AVAILABLE, 320.0
        ) {
            @Override
            public boolean canBeRentedBy(
                    int driverAge,
                    boolean hasSpecialLicense
            ) {
                return driverAge >= 18;
            }
        };

        assertEquals("C-2", vehicle.getId());
        assertNull(vehicle.getBatteryLevel());
        assertTrue(vehicle.canBeRentedBy(18, false));
    }

    @Test
    public void factoryCoversEveryPersistedVehicleTypeAndNull() {
        assertInstanceOf(Car.class, VehicleFactory.create(
                "N", "B", "M", null,
                VehicleStatus.AVAILABLE, 1.0, null
        ));
        assertInstanceOf(Car.class, VehicleFactory.create(
                "S", "B", "M", VehicleType.SUV,
                VehicleStatus.AVAILABLE, 1.0, null
        ));
        assertInstanceOf(Car.class, VehicleFactory.create(
                "H", "B", "M", VehicleType.HYBRID_CAR,
                VehicleStatus.AVAILABLE, 1.0, null
        ));
        assertInstanceOf(ElectricMotorcycle.class, VehicleFactory.create(
                "B", "B", "M", VehicleType.ELECTRIC_BIKE,
                VehicleStatus.AVAILABLE, 1.0, 80
        ));
        assertInstanceOf(Car.class, VehicleFactory.create());
    }
}
