package com.velora.observer;

import com.velora.vehicle.Vehicle;

/** Observer that receives a vehicle-availability event. */
public interface VehicleAvailabilityObserver {
    void onVehicleAvailable(Vehicle vehicle);
}
