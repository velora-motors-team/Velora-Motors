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

    Vehicle vehicle = new Vehicle(
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
}
