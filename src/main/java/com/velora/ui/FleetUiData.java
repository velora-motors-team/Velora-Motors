package com.velora.ui;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleType;

import java.util.Locale;

final class FleetUiData {

    private FleetUiData() {
    }

    static String displayName(Vehicle vehicle) {
        return vehicle.getBrand() + " " + vehicle.getModel();
    }

    static String imagePath(Vehicle vehicle) {
        return "/images/vehicles/" + vehicle.getId() + ".jpg";
    }

    static String color(Vehicle vehicle) {
        String[] colors = {
                "Black Sapphire",
                "Frozen Grey",
                "Isle of Man Green",
                "Alpine White",
                "Portimao Blue",
                "Toronto Red",
                "Thundernight Purple"
        };
        return colors[Math.floorMod(displayName(vehicle).hashCode(), colors.length)];
    }

    static String vin(Vehicle vehicle) {
        String seed = (vehicle.getId() + displayName(vehicle))
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        return ("WBAVM" + seed + "X12345").substring(0, 17);
    }

    static String plate(Vehicle vehicle) {
        return vehicle.getId() + "-" + Math.floorMod(displayName(vehicle).hashCode(), 9000);
    }

    static String serviceType(Vehicle vehicle, int index) {
        if (vehicle.getType() == VehicleType.ELECTRIC_VEHICLE || vehicle.getType() == VehicleType.ELECTRIC_BIKE) {
            return index % 2 == 0 ? "Battery Check" : "Full Inspection";
        }
        return switch (index % 4) {
            case 0 -> "Oil Service";
            case 1 -> "Brake Inspection";
            case 2 -> "Tire Service";
            default -> "Full Inspection";
        };
    }
}
