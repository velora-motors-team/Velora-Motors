package com.velora.strategy;

import com.velora.vehicle.Vehicle;

import java.time.Duration;

public final class MotorcycleLateFeeStrategy implements LateFeeStrategy {

    private static final double HOURLY_RATE_PERCENT = 0.03;

    @Override
    public double calculateLateFee(Vehicle vehicle, Duration overdueAfterGrace) {
        return LateFeeStrategySupport.calculateStartedHourFee(
                vehicle,
                overdueAfterGrace,
                HOURLY_RATE_PERCENT
        );
    }
}
