package com.velora.vehicle;

public class Vehicle {

    private String id;
    private String brand;
    private String model;
    private VehicleType type;
    private VehicleStatus status;
    private double dailyPrice;
    private Integer batteryLevel;

    public Vehicle() {
    }

    public Vehicle(String id, String brand, String model,
                   VehicleType type, VehicleStatus status, double dailyPrice) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.type = type;
        this.status = status;
        this.dailyPrice = dailyPrice;
        this.batteryLevel = null;
    }

    public Vehicle(String id, String brand, String model,
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
}