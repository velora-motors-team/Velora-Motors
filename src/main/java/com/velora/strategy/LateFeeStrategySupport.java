package com.velora.strategy;

import com.velora.vehicle.Vehicle;

import java.time.Duration;

final class LateFeeStrategySupport {

    private LateFeeStrategySupport() {
    }

    static double calculateStartedHourFee(
            Vehicle vehicle,
            Duration overdueAfterGrace,
            double hourlyRatePercent
    ) {
        if (vehicle == null || overdueAfterGrace == null
                || overdueAfterGrace.isNegative() || overdueAfterGrace.isZero()) {
            return 0.0;
        }

        long seconds = Math.max(1L, overdueAfterGrace.getSeconds());
        long startedHours = (long) Math.ceil(seconds / 3600.0);

        double fee = vehicle.getDailyPrice() * hourlyRatePercent * startedHours;
        return Math.round(fee * 100.0) / 100.0;
    }
}
