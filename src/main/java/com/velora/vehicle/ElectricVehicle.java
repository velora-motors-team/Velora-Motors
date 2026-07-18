package com.velora.vehicle;

public class ElectricVehicle extends Vehicle {

    public static final int MINIMUM_BATTERY_LEVEL = 30;

    public ElectricVehicle() {
        setType(VehicleType.ELECTRIC_VEHICLE);
    }

    public ElectricVehicle(String id, String brand, String model,
                           VehicleStatus status, double dailyPrice,
                           Integer batteryLevel) {
        this(
                id, brand, model, VehicleType.ELECTRIC_VEHICLE,
                status, dailyPrice, batteryLevel
        );
    }

    public ElectricVehicle(String id, String brand, String model,
                           VehicleType type, VehicleStatus status,
                           double dailyPrice, Integer batteryLevel) {
        super(id, brand, model, type, status, dailyPrice, batteryLevel);
    }

    public boolean hasEnoughBattery() {
        return getBatteryLevel() != null
                && getBatteryLevel() >= MINIMUM_BATTERY_LEVEL;
    }

    @Override
    public boolean canBeRentedBy(int driverAge, boolean hasSpecialLicense) {
        return hasEnoughBattery();
    }
}
