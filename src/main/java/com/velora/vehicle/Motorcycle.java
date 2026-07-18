package com.velora.vehicle;

public class Motorcycle extends Vehicle {

    public static final int MINIMUM_DRIVER_AGE = 21;

    public Motorcycle() {
        setType(VehicleType.MOTORCYCLE);
    }

    public Motorcycle(String id, String brand, String model,
                      VehicleStatus status, double dailyPrice) {
        this(id, brand, model, status, dailyPrice, null);
    }

    public Motorcycle(String id, String brand, String model,
                      VehicleStatus status, double dailyPrice,
                      Integer batteryLevel) {
        super(
                id, brand, model, VehicleType.MOTORCYCLE,
                status, dailyPrice, batteryLevel
        );
    }

    @Override
    public boolean canBeRentedBy(int driverAge, boolean hasSpecialLicense) {
        return driverAge >= MINIMUM_DRIVER_AGE;
    }
}
