/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.velora.strategy;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author 97259
 */
public class LateFeeStrategyFactoryTest {
    
    public LateFeeStrategyFactoryTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    
   @Test
public void testCarStrategy() {

    Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof StandardLateFeeStrategy);//تأكد إن المتغير res يحمل object من نوع StandardLateFeeStrategy.
}
 @Test
public void testCarStrategy_SUV() {

    Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.SUV,
            VehicleStatus.AVAILABLE,
            200.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof StandardLateFeeStrategy);//تأكد إن المتغير res يحمل object من نوع StandardLateFeeStrategy.
}

@Test
public void testHybridCarStrategy() {

    Vehicle vehicle = Vehicle.create(
            "V004",
            "Toyota",
            "Prius",
            VehicleType.HYBRID_CAR,
            VehicleStatus.AVAILABLE,
            220.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof ElectricLateFeeStrategy);
}
@Test
public void testElectricVehicleStrategy() {

    Vehicle vehicle = Vehicle.create(
            "V003",
            "BMW",
            "i4",
            VehicleType.ELECTRIC_VEHICLE,
            VehicleStatus.AVAILABLE,
            300.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof ElectricLateFeeStrategy);
}

@Test
public void testMotorcycleStrategy() {

    Vehicle vehicle = Vehicle.create(
            "V005",
            "Honda",
            "CBR",
            VehicleType.MOTORCYCLE,
            VehicleStatus.AVAILABLE,
            150.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof MotorcycleLateFeeStrategy);
}

@Test
public void testElectricBikeStrategy() {

    Vehicle vehicle = Vehicle.create(
            "V006",
            "Xiaomi",
            "E-Bike",
            VehicleType.ELECTRIC_BIKE,
            VehicleStatus.AVAILABLE,
            80.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof MotorcycleLateFeeStrategy);
}

@Test
public void testTruckStrategy() {

    Vehicle vehicle = Vehicle.create(
            "V007",
            "Volvo",
            "FH",
            VehicleType.TRUCK,
            VehicleStatus.AVAILABLE,
            400.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof TruckLateFeeStrategy);
}

@Test
public void testNullVehicleStrategy() {

    Vehicle vehicle = null;

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof StandardLateFeeStrategy);
}

@Test
public void testNullVehicleTypeStrategy() {

    Vehicle vehicle = Vehicle.create(
            "V008",
            "Unknown",
            "Unknown",
            null,
            VehicleStatus.AVAILABLE,
            100.0
    );

    LateFeeStrategy res = LateFeeStrategyFactory.forVehicle(vehicle);

    assertTrue(res instanceof StandardLateFeeStrategy);
}

}

