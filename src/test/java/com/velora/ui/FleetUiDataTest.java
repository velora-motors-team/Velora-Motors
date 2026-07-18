package com.velora.ui;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FleetUiDataTest {

    private Vehicle createVehicle(
            String id,
            String brand,
            String model,
            VehicleType type
    ) {
        return Vehicle.create(
                id,
                brand,
                model,
                type,
                VehicleStatus.AVAILABLE,
                250.0
        );
    }

    @Test
    public void testDisplayName() {
        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleType.SUV
        );

        assertEquals("BMW X5", FleetUiData.displayName(vehicle));
    }

    @Test
    public void testImagePath() {
        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleType.SUV
        );

        assertEquals(
                "/images/vehicles/V001.jpg",
                FleetUiData.imagePath(vehicle)
        );
    }

    @Test
    public void testColorIsDeterministicAndSupported() {
        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleType.SUV
        );

        String first = FleetUiData.color(vehicle);
        String second = FleetUiData.color(vehicle);

        assertEquals(first, second);

        assertTrue(
                first.equals("Black Sapphire")
                        || first.equals("Frozen Grey")
                        || first.equals("Isle of Man Green")
                        || first.equals("Alpine White")
                        || first.equals("Portimao Blue")
                        || first.equals("Toronto Red")
                        || first.equals("Thundernight Purple")
        );
    }

    @Test
    public void testVin() {
        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5 Competition",
                VehicleType.SUV
        );

        String vin = FleetUiData.vin(vehicle);

        assertEquals(17, vin.length());
        assertTrue(vin.startsWith("WBAVM"));
        assertTrue(vin.matches("[A-Z0-9]+"));
    }

    @Test
    public void testPlate() {
        Vehicle vehicle = createVehicle(
                "V001",
                "BMW",
                "X5",
                VehicleType.SUV
        );

        String plate = FleetUiData.plate(vehicle);

        assertTrue(plate.startsWith("V001-"));

        int number = Integer.parseInt(plate.substring("V001-".length()));
        assertTrue(number >= 0 && number < 9000);
    }

    @Test
    public void testElectricVehicleServiceType() {
        Vehicle vehicle = createVehicle(
                "EV001",
                "Tesla",
                "Model 3",
                VehicleType.ELECTRIC_VEHICLE
        );

        assertEquals("Battery Check", FleetUiData.serviceType(vehicle, 0));
        assertEquals("Full Inspection", FleetUiData.serviceType(vehicle, 1));
    }

    @Test
    public void testElectricBikeServiceType() {
        Vehicle vehicle = createVehicle(
                "EB001",
                "Xiaomi",
                "E-Bike",
                VehicleType.ELECTRIC_BIKE
        );

        assertEquals("Battery Check", FleetUiData.serviceType(vehicle, 2));
        assertEquals("Full Inspection", FleetUiData.serviceType(vehicle, 3));
    }

    @Test
    public void testNormalVehicleServiceTypes() {
        Vehicle vehicle = createVehicle(
                "V002",
                "Toyota",
                "Corolla",
                VehicleType.CAR
        );

        assertEquals("Oil Service", FleetUiData.serviceType(vehicle, 0));
        assertEquals("Brake Inspection", FleetUiData.serviceType(vehicle, 1));
        assertEquals("Tire Service", FleetUiData.serviceType(vehicle, 2));
        assertEquals("Full Inspection", FleetUiData.serviceType(vehicle, 3));
    }
}