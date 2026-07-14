package com.velora.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleTypeTest {

    @Test
    public void testAllVehicleTypes() {
        VehicleType[] values = VehicleType.values();

        assertEquals(7, values.length);

        assertArrayEquals(
                new VehicleType[]{
                        VehicleType.CAR,
                        VehicleType.SUV,
                        VehicleType.HYBRID_CAR,
                        VehicleType.ELECTRIC_VEHICLE,
                        VehicleType.ELECTRIC_BIKE,
                        VehicleType.MOTORCYCLE,
                        VehicleType.TRUCK
                },
                values
        );
    }

    @Test
    public void testCarValue() {
        assertEquals(VehicleType.CAR, VehicleType.valueOf("CAR"));
    }

    @Test
    public void testSuvValue() {
        assertEquals(VehicleType.SUV, VehicleType.valueOf("SUV"));
    }

    @Test
    public void testHybridCarValue() {
        assertEquals(VehicleType.HYBRID_CAR, VehicleType.valueOf("HYBRID_CAR"));
    }

    @Test
    public void testElectricVehicleValue() {
        assertEquals(
                VehicleType.ELECTRIC_VEHICLE,
                VehicleType.valueOf("ELECTRIC_VEHICLE")
        );
    }

    @Test
    public void testElectricBikeValue() {
        assertEquals(
                VehicleType.ELECTRIC_BIKE,
                VehicleType.valueOf("ELECTRIC_BIKE")
        );
    }

    @Test
    public void testMotorcycleValue() {
        assertEquals(
                VehicleType.MOTORCYCLE,
                VehicleType.valueOf("MOTORCYCLE")
        );
    }

    @Test
    public void testTruckValue() {
        assertEquals(VehicleType.TRUCK, VehicleType.valueOf("TRUCK"));
    }
}