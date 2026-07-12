package com.velora.service;

import com.velora.observer.VehicleAvailabilitySubject;
import com.velora.repository.VehicleRepository;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;

import java.util.ArrayList;
import java.util.List;

public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleAvailabilitySubject availabilitySubject;

    public VehicleService() {
        this.vehicleRepository = new VehicleRepository();
        this.availabilitySubject = new VehicleAvailabilitySubject();
    }

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
        this.availabilitySubject = new VehicleAvailabilitySubject();
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

    public void saveVehicles() {
        vehicleRepository.saveAll();
    }

    /** Changes status and fires the Observer event for every transition to AVAILABLE. */
    public int changeStatus(Vehicle vehicle, VehicleStatus newStatus) {
        if (vehicle == null || newStatus == null) {
            return 0;
        }
        VehicleStatus previousStatus = vehicle.getStatus();
        vehicle.setStatus(newStatus);
        saveVehicles();
        if (previousStatus != VehicleStatus.AVAILABLE && newStatus == VehicleStatus.AVAILABLE) {
            return availabilitySubject.notifyObservers(vehicle);
        }
        return 0;
    }

    /** Delivers subscriptions left pending if a vehicle was made available by an older code path. */
    public int notifyWaitlistsForAvailableVehicles() {
        int notified = 0;
        for (Vehicle vehicle : getAllVehicles()) {
            if (vehicle.getStatus() == VehicleStatus.AVAILABLE) {
                notified += availabilitySubject.notifyObservers(vehicle);
            }
        }
        return notified;
    }
}
