package com.velora.repository;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class VehicleRepository {

    private final List<Vehicle> vehicles;

    public VehicleRepository() {
        vehicles = new ArrayList<>();

        vehicles.add(new Vehicle(
                "V001",
                "Toyota",
                "Prius",
                VehicleType.HYBRID_CAR,
                VehicleStatus.AVAILABLE,
                35.0
        ));

        vehicles.add(new Vehicle(
                "V002",
                "BMW",
                "i8 Roadster",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                90.0,
                100
        ));

        vehicles.add(new Vehicle(
                "V003",
                "Xiaomi",
                "Electric Bike Pro",
                VehicleType.ELECTRIC_BIKE,
                VehicleStatus.AVAILABLE,
                12.0,
                70
        ));

        vehicles.add(new Vehicle(
                "V004",
                "Yamaha",
                "MT-07",
                VehicleType.MOTORCYCLE,
                VehicleStatus.AVAILABLE,
                40.0
        ));

        vehicles.add(new Vehicle(
                "V005",
                "Hyundai",
                "Tucson",
                VehicleType.SUV,
                VehicleStatus.RENTED,
                45.0
        ));

        vehicles.add(new Vehicle(
                "V006",
                "Ford",
                "Transit",
                VehicleType.TRUCK,
                VehicleStatus.MAINTENANCE,
                65.0
        ));

        vehicles.add(new Vehicle(
                "V007",
                "Tesla",
                "Model 3",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                80.0,
                50
        ));
    }

    public List<Vehicle> findAll() {
        return vehicles;
    }
}