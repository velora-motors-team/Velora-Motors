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
public class StandardLateFeeStrategyTest {
    
    public StandardLateFeeStrategyTest() {
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
     public void testOneHourLateFee()
     {
            int price =200;
            double late_price=0.05;
            int late=1;
            double result=late*late_price*price;
            StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
            );

            Duration overdue = Duration.ofHours(1);
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
            
     }
     @Test
     public void testThirtyMinutesLateFee()
     {
         
         int price =200;
         double late_price=0.05;
         int late=1;
         double result=late*late_price*price;
         StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
            );
            Duration overdue = Duration.ofMinutes(30);
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
     }
      @Test
     public void testOneSecondLateFee()
     {
         
         int price =200;
         double late_price=0.05;
         int late=1;
         double result=late*late_price*price;
         StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
            );
            Duration overdue = Duration.ofSeconds(1);
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
     }
     
      @Test
     public void testOneHour_Plus_OneSecondLateFee()
     {
         
         int price =200;
         double late_price=0.05;
         int late=2;
         double result=late*late_price*price;
         StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
            );
            Duration overdue = Duration.ofHours(1).plusSeconds(1);
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
     }
     
      @Test
     public void testZeroLateFee()
     {
         
         int price =200;
         double late_price=0.05;
         int late=0;
         double result=late*late_price*price;
         StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
            );
            Duration overdue = Duration.ZERO;
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
     }
     
      @Test
     public void testNegativeLateFee()
     {
         
         int price =200;
         double late_price=0.05;
         int late=0;
         double result=late*late_price*price;
         StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
            );
            Duration overdue = Duration.ofMinutes(-30);
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
     }
     
      @Test
     public void testNullDurationLateFee()
     {
         
         int price =200;
         double late_price=0.05;
         int late=0;
         double result=late*late_price*price;
         StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = Vehicle.create(
            "V001",
            "Toyota",
            "Corolla",
            VehicleType.CAR,
            VehicleStatus.AVAILABLE,
            200.0
            );
            Duration overdue = null;
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
     }
     
     @Test
     public void testNullVehicleLateFee()
     {
         
         int price =200;
         double late_price=0.05;
         int late=0;
         double result=late*late_price*price;
         StandardLateFeeStrategy obj=new StandardLateFeeStrategy ();
            Vehicle vehicle = null;
            Duration overdue = Duration.ofHours(1);
            double res=obj.calculateLateFee(vehicle, overdue);
            assertEquals(result,res);
     }
    
}
