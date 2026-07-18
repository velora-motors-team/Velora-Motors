package com.velora.service;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class RentalPricingServiceTest {

    private static RentalPricingService serviceAt(String instant) {
        return new RentalPricingService(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private static Vehicle vehicle(VehicleType type, double dailyPrice) {
        Integer battery = type == VehicleType.ELECTRIC_BIKE
                || type == VehicleType.ELECTRIC_VEHICLE ? 80 : null;
        return Vehicle.create("V-1", "BMW", "Test", type,
                VehicleStatus.AVAILABLE, dailyPrice, battery);
    }

    @Test
    public void motorcyclesReceiveThirtyPercentSummerDiscountBeforeTax() {
        var quote = serviceAt("2026-07-18T00:00:00Z")
                .quote(vehicle(VehicleType.MOTORCYCLE, 100.0), 2, false);

        assertEquals(200.0, quote.originalSubtotal(), 0.001);
        assertEquals(0.30, quote.promotionRate(), 0.001);
        assertEquals(60.0, quote.promotionDiscount(), 0.001);
        assertEquals(140.0, quote.taxableSubtotal(), 0.001);
        assertEquals(14.0, quote.tax(), 0.001);
        assertEquals(154.0, quote.total(), 0.001);
    }

    @Test
    public void electricBikesReceiveThirtyPercentAndTrucksReceiveTwentyPercent() {
        var service = serviceAt("2026-08-31T12:00:00Z");

        assertEquals(0.30, service.promotionRate(VehicleType.ELECTRIC_BIKE), 0.001);
        assertEquals(0.20, service.promotionRate(VehicleType.TRUCK), 0.001);
        assertEquals(0.0, service.promotionRate(VehicleType.CAR), 0.001);
    }

    @Test
    public void loyaltyRewardStacksAfterPromotionAndBeforeTax() {
        var quote = serviceAt("2026-07-18T00:00:00Z")
                .quote(vehicle(VehicleType.TRUCK, 500.0), 2, true);

        assertEquals(200.0, quote.promotionDiscount(), 0.001);
        assertEquals(80.0, quote.loyaltyDiscount(), 0.001);
        assertEquals(720.0, quote.taxableSubtotal(), 0.001);
        assertEquals(72.0, quote.tax(), 0.001);
        assertEquals(792.0, quote.total(), 0.001);
        assertEquals(280.0, quote.totalDiscount(), 0.001);
    }

    @Test
    public void promotionExpiresAfterAugustThirtyFirst() {
        var quote = serviceAt("2026-09-01T00:00:00Z")
                .quote(vehicle(VehicleType.MOTORCYCLE, 100.0), 1, false);

        assertFalse(quote.hasPromotion());
        assertEquals(110.0, quote.total(), 0.001);
    }

    @Test
    public void validatesVehicleAndRentalDuration() {
        var service = serviceAt("2026-07-18T00:00:00Z");
        Vehicle motorcycle = vehicle(VehicleType.MOTORCYCLE, 100.0);

        assertEquals("Vehicle is required.",
                assertThrows(IllegalArgumentException.class,
                        () -> service.quote(null, 1, false)).getMessage());
        for (int days : new int[]{-1, 0, 31}) {
            assertEquals("Rental days must be between 1 and 30.",
                    assertThrows(IllegalArgumentException.class,
                            () -> service.quote(motorcycle, days, false)).getMessage());
        }
        assertDoesNotThrow(() -> service.quote(motorcycle, 1, false));
        assertDoesNotThrow(() -> service.quote(motorcycle, 30, false));
    }
}
