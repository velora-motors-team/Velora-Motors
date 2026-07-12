package com.velora.strategy;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleType;

public final class LateFeeStrategyFactory {

    private static final LateFeeStrategy STANDARD = new StandardLateFeeStrategy();
    private static final LateFeeStrategy ELECTRIC = new ElectricLateFeeStrategy();
    private static final LateFeeStrategy MOTORCYCLE = new MotorcycleLateFeeStrategy();
    private static final LateFeeStrategy TRUCK = new TruckLateFeeStrategy();

    private LateFeeStrategyFactory() {
    }

    public static LateFeeStrategy forVehicle(Vehicle vehicle) {
        if (vehicle == null || vehicle.getType() == null) {
            return STANDARD;
        }

        VehicleType type = vehicle.getType();

        return switch (type) {
            case TRUCK -> TRUCK;
            case MOTORCYCLE, ELECTRIC_BIKE -> MOTORCYCLE;
            case ELECTRIC_VEHICLE, HYBRID_CAR -> ELECTRIC;
            default -> STANDARD;
        };
    }
}
