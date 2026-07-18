package com.velora.vehicle;

/** Electric bike: combines the motorcycle age rule with the battery rule. */
public final class ElectricMotorcycle extends Motorcycle {

    public ElectricMotorcycle() {
        setType(VehicleType.ELECTRIC_BIKE);
    }

    public ElectricMotorcycle(String id, String brand, String model,
                              VehicleStatus status, double dailyPrice,
                              Integer batteryLevel) {
        super(id, brand, model, status, dailyPrice, batteryLevel);
        setType(VehicleType.ELECTRIC_BIKE);
    }

    public boolean hasEnoughBattery() {
        return getBatteryLevel() != null
                && getBatteryLevel() >= ElectricVehicle.MINIMUM_BATTERY_LEVEL;
    }

    @Override
    public boolean canBeRentedBy(int driverAge, boolean hasSpecialLicense) {
        return super.canBeRentedBy(driverAge, hasSpecialLicense)
                && hasEnoughBattery();
    }
}
