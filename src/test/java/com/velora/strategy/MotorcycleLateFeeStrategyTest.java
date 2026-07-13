package com.velora.strategy;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MotorcycleLateFeeStrategyTest {

    @Test
    public void testTwoHoursLateFee() {

        int price = 200;
        double latePrice = 0.03;
        int late = 2;

        double result = late * latePrice * price;

        MotorcycleLateFeeStrategy obj = new MotorcycleLateFeeStrategy();

        Vehicle vehicle = new Vehicle(
                "V001",
                "Honda",
                "CBR",
                VehicleType.MOTORCYCLE,
                VehicleStatus.AVAILABLE,
                price
        );

        Duration overdue = Duration.ofHours(2);

        double res = obj.calculateLateFee(vehicle, overdue);

        assertEquals(result, res, 0.0001);
    }
}