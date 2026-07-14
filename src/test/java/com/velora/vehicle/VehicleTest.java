package com.velora.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        Vehicle vehicle = new Vehicle();

        vehicle.setId("V001");
        vehicle.setBrand("BMW");
        vehicle.setModel("X5");
        vehicle.setType(VehicleType.SUV);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setDailyPrice(250.0);
        vehicle.setBatteryLevel(80);

        assertEquals("V001", vehicle.getId());
        assertEquals("BMW", vehicle.getBrand());
        assertEquals("X5", vehicle.getModel());
        assertEquals(VehicleType.SUV, vehicle.getType());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertEquals(250.0, vehicle.getDailyPrice(), 0.0001);
        assertEquals(80, vehicle.getBatteryLevel());
    }

    @Test
    public void testConstructorWithoutBattery() {
        Vehicle vehicle = new Vehicle(
                "V002",
                "Toyota",
                "Corolla",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                200.0
        );

        assertEquals("V002", vehicle.getId());
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals("Corolla", vehicle.getModel());
        assertEquals(VehicleType.CAR, vehicle.getType());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertEquals(200.0, vehicle.getDailyPrice(), 0.0001);
        assertNull(vehicle.getBatteryLevel());
    }

    @Test
    public void testConstructorWithBattery() {
        Vehicle vehicle = new Vehicle(
                "V003",
                "Tesla",
                "Model 3",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                300.0,
                90
        );

        assertEquals("V003", vehicle.getId());
        assertEquals("Tesla", vehicle.getBrand());
        assertEquals("Model 3", vehicle.getModel());
        assertEquals(VehicleType.ELECTRIC_VEHICLE, vehicle.getType());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertEquals(300.0, vehicle.getDailyPrice(), 0.0001);
        assertEquals(90, vehicle.getBatteryLevel());
    }

    @Test
    public void testAvailableVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        assertTrue(vehicle.isAvailable());
    }

    @Test
    public void testUnavailableVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setStatus(VehicleStatus.RENTED);

        assertFalse(vehicle.isAvailable());
    }

    @Test
    public void testNullStatusIsNotAvailable() {
        Vehicle vehicle = new Vehicle();
        vehicle.setStatus(null);

        assertFalse(vehicle.isAvailable());
    }

    @Test
    public void testElectricVehicleHasBattery() {
        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.ELECTRIC_VEHICLE);

        assertTrue(vehicle.hasBattery());
    }

    @Test
    public void testElectricBikeHasBattery() {
        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.ELECTRIC_BIKE);

        assertTrue(vehicle.hasBattery());
    }

    @Test
    public void testCarDoesNotHaveBattery() {
        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.CAR);

        assertFalse(vehicle.hasBattery());
    }

    @Test
    public void testNullTypeDoesNotHaveBattery() {
        Vehicle vehicle = new Vehicle();
        vehicle.setType(null);

        assertFalse(vehicle.hasBattery());
    }

    @Test
    public void testDisplayName() {
        Vehicle vehicle = new Vehicle(
                "V004",
                "BMW",
                "X5",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                250.0
        );

        assertEquals("BMW X5", vehicle.getDisplayName());
    }
}