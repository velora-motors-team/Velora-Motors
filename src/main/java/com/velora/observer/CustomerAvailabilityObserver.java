package com.velora.observer;

import com.velora.repository.NotificationRepository;
import com.velora.vehicle.Vehicle;

import java.util.Objects;

/** Concrete observer that delivers the event to a customer notification inbox. */
public final class CustomerAvailabilityObserver implements VehicleAvailabilityObserver {

    private final String customerEmail;
    private final String customerName;
    private final NotificationRepository notificationRepository;

    public CustomerAvailabilityObserver(String customerEmail) {
        this("Customer", customerEmail, new NotificationRepository());
    }

    public CustomerAvailabilityObserver(
            String customerName,
            String customerEmail,
            NotificationRepository notificationRepository
    ) {
        this.customerName = Objects.requireNonNullElse(customerName, "Customer").trim();
        this.customerEmail = Objects.requireNonNullElse(customerEmail, "").trim().toLowerCase();
        this.notificationRepository = Objects.requireNonNull(notificationRepository);
    }

    @Override
    public void onVehicleAvailable(Vehicle vehicle) {
        notificationRepository.create(
                customerEmail,
                "VEHICLE_AVAILABLE",
                "Vehicle Available",
                vehicle.getDisplayName() + " is now available for rent."
        );
        notificationRepository.create(
                customerEmail,
                "ADMIN_EMAIL",
                "Your requested vehicle is ready",
                "From: Velora Motors Administration\n\nHello " + displayName()
                        + ",\n" + vehicle.getDisplayName()
                        + " is now available and ready for rent."
        );
    }

    private String displayName() {
        return customerName.isBlank() ? "Customer" : customerName;
    }
}
