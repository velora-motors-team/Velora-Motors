package com.velora.vehicle;

/** Creates the concrete vehicle subtype represented by a persisted VehicleType. */
public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static Vehicle create() {
        return new Car();
    }

    public static Vehicle create(
            String id,
            String brand,
            String model,
            VehicleType type,
            VehicleStatus status,
            double dailyPrice,
            Integer batteryLevel
    ) {
        if (type == null) {
            return new Car(
                    id, brand, model, null, status, dailyPrice, batteryLevel
            );
        }

        return switch (type) {
            case TRUCK -> new Truck(
                    id, brand, model, status, dailyPrice, batteryLevel
            );
            case MOTORCYCLE -> new Motorcycle(
                    id, brand, model, status, dailyPrice, batteryLevel
            );
            case ELECTRIC_VEHICLE -> new ElectricVehicle(
                    id, brand, model, type, status, dailyPrice, batteryLevel
            );
            case ELECTRIC_BIKE -> new ElectricMotorcycle(
                    id, brand, model, status, dailyPrice, batteryLevel
            );
            case CAR, SUV, HYBRID_CAR -> new Car(
                    id, brand, model, type, status, dailyPrice, batteryLevel
            );
        };
    }
}
