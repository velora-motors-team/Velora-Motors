package com.velora.vehicle;

public abstract class Vehicle {

    private String id;
    private String brand;
    private String model;
    private VehicleType type;
    private VehicleStatus status;
    private double dailyPrice;
    private Integer batteryLevel;

    protected Vehicle() {
    }

    protected Vehicle(String id, String brand, String model,
                      VehicleType type, VehicleStatus status, double dailyPrice) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.type = type;
        this.status = status;
        this.dailyPrice = dailyPrice;
        this.batteryLevel = null;
    }

    protected Vehicle(String id, String brand, String model,
                      VehicleType type, VehicleStatus status,
                      double dailyPrice, Integer batteryLevel) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.type = type;
        this.status = status;
        this.dailyPrice = dailyPrice;
        this.batteryLevel = batteryLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public double getDailyPrice() {
        return dailyPrice;
    }

    public void setDailyPrice(double dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }

    public boolean hasBattery() {
        return type == VehicleType.ELECTRIC_VEHICLE
                || type == VehicleType.ELECTRIC_BIKE;
    }

    public String getDisplayName() {
        return brand + " " + model;
    }

    /**
     * Compatibility factory used by callers that only know the stored vehicle type.
     * The returned object is always one of the concrete vehicle subclasses.
     */
    public static Vehicle create() {
        return VehicleFactory.create();
    }

    public static Vehicle create(String id, String brand, String model,
                                 VehicleType type, VehicleStatus status,
                                 double dailyPrice) {
        return VehicleFactory.create(
                id, brand, model, type, status, dailyPrice, null
        );
    }

    public static Vehicle create(String id, String brand, String model,
                                 VehicleType type, VehicleStatus status,
                                 double dailyPrice, Integer batteryLevel) {
        return VehicleFactory.create(
                id, brand, model, type, status, dailyPrice, batteryLevel
        );
    }

    public abstract boolean canBeRentedBy(
            int driverAge,
            boolean hasSpecialLicense
    );
}
