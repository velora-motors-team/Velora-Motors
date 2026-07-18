package com.velora.vehicle;

public class Car extends Vehicle {

    public Car() {
        setType(VehicleType.CAR);
    }

    public Car(String id, String brand, String model,
               VehicleStatus status, double dailyPrice) {
        this(id, brand, model, VehicleType.CAR, status, dailyPrice, null);
    }

    public Car(String id, String brand, String model,
               VehicleType type, VehicleStatus status,
               double dailyPrice, Integer batteryLevel) {
        super(id, brand, model, type, status, dailyPrice, batteryLevel);
    }

    @Override
    public boolean canBeRentedBy(int driverAge, boolean hasSpecialLicense) {
        return true;
    }
}
