/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.velora.strategy;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;
import java.time.Duration;
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
public class ElectricLateFeeStrategyTest {
    
    public ElectricLateFeeStrategyTest() {
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
public void testTwoHoursLateFee() {

    int price = 200;
    double latePrice = 0.04;
    int late = 2;

    double result = late * latePrice * price;

    ElectricLateFeeStrategy obj = new ElectricLateFeeStrategy();

    Vehicle vehicle = Vehicle.create(
            "V001",
            "BMW",
            "i4",
            VehicleType.ELECTRIC_VEHICLE,
            VehicleStatus.AVAILABLE,
            price
    );

    Duration overdue = Duration.ofHours(2);

    double res = obj.calculateLateFee(vehicle, overdue);

    assertEquals(result, res);
}
}