package com.velora.strategy;

import com.velora.vehicle.Vehicle;

import java.time.Duration;

public interface LateFeeStrategy {

    double calculateLateFee(Vehicle vehicle, Duration overdueAfterGrace);
}
