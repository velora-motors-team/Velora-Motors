package com.velora.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReservationRepository {

    private static final String HEADER = "RESERVATION\treservationId\tcustomerEmail\tvehicleId\tpickupDateTime\treturnDateTime\tstatus\tcreatedAt";
    private static final Path FILE = DataStoreSupport.dataFile("reservations.tsv");

    public ReservationRepository() {
        DataStoreSupport.ensureFile(FILE, HEADER);
    }

    public synchronized List<ReservationRecord> findAll() {
        List<ReservationRecord> result = new ArrayList<>();
        for (String line : DataStoreSupport.readDataLines(FILE)) {
            String[] p = line.split("\\t", -1);
            if (p.length != 8 || !"RESERVATION".equals(p[0])) {
                continue;
            }
            result.add(new ReservationRecord(
                    DataStoreSupport.decode(p[1]),
                    DataStoreSupport.decode(p[2]),
                    DataStoreSupport.decode(p[3]),
                    DataStoreSupport.decode(p[4]),
                    DataStoreSupport.decode(p[5]),
                    DataStoreSupport.decode(p[6]),
                    DataStoreSupport.decode(p[7])
            ));
        }
        return result;
    }

    public synchronized List<ReservationRecord> findByCustomerEmail(String email) {
        String normalized = normalize(email);
        return findAll().stream()
                .filter(r -> normalize(r.customerEmail()).equals(normalized))
                .toList();
    }

    public synchronized boolean hasOverlap(String vehicleId, LocalDateTime pickup, LocalDateTime returned) {
        return findAll().stream()
                .filter(r -> r.vehicleId().equalsIgnoreCase(vehicleId))
                .filter(r -> "CONFIRMED".equalsIgnoreCase(r.status()) || "ACTIVE".equalsIgnoreCase(r.status()))
                .anyMatch(r -> overlaps(
                        pickup,
                        returned,
                        LocalDateTime.parse(r.pickupDateTime()),
                        LocalDateTime.parse(r.returnDateTime())
                ));
    }

    public synchronized ReservationRecord create(
            String customerEmail,
            String vehicleId,
            LocalDateTime pickup,
            LocalDateTime returned
    ) {
        if (hasOverlap(vehicleId, pickup, returned)) {
            throw new IllegalStateException("Vehicle is already reserved for the selected period.");
        }
        ReservationRecord record = new ReservationRecord(
                DataStoreSupport.newId("RES"),
                normalize(customerEmail),
                vehicleId,
                pickup.toString(),
                returned.toString(),
                "CONFIRMED",
                LocalDateTime.now().toString()
        );
        DataStoreSupport.appendLine(FILE, serialize(record));
        return record;
    }


    public synchronized boolean updateStatus(String reservationId, String newStatus) {
        List<ReservationRecord> all = findAll();
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        for (ReservationRecord record : all) {
            ReservationRecord out = record;
            if (record.reservationId().equalsIgnoreCase(reservationId)) {
                out = new ReservationRecord(
                        record.reservationId(),
                        record.customerEmail(),
                        record.vehicleId(),
                        record.pickupDateTime(),
                        record.returnDateTime(),
                        newStatus,
                        record.createdAt()
                );
                updated = true;
            }
            lines.add(serialize(out));
        }

        if (updated) {
            DataStoreSupport.writeAll(FILE, lines, HEADER);
        }
        return updated;
    }

    private static boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd,
                                    LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    private static String serialize(ReservationRecord r) {
        return String.join("\t",
                "RESERVATION",
                DataStoreSupport.encode(r.reservationId()),
                DataStoreSupport.encode(r.customerEmail()),
                DataStoreSupport.encode(r.vehicleId()),
                DataStoreSupport.encode(r.pickupDateTime()),
                DataStoreSupport.encode(r.returnDateTime()),
                DataStoreSupport.encode(r.status()),
                DataStoreSupport.encode(r.createdAt())
        );
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record ReservationRecord(
            String reservationId,
            String customerEmail,
            String vehicleId,
            String pickupDateTime,
            String returnDateTime,
            String status,
            String createdAt
    ) {
    }
}
