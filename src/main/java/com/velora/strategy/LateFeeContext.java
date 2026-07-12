package com.velora.strategy;

import com.velora.vehicle.Vehicle;

import java.time.Duration;

/**
 * Context class for the late-fee Strategy Pattern.
 *
 * The context stores the currently selected strategy and delegates
 * the late-fee calculation to that strategy.
 */
public final class LateFeeContext {

    private LateFeeStrategy strategy;

    public LateFeeContext() {
    }

    public LateFeeContext(LateFeeStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(LateFeeStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Late fee strategy is required.");
        }
        this.strategy = strategy;
    }

    public LateFeeStrategy getStrategy() {
        return strategy;
    }

    public double calculateLateFee(Vehicle vehicle, Duration overdueAfterGrace) {
        if (strategy == null) {
            throw new IllegalStateException("No late fee strategy has been selected.");
        }
        return strategy.calculateLateFee(vehicle, overdueAfterGrace);
    }
}
