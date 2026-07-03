package com.velora.service;

import com.velora.repository.VehicleRepository;
import com.velora.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService() {
        this.vehicleRepository = new VehicleRepository();
    }

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> availableVehicles = new ArrayList<>();

        for (Vehicle vehicle : vehicleRepository.findAll()) {
            if (vehicle.isAvailable()) {
                availableVehicles.add(vehicle);
            }
        }

        return availableVehicles;
    }

    public int countAvailableVehicles() {
        return getAvailableVehicles().size();
    }
}