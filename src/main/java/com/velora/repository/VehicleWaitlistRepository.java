package com.velora.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Persistent subscriptions for customers waiting for a specific vehicle. */
public final class VehicleWaitlistRepository {

    private static final String HEADER = "vehicleId\tcustomerName\tcustomerEmail\tsubscribedAt";
    private static final Path FILE = DataStoreSupport.dataFile("vehicle-waitlist.tsv");

    public VehicleWaitlistRepository() {
        DataStoreSupport.ensureFile(FILE, HEADER);
    }

    public synchronized List<WaitlistRecord> findAll() {
        List<WaitlistRecord> result = new ArrayList<>();
        for (String line : DataStoreSupport.readDataLines(FILE)) {
            String[] fields = line.split("\\t", -1);
            if (fields.length == 3) {
                // Backward compatibility with the original waitlist format.
                result.add(new WaitlistRecord(fields[0], "", normalize(fields[1]), fields[2]));
                continue;
            }
            if (fields.length != 4) {
                continue;
            }
            result.add(new WaitlistRecord(fields[0], fields[1], normalize(fields[2]), fields[3]));
        }
        return result;
    }

    public synchronized List<WaitlistRecord> findByVehicleId(String vehicleId) {
        String normalizedId = normalizeId(vehicleId);
        return findAll().stream()
                .filter(record -> normalizeId(record.vehicleId()).equals(normalizedId))
                .toList();
    }

    public synchronized boolean isSubscribed(String vehicleId, String customerEmail) {
        String normalizedEmail = normalize(customerEmail);
        return findByVehicleId(vehicleId).stream()
                .anyMatch(record -> record.customerEmail().equals(normalizedEmail));
    }

    /** @return false when the same customer is already subscribed to this vehicle. */
    public synchronized boolean subscribe(String vehicleId, String customerName, String customerEmail) {
        String normalizedId = normalizeId(vehicleId);
        String normalizedEmail = normalize(customerEmail);
        if (normalizedId.isBlank() || normalizedEmail.isBlank()
                || isSubscribed(normalizedId, normalizedEmail)) {
            return false;
        }

        WaitlistRecord record = new WaitlistRecord(
                normalizedId,
                sanitize(customerName),
                normalizedEmail,
                LocalDateTime.now().toString()
        );
        DataStoreSupport.appendLine(FILE, serialize(record));
        return true;
    }

    public synchronized boolean unsubscribe(String vehicleId, String customerEmail) {
        String normalizedId = normalizeId(vehicleId);
        String normalizedEmail = normalize(customerEmail);
        List<WaitlistRecord> all = findAll();
        List<WaitlistRecord> remaining = all.stream()
                .filter(record -> !(normalizeId(record.vehicleId()).equals(normalizedId)
                        && record.customerEmail().equals(normalizedEmail)))
                .toList();
        if (remaining.size() == all.size()) {
            return false;
        }
        writeAll(remaining);
        return true;
    }

    public synchronized void removeByVehicleId(String vehicleId) {
        String normalizedId = normalizeId(vehicleId);
        List<WaitlistRecord> remaining = findAll().stream()
                .filter(record -> !normalizeId(record.vehicleId()).equals(normalizedId))
                .toList();
        writeAll(remaining);
    }

    private static void writeAll(List<WaitlistRecord> records) {
        DataStoreSupport.writeAll(
                FILE,
                records.stream().map(VehicleWaitlistRepository::serialize).toList(),
                HEADER
        );
    }

    private static String serialize(WaitlistRecord record) {
        return String.join("\t",
                record.vehicleId(),
                record.customerName(),
                record.customerEmail(),
                record.subscribedAt()
        );
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.replace('\t', ' ').replace('\n', ' ').trim();
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeId(String vehicleId) {
        return vehicleId == null ? "" : vehicleId.trim().toUpperCase(Locale.ROOT);
    }

    public record WaitlistRecord(
            String vehicleId,
            String customerName,
            String customerEmail,
            String subscribedAt
    ) {
    }
}
