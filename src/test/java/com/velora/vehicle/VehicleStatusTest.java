package com.velora.vehicle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleStatusTest {

    @Test
    public void testAvailableValue() {
        assertEquals(VehicleStatus.AVAILABLE, VehicleStatus.valueOf("AVAILABLE"));
    }

    @Test
    public void testRentedValue() {
        assertEquals(VehicleStatus.RENTED, VehicleStatus.valueOf("RENTED"));
    }

    @Test
    public void testMaintenanceValue() {
        assertEquals(VehicleStatus.MAINTENANCE, VehicleStatus.valueOf("MAINTENANCE"));
    }

    @Test
    public void testAllValues() {
        VehicleStatus[] values = VehicleStatus.values();

        assertEquals(3, values.length);
        assertArrayEquals(
                new VehicleStatus[]{
                        VehicleStatus.AVAILABLE,
                        VehicleStatus.RENTED,
                        VehicleStatus.MAINTENANCE
                },
                values
        );
    }
}