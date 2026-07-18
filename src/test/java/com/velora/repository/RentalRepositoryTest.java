package com.velora.repository;

import com.velora.repository.RentalRepository.RentalRecord;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RentalRepositoryTest {

    private Path rentalFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        rentalFile = getRentalFile();

        originalFileExisted = Files.exists(rentalFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(rentalFile)
                : null;

        Files.deleteIfExists(rentalFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(rentalFile);

        if (originalFileExisted) {
            Files.createDirectories(rentalFile.getParent());
            Files.write(rentalFile, originalFileContent);
        }
    }

    private RentalRecord createRental(
            String rentalId,
            String customerEmail,
            String vehicleId,
            String status,
            double lateFee
    ) {
        return new RentalRecord(
                rentalId,
                customerEmail,
                "Hamada Ahmad",
                vehicleId,
                "BMW X5",
                "2026-07-14T10:00:00",
                "2026-07-16T10:00:00",
                "",
                status,
                2,
                250.0,
                500.0,
                lateFee,
                "INV-1",
                "PAID"
        );
    }

    @Test
    public void testConstructorCreatesRentalFile() {
        assertFalse(Files.exists(rentalFile));

        new RentalRepository();

        assertTrue(Files.exists(rentalFile));
    }

    @Test
    public void testFindAllInitiallyEmpty() {
        RentalRepository repository = new RentalRepository();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testSaveAndFindAll() {
        RentalRepository repository = new RentalRepository();

        RentalRecord rental = createRental(
                "RNT-1",
                "hamada@email.com",
                "V001",
                "ACTIVE",
                0.0
        );

        repository.save(rental);

        List<RentalRecord> rentals = repository.findAll();

        assertEquals(1, rentals.size());

        RentalRecord loaded = rentals.get(0);

        assertEquals("RNT-1", loaded.rentalId());
        assertEquals("hamada@email.com", loaded.customerEmail());
        assertEquals("Hamada Ahmad", loaded.customerName());
        assertEquals("V001", loaded.vehicleId());
        assertEquals("BMW X5", loaded.vehicleName());
        assertEquals("ACTIVE", loaded.status());
        assertEquals(2, loaded.rentalDays());
        assertEquals(250.0, loaded.dailyRate(), 0.0001);
        assertEquals(500.0, loaded.baseAmount(), 0.0001);
        assertEquals(0.0, loaded.lateFee(), 0.0001);
        assertEquals("INV-1", loaded.invoiceId());
        assertEquals("PAID", loaded.paymentStatus());
    }

    @Test
    public void testFindAllSkipsMalformedLines() throws Exception {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        Files.writeString(
                rentalFile,
                "BAD\tLINE" + System.lineSeparator()
                        + "OTHER\t1\t2\t3\t4\t5\t6\t7\t8\t9\t10\t11\t12\t13\t14\t15"
                        + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<RentalRecord> rentals = repository.findAll();

        assertEquals(1, rentals.size());
        assertEquals("RNT-1", rentals.get(0).rentalId());
    }

    @Test
    public void testFindByCustomerEmailNormalizesEmail() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        repository.save(
                createRental(
                        "RNT-2",
                        "other@email.com",
                        "V002",
                        "RETURNED",
                        0.0
                )
        );

        List<RentalRecord> result =
                repository.findByCustomerEmail(
                        "  HAMADA@EMAIL.COM  "
                );

        assertEquals(1, result.size());
        assertEquals("RNT-1", result.get(0).rentalId());
    }

    @Test
    public void testFindByCustomerEmailNullReturnsEmpty() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        assertTrue(
                repository.findByCustomerEmail(null).isEmpty()
        );
    }

    @Test
    public void testFindByIdIsCaseInsensitive() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-ABC",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        Optional<RentalRecord> result =
                repository.findById("rnt-abc");

        assertTrue(result.isPresent());
        assertEquals("RNT-ABC", result.get().rentalId());
    }

    @Test
    public void testFindByIdNotFound() {
        RentalRepository repository = new RentalRepository();

        assertTrue(
                repository.findById("RNT-MISSING").isEmpty()
        );
    }

    @Test
    public void testHasActiveRentalForVehicleActive() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        " active ",
                        0.0
                )
        );

        assertTrue(
                repository.hasActiveRentalForVehicle("v001")
        );
    }

    @Test
    public void testHasActiveRentalForVehicleReturnDue() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "RETURN_DUE",
                        0.0
                )
        );

        assertTrue(
                repository.hasActiveRentalForVehicle("V001")
        );
    }

    @Test
    public void testHasActiveRentalForVehicleOverdue() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "OVERDUE",
                        20.0
                )
        );

        assertTrue(
                repository.hasActiveRentalForVehicle("V001")
        );
    }

    @Test
    public void testHasActiveRentalForVehicleLate() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "LATE",
                        20.0
                )
        );

        assertTrue(
                repository.hasActiveRentalForVehicle("V001")
        );
    }

    @Test
    public void testHasActiveRentalForVehicleReturnedIsFalse() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "RETURNED",
                        20.0
                )
        );

        assertFalse(
                repository.hasActiveRentalForVehicle("V001")
        );
    }

    @Test
    public void testHasActiveRentalForVehicleNullStatusIsFalse() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        null,
                        0.0
                )
        );

        assertFalse(
                repository.hasActiveRentalForVehicle("V001")
        );
    }

    @Test
    public void testActiveRentalForDifferentVehicleIsFalse() {
        RentalRepository repository = new RentalRepository();
        repository.save(createRental(
                "RNT-1",
                "hamada@email.com",
                "V-OTHER",
                "ACTIVE",
                0.0
        ));

        assertFalse(repository.hasActiveRentalForVehicle("V001"));
    }

    @Test
    public void testCreateRental() {
        RentalRepository repository = new RentalRepository();

        LocalDateTime start =
                LocalDateTime.of(2026, 7, 14, 10, 0);

        LocalDateTime expectedReturn =
                LocalDateTime.of(2026, 7, 17, 10, 0);

        RentalRecord result = repository.create(
                "  HAMADA@EMAIL.COM  ",
                "Hamada Ahmad",
                "V001",
                "BMW X5",
                start,
                expectedReturn,
                3,
                250.0,
                750.0,
                "INV-1",
                "PAID"
        );

        assertNotNull(result.rentalId());
        assertTrue(result.rentalId().startsWith("RNT-"));
        assertEquals(
                "hamada@email.com",
                result.customerEmail()
        );
        assertEquals("Hamada Ahmad", result.customerName());
        assertEquals("V001", result.vehicleId());
        assertEquals("BMW X5", result.vehicleName());
        assertEquals(start.toString(), result.startDateTime());
        assertEquals(
                expectedReturn.toString(),
                result.expectedReturnDateTime()
        );
        assertEquals("", result.actualReturnDateTime());
        assertEquals("ACTIVE", result.status());
        assertEquals(3, result.rentalDays());
        assertEquals(250.0, result.dailyRate(), 0.0001);
        assertEquals(750.0, result.baseAmount(), 0.0001);
        assertEquals(0.0, result.lateFee(), 0.0001);
        assertEquals("INV-1", result.invoiceId());
        assertEquals("PAID", result.paymentStatus());

        assertTrue(
                repository.findById(result.rentalId()).isPresent()
        );
    }

    @Test
    public void testCreateRentalWithNullEmailNormalizesToEmpty() {
        RentalRepository repository = new RentalRepository();

        LocalDateTime start =
                LocalDateTime.of(2026, 7, 14, 10, 0);

        RentalRecord result = repository.create(
                null,
                "Guest",
                "V001",
                "BMW X5",
                start,
                start.plusDays(1),
                1,
                250.0,
                250.0,
                "INV-1",
                "PAID"
        );

        assertEquals("", result.customerEmail());
    }

    @Test
    public void testUpdateStatusSuccess() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        boolean result = repository.updateStatus(
                "rnt-1",
                " returned ",
                "2026-07-16T10:30:00",
                25.5
        );

        assertTrue(result);

        RentalRecord updated =
                repository.findById("RNT-1").orElseThrow();

        assertEquals("RETURNED", updated.status());
        assertEquals(
                "2026-07-16T10:30:00",
                updated.actualReturnDateTime()
        );
        assertEquals(25.5, updated.lateFee(), 0.0001);
    }

    @Test
    public void testUpdateStatusNullActualReturnAndNegativeLateFee() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        5.0
                )
        );

        assertTrue(
                repository.updateStatus(
                        "RNT-1",
                        null,
                        null,
                        -20.0
                )
        );

        RentalRecord updated =
                repository.findById("RNT-1").orElseThrow();

        assertEquals("", updated.status());
        assertEquals("", updated.actualReturnDateTime());
        assertEquals(0.0, updated.lateFee(), 0.0001);
    }

    @Test
    public void testUpdateStatusNotFound() {
        RentalRepository repository = new RentalRepository();

        assertFalse(
                repository.updateStatus(
                        "RNT-MISSING",
                        "RETURNED",
                        LocalDateTime.now().toString(),
                        0.0
                )
        );
    }

    @Test
    public void testUpdateTimingStatusSuccess() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        assertTrue(
                repository.updateTimingStatus(
                        "RNT-1",
                        " overdue ",
                        35.0
                )
        );

        RentalRecord updated =
                repository.findById("RNT-1").orElseThrow();

        assertEquals("OVERDUE", updated.status());
        assertEquals(35.0, updated.lateFee(), 0.0001);
    }

    @Test
    public void testUpdateTimingStatusNegativeLateFeeBecomesZero() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        10.0
                )
        );

        assertTrue(
                repository.updateTimingStatus(
                        "RNT-1",
                        "RETURN_DUE",
                        -5.0
                )
        );

        assertEquals(
                0.0,
                repository.findById("RNT-1")
                        .orElseThrow()
                        .lateFee(),
                0.0001
        );
    }

    @Test
    public void testUpdateTimingStatusNotFound() {
        RentalRepository repository = new RentalRepository();

        assertFalse(
                repository.updateTimingStatus(
                        "RNT-MISSING",
                        "OVERDUE",
                        10.0
                )
        );
    }

    @Test
    public void testUpdateLateFeeSuccess() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "OVERDUE",
                        5.0
                )
        );

        assertTrue(
                repository.updateLateFee(
                        "RNT-1",
                        45.5
                )
        );

        assertEquals(
                45.5,
                repository.findById("RNT-1")
                        .orElseThrow()
                        .lateFee(),
                0.0001
        );
    }

    @Test
    public void testUpdateLateFeeNegativeBecomesZero() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "OVERDUE",
                        15.0
                )
        );

        assertTrue(
                repository.updateLateFee(
                        "RNT-1",
                        -100.0
                )
        );

        assertEquals(
                0.0,
                repository.findById("RNT-1")
                        .orElseThrow()
                        .lateFee(),
                0.0001
        );
    }

    @Test
    public void testUpdateLateFeeNotFound() {
        RentalRepository repository = new RentalRepository();

        assertFalse(
                repository.updateLateFee(
                        "RNT-MISSING",
                        20.0
                )
        );
    }

    @Test
    public void testMarkReturnedWithProvidedDate() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        LocalDateTime actualReturn =
                LocalDateTime.of(2026, 7, 16, 12, 30);

        assertTrue(
                repository.markReturned(
                        "RNT-1",
                        actualReturn,
                        30.0
                )
        );

        RentalRecord updated =
                repository.findById("RNT-1").orElseThrow();

        assertEquals("RETURNED", updated.status());
        assertEquals(
                actualReturn.toString(),
                updated.actualReturnDateTime()
        );
        assertEquals(30.0, updated.lateFee(), 0.0001);
    }

    @Test
    public void testMarkReturnedWithNullDateUsesCurrentTime() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "hamada@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        LocalDateTime before = LocalDateTime.now();

        assertTrue(
                repository.markReturned(
                        "RNT-1",
                        null,
                        0.0
                )
        );

        LocalDateTime after = LocalDateTime.now();

        RentalRecord updated =
                repository.findById("RNT-1").orElseThrow();

        LocalDateTime saved =
                LocalDateTime.parse(
                        updated.actualReturnDateTime()
                );

        assertFalse(saved.isBefore(before));
        assertFalse(saved.isAfter(after));
        assertEquals("RETURNED", updated.status());
    }

    @Test
    public void testMarkReturnedNotFound() {
        RentalRepository repository = new RentalRepository();

        assertFalse(
                repository.markReturned(
                        "RNT-MISSING",
                        LocalDateTime.now(),
                        0.0
                )
        );
    }

    @Test
    public void testReplacePreservesOtherRentals() {
        RentalRepository repository = new RentalRepository();

        repository.save(
                createRental(
                        "RNT-1",
                        "one@email.com",
                        "V001",
                        "ACTIVE",
                        0.0
                )
        );

        repository.save(
                createRental(
                        "RNT-2",
                        "two@email.com",
                        "V002",
                        "ACTIVE",
                        0.0
                )
        );

        assertTrue(
                repository.updateLateFee(
                        "RNT-1",
                        50.0
                )
        );

        List<RentalRecord> rentals = repository.findAll();

        assertEquals(2, rentals.size());

        assertEquals(
                50.0,
                repository.findById("RNT-1")
                        .orElseThrow()
                        .lateFee(),
                0.0001
        );

        assertEquals(
                0.0,
                repository.findById("RNT-2")
                        .orElseThrow()
                        .lateFee(),
                0.0001
        );
    }

    @Test
    public void testRentalRecordAccessors() {
        RentalRecord rental = createRental(
                "RNT-1",
                "hamada@email.com",
                "V001",
                "ACTIVE",
                10.0
        );

        assertEquals("RNT-1", rental.rentalId());
        assertEquals(
                "hamada@email.com",
                rental.customerEmail()
        );
        assertEquals("Hamada Ahmad", rental.customerName());
        assertEquals("V001", rental.vehicleId());
        assertEquals("BMW X5", rental.vehicleName());
        assertEquals(
                "2026-07-14T10:00:00",
                rental.startDateTime()
        );
        assertEquals(
                "2026-07-16T10:00:00",
                rental.expectedReturnDateTime()
        );
        assertEquals("", rental.actualReturnDateTime());
        assertEquals("ACTIVE", rental.status());
        assertEquals(2, rental.rentalDays());
        assertEquals(250.0, rental.dailyRate(), 0.0001);
        assertEquals(500.0, rental.baseAmount(), 0.0001);
        assertEquals(10.0, rental.lateFee(), 0.0001);
        assertEquals("INV-1", rental.invoiceId());
        assertEquals("PAID", rental.paymentStatus());
    }

    private Path getRentalFile() throws Exception {
        Field field = RentalRepository.class.getDeclaredField("FILE");
        field.setAccessible(true);
        return (Path) field.get(null);
    }
}
