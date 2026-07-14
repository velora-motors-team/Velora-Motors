package com.velora.strategy;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LateFeeContextTest {

    @Test
    public void testConstructorWithStrategy() {

        LateFeeStrategy strategy = new StandardLateFeeStrategy();

        LateFeeContext context = new LateFeeContext(strategy);

        assertSame(strategy, context.getStrategy());
    }


    @Test
    public void testSetStrategy() {

        LateFeeContext context = new LateFeeContext();

        LateFeeStrategy strategy = new TruckLateFeeStrategy();

        context.setStrategy(strategy);

        assertSame(strategy, context.getStrategy());
    }


    @Test
    public void testChangeStrategy() {

        LateFeeContext context =
                new LateFeeContext(new StandardLateFeeStrategy());

        context.setStrategy(new ElectricLateFeeStrategy());

        assertTrue(
                context.getStrategy() instanceof ElectricLateFeeStrategy
        );
    }


    @Test
    public void testNullStrategy() {

        LateFeeContext context = new LateFeeContext();

        assertThrows(
                IllegalArgumentException.class,
                () -> context.setStrategy(null)
        );
    }


    @Test
    public void testCalculateWithoutStrategy() {

        LateFeeContext context = new LateFeeContext();

        Vehicle vehicle = new Vehicle(
                "V001",
                "Toyota",
                "Corolla",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                200.0
        );

        Duration overdue = Duration.ofHours(1);

        assertThrows(
                IllegalStateException.class,
                () -> context.calculateLateFee(vehicle, overdue)
        );
    }


    @Test
    public void testCalculateLateFee() {

        LateFeeContext context =
                new LateFeeContext(new StandardLateFeeStrategy());

        Vehicle vehicle = new Vehicle(
                "V001",
                "Toyota",
                "Corolla",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                200.0
        );

        Duration overdue = Duration.ofHours(1);

        double result = context.calculateLateFee(vehicle, overdue);

        assertEquals(10.0, result, 0.0001);
    }
}