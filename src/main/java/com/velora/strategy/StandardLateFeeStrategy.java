package com.velora.strategy;

import com.velora.vehicle.Vehicle;

import java.time.Duration;

public final class StandardLateFeeStrategy implements LateFeeStrategy {

    private static final double HOURLY_RATE_PERCENT = 0.05;

    @Override
    public double calculateLateFee(Vehicle vehicle, Duration overdueAfterGrace) {
        return LateFeeStrategySupport.calculateStartedHourFee(
                vehicle,
                overdueAfterGrace,
                HOURLY_RATE_PERCENT
        );
    }
}
