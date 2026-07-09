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
                "VM-0001",
                "BMW",
                "XM Label",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                520.0
        ));

        vehicles.add(new Vehicle(
                "VM-0002",
                "BMW",
                "XM 50e",
                VehicleType.HYBRID_CAR,
                VehicleStatus.RENTED,
                430.0
        ));

        vehicles.add(new Vehicle(
                "VM-0003",
                "BMW",
                "X7 M60i",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                390.0
        ));

        vehicles.add(new Vehicle(
                "VM-0004",
                "BMW",
                "X6 M Competition",
                VehicleType.SUV,
                VehicleStatus.RENTED,
                470.0
        ));

        vehicles.add(new Vehicle(
                "VM-0005",
                "BMW",
                "X6 M60i",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                360.0
        ));

        vehicles.add(new Vehicle(
                "VM-0006",
                "BMW",
                "X5 M Competition",
                VehicleType.SUV,
                VehicleStatus.MAINTENANCE,
                440.0
        ));

        vehicles.add(new Vehicle(
                "VM-0007",
                "BMW",
                "X5 M60e xDrive",
                VehicleType.HYBRID_CAR,
                VehicleStatus.AVAILABLE,
                340.0
        ));

        vehicles.add(new Vehicle(
                "VM-0008",
                "BMW",
                "X3 M50",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                260.0
        ));

        vehicles.add(new Vehicle(
                "VM-0009",
                "BMW",
                "X2 M35i",
                VehicleType.SUV,
                VehicleStatus.RENTED,
                210.0
        ));

        vehicles.add(new Vehicle(
                "VM-0010",
                "BMW",
                "X1 M35i",
                VehicleType.SUV,
                VehicleStatus.AVAILABLE,
                190.0
        ));

        vehicles.add(new Vehicle(
                "VM-0011",
                "BMW",
                "iX M70",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                410.0,
                92
        ));

        vehicles.add(new Vehicle(
                "VM-0012",
                "BMW",
                "i7 M70 xDrive",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.RENTED,
                480.0,
                78
        ));

        vehicles.add(new Vehicle(
                "VM-0013",
                "BMW",
                "M760e xDrive",
                VehicleType.HYBRID_CAR,
                VehicleStatus.AVAILABLE,
                455.0
        ));

        vehicles.add(new Vehicle(
                "VM-0014",
                "BMW",
                "M5 Sedan",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                430.0
        ));

        vehicles.add(new Vehicle(
                "VM-0015",
                "BMW",
                "M5 Touring",
                VehicleType.CAR,
                VehicleStatus.MAINTENANCE,
                440.0
        ));

        vehicles.add(new Vehicle(
                "VM-0016",
                "BMW",
                "i5 M60 Sedan",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                315.0,
                88
        ));

        vehicles.add(new Vehicle(
                "VM-0017",
                "BMW",
                "i5 M60 Touring",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.RENTED,
                325.0,
                81
        ));

        vehicles.add(new Vehicle(
                "VM-0018",
                "BMW",
                "M4 Coupe",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                360.0
        ));

        vehicles.add(new Vehicle(
                "VM-0019",
                "BMW",
                "M4 Competition Coupe",
                VehicleType.CAR,
                VehicleStatus.RENTED,
                395.0
        ));

        vehicles.add(new Vehicle(
                "VM-0020",
                "BMW",
                "M4 Competition Convertible",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                410.0
        ));

        vehicles.add(new Vehicle(
                "VM-0021",
                "BMW",
                "M440i Gran Coupe",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                275.0
        ));

        vehicles.add(new Vehicle(
                "VM-0022",
                "BMW",
                "i4 M60 xDrive",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.MAINTENANCE,
                295.0,
                73
        ));

        vehicles.add(new Vehicle(
                "VM-0023",
                "BMW",
                "M3 Sedan",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                350.0
        ));

        vehicles.add(new Vehicle(
                "VM-0024",
                "BMW",
                "M3 Competition Sedan",
                VehicleType.CAR,
                VehicleStatus.RENTED,
                385.0
        ));

        vehicles.add(new Vehicle(
                "VM-0025",
                "BMW",
                "M3 Competition Touring",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                400.0
        ));

        vehicles.add(new Vehicle(
                "VM-0026",
                "BMW",
                "M340i Sedan",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                245.0
        ));

        vehicles.add(new Vehicle(
                "VM-0027",
                "BMW",
                "M2",
                VehicleType.CAR,
                VehicleStatus.RENTED,
                300.0
        ));

        vehicles.add(new Vehicle(
                "VM-0028",
                "BMW",
                "M2 CS",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                340.0
        ));

        vehicles.add(new Vehicle(
                "VM-0029",
                "BMW",
                "Z4 M40i",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                285.0
        ));

        vehicles.add(new Vehicle(
                "VM-0030",
                "BMW",
                "Z4 Final Edition",
                VehicleType.CAR,
                VehicleStatus.MAINTENANCE,
                310.0
        ));
    }

    public List<Vehicle> findAll() {
        return vehicles;
    }
}
