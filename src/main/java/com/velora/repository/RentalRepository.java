package com.velora.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class RentalRepository {

    private static final String HEADER =
            "RENTAL\trentalId\tcustomerEmail\tcustomerName\tvehicleId\tvehicleName\tstartDateTime\t"
            + "expectedReturnDateTime\tactualReturnDateTime\tstatus\trentalDays\tdailyRate\tbaseAmount\t"
            + "lateFee\tinvoiceId\tpaymentStatus";

    private static final Path FILE = DataStoreSupport.dataFile("rentals.tsv");

    public RentalRepository() {
        DataStoreSupport.ensureFile(FILE, HEADER);
    }

    public synchronized List<RentalRecord> findAll() {
        List<RentalRecord> rentals = new ArrayList<>();

        for (String line : DataStoreSupport.readDataLines(FILE)) {
            String[] p = line.split("\\t", -1);

            if (p.length != 16 || !"RENTAL".equals(p[0])) {
                continue;
            }

            rentals.add(new RentalRecord(
                    DataStoreSupport.decode(p[1]),
                    DataStoreSupport.decode(p[2]),
                    DataStoreSupport.decode(p[3]),
                    DataStoreSupport.decode(p[4]),
                    DataStoreSupport.decode(p[5]),
                    DataStoreSupport.decode(p[6]),
                    DataStoreSupport.decode(p[7]),
                    DataStoreSupport.decode(p[8]),
                    DataStoreSupport.decode(p[9]),
                    DataStoreSupport.parseInt(p[10], 1),
                    DataStoreSupport.parseDouble(p[11], 0),
                    DataStoreSupport.parseDouble(p[12], 0),
                    DataStoreSupport.parseDouble(p[13], 0),
                    DataStoreSupport.decode(p[14]),
                    DataStoreSupport.decode(p[15])
            ));
        }

        return rentals;
    }

    public synchronized List<RentalRecord> findByCustomerEmail(String email) {
        String normalized = normalize(email);

        return findAll().stream()
                .filter(r -> normalize(r.customerEmail()).equals(normalized))
                .toList();
    }

    public synchronized Optional<RentalRecord> findById(String rentalId) {
        return findAll().stream()
                .filter(r -> r.rentalId().equalsIgnoreCase(rentalId))
                .findFirst();
    }

    public synchronized boolean hasActiveRentalForVehicle(String vehicleId) {
        return findAll().stream().anyMatch(r ->
                r.vehicleId().equalsIgnoreCase(vehicleId)
                        && isOpenRentalStatus(r.status())
        );
    }

    public synchronized RentalRecord create(
            String customerEmail,
            String customerName,
            String vehicleId,
            String vehicleName,
            LocalDateTime start,
            LocalDateTime expectedReturn,
            int rentalDays,
            double dailyRate,
            double baseAmount,
            String invoiceId,
            String paymentStatus
    ) {
        RentalRecord record = new RentalRecord(
                DataStoreSupport.newId("RNT"),
                normalize(customerEmail),
                customerName,
                vehicleId,
                vehicleName,
                start.toString(),
                expectedReturn.toString(),
                "",
                "ACTIVE",
                rentalDays,
                dailyRate,
                baseAmount,
                0.0,
                invoiceId,
                paymentStatus
        );

        save(record);
        return record;
    }

    public synchronized void save(RentalRecord record) {
        DataStoreSupport.appendLine(FILE, serialize(record));
    }

    public synchronized boolean updateStatus(
            String rentalId,
            String newStatus,
            String actualReturnDateTime,
            double lateFee
    ) {
        Optional<RentalRecord> existing = findById(rentalId);

        if (existing.isEmpty()) {
            return false;
        }

        RentalRecord current = existing.get();

        RentalRecord updated = new RentalRecord(
                current.rentalId(),
                current.customerEmail(),
                current.customerName(),
                current.vehicleId(),
                current.vehicleName(),
                current.startDateTime(),
                current.expectedReturnDateTime(),
                actualReturnDateTime == null ? "" : actualReturnDateTime,
                normalizeStatus(newStatus),
                current.rentalDays(),
                current.dailyRate(),
                current.baseAmount(),
                Math.max(0.0, lateFee),
                current.invoiceId(),
                current.paymentStatus()
        );

        return replace(updated);
    }

    public synchronized boolean updateTimingStatus(
            String rentalId,
            String newStatus,
            double lateFee
    ) {
        Optional<RentalRecord> existing = findById(rentalId);

        if (existing.isEmpty()) {
            return false;
        }

        RentalRecord current = existing.get();

        RentalRecord updated = new RentalRecord(
                current.rentalId(),
                current.customerEmail(),
                current.customerName(),
                current.vehicleId(),
                current.vehicleName(),
                current.startDateTime(),
                current.expectedReturnDateTime(),
                current.actualReturnDateTime(),
                normalizeStatus(newStatus),
                current.rentalDays(),
                current.dailyRate(),
                current.baseAmount(),
                Math.max(0.0, lateFee),
                current.invoiceId(),
                current.paymentStatus()
        );

        return replace(updated);
    }

    public synchronized boolean updateLateFee(
            String rentalId,
            double lateFee
    ) {
        Optional<RentalRecord> existing = findById(rentalId);

        if (existing.isEmpty()) {
            return false;
        }

        RentalRecord current = existing.get();

        RentalRecord updated = new RentalRecord(
                current.rentalId(),
                current.customerEmail(),
                current.customerName(),
                current.vehicleId(),
                current.vehicleName(),
                current.startDateTime(),
                current.expectedReturnDateTime(),
                current.actualReturnDateTime(),
                current.status(),
                current.rentalDays(),
                current.dailyRate(),
                current.baseAmount(),
                Math.max(0.0, lateFee),
                current.invoiceId(),
                current.paymentStatus()
        );

        return replace(updated);
    }

    public synchronized boolean markReturned(
            String rentalId,
            LocalDateTime actualReturn,
            double finalLateFee
    ) {
        if (actualReturn == null) {
            actualReturn = LocalDateTime.now();
        }

        return updateStatus(
                rentalId,
                "RETURNED",
                actualReturn.toString(),
                finalLateFee
        );
    }

    private synchronized boolean replace(RentalRecord replacement) {
        List<RentalRecord> all = findAll();
        boolean updated = false;
        List<String> lines = new ArrayList<>();

        for (RentalRecord record : all) {
            if (record.rentalId().equalsIgnoreCase(replacement.rentalId())) {
                lines.add(serialize(replacement));
                updated = true;
            } else {
                lines.add(serialize(record));
            }
        }

        if (updated) {
            DataStoreSupport.writeAll(FILE, lines, HEADER);
        }

        return updated;
    }

    private static boolean isOpenRentalStatus(String status) {
        String normalized = normalizeStatus(status);

        return "ACTIVE".equals(normalized)
                || "RETURN_DUE".equals(normalized)
                || "OVERDUE".equals(normalized)
                || "LATE".equals(normalized);
    }

    private static String normalizeStatus(String status) {
        return status == null
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);
    }

    private static String serialize(RentalRecord r) {
        return String.join("\t",
                "RENTAL",
                DataStoreSupport.encode(r.rentalId()),
                DataStoreSupport.encode(r.customerEmail()),
                DataStoreSupport.encode(r.customerName()),
                DataStoreSupport.encode(r.vehicleId()),
                DataStoreSupport.encode(r.vehicleName()),
                DataStoreSupport.encode(r.startDateTime()),
                DataStoreSupport.encode(r.expectedReturnDateTime()),
                DataStoreSupport.encode(r.actualReturnDateTime()),
                DataStoreSupport.encode(r.status()),
                String.valueOf(r.rentalDays()),
                String.valueOf(r.dailyRate()),
                String.valueOf(r.baseAmount()),
                String.valueOf(r.lateFee()),
                DataStoreSupport.encode(r.invoiceId()),
                DataStoreSupport.encode(r.paymentStatus())
        );
    }

    private static String normalize(String email) {
        return email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);
    }

    public record RentalRecord(
            String rentalId,
            String customerEmail,
            String customerName,
            String vehicleId,
            String vehicleName,
            String startDateTime,
            String expectedReturnDateTime,
            String actualReturnDateTime,
            String status,
            int rentalDays,
            double dailyRate,
            double baseAmount,
            double lateFee,
            String invoiceId,
            String paymentStatus
    ) {
    }
}
