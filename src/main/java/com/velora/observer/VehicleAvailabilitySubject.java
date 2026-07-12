package com.velora.observer;

import com.velora.authentication.Customer;
import com.velora.repository.VehicleWaitlistRepository;
import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;

import java.util.List;
import java.util.Objects;

/** Subject responsible for persistent vehicle waitlists and observer notification. */
public final class VehicleAvailabilitySubject {

    private final VehicleWaitlistRepository waitlistRepository;

    public VehicleAvailabilitySubject() {
        this(new VehicleWaitlistRepository());
    }

    public VehicleAvailabilitySubject(VehicleWaitlistRepository waitlistRepository) {
        this.waitlistRepository = Objects.requireNonNull(waitlistRepository);
    }

    public boolean addObserver(Vehicle vehicle, Customer customer) {
        requireVehicle(vehicle);
        if (customer == null || customer.getEmail().isBlank()) {
            return false;
        }
        return waitlistRepository.subscribe(
                vehicle.getId(),
                customer.getFullName(),
                customer.getEmail()
        );
    }

    public boolean removeObserver(Vehicle vehicle, Customer customer) {
        requireVehicle(vehicle);
        if (customer == null || customer.getEmail().isBlank()) {
            return false;
        }
        return waitlistRepository.unsubscribe(vehicle.getId(), customer.getEmail());
    }

    public boolean hasObserver(Vehicle vehicle, Customer customer) {
        return vehicle != null && customer != null
                && waitlistRepository.isSubscribed(vehicle.getId(), customer.getEmail());
    }

    /**
     * Notifies all observers and expires the waitlist after successful delivery.
     *
     * @return number of notified customers
     */
    public int notifyObservers(Vehicle vehicle) {
        requireVehicle(vehicle);
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            return 0;
        }

        List<VehicleWaitlistRepository.WaitlistRecord> subscriptions =
                waitlistRepository.findByVehicleId(vehicle.getId());
        for (VehicleWaitlistRepository.WaitlistRecord subscription : subscriptions) {
            VehicleAvailabilityObserver observer =
                    new CustomerAvailabilityObserver(
                            subscription.customerName(),
                            subscription.customerEmail(),
                            new com.velora.repository.NotificationRepository()
                    );
            observer.onVehicleAvailable(vehicle);
        }
        if (!subscriptions.isEmpty()) {
            waitlistRepository.removeByVehicleId(vehicle.getId());
        }
        return subscriptions.size();
    }

    private static void requireVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle");
        if (vehicle.getId() == null || vehicle.getId().isBlank()) {
            throw new IllegalArgumentException("Vehicle must have an ID");
        }
    }
}
