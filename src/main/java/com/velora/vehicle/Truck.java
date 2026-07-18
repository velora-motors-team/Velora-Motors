package com.velora.vehicle;

public class Truck extends Vehicle {

    public Truck() {
        setType(VehicleType.TRUCK);
    }

    public Truck(String id, String brand, String model,
                 VehicleStatus status, double dailyPrice) {
        this(id, brand, model, status, dailyPrice, null);
    }

    public Truck(String id, String brand, String model,
                 VehicleStatus status, double dailyPrice,
                 Integer batteryLevel) {
        super(
                id, brand, model, VehicleType.TRUCK,
                status, dailyPrice, batteryLevel
        );
    }

    @Override
    public boolean canBeRentedBy(int driverAge, boolean hasSpecialLicense) {
        return hasSpecialLicense;
    }
}
