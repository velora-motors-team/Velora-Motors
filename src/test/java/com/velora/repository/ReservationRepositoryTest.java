package com.velora.repository;

import com.velora.repository.ReservationRepository.ReservationRecord;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationRepositoryTest {

    private Path reservationFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        reservationFile = getReservationFile();

        originalFileExisted = Files.exists(reservationFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(reservationFile)
                : null;

        Files.deleteIfExists(reservationFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(reservationFile);

        if (originalFileExisted) {
            Files.createDirectories(reservationFile.getParent());
            Files.write(reservationFile, originalFileContent);
        }
    }

    @Test
    public void testConstructorCreatesReservationFile() {
        assertFalse(Files.exists(reservationFile));

        new ReservationRepository();

        assertTrue(Files.exists(reservationFile));
    }

    @Test
    public void testFindAllInitiallyEmpty() {
        ReservationRepository repository =
                new ReservationRepository();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testCreateAndFindAll() {
        ReservationRepository repository =
                new ReservationRepository();

        LocalDateTime pickup =
                LocalDateTime.of(2026, 7, 20, 10, 0);

        LocalDateTime returned =
                LocalDateTime.of(2026, 7, 22, 10, 0);

        ReservationRecord created = repository.create(
                "hamada@email.com",
                "V001",
                pickup,
                returned
        );

        List<ReservationRecord> reservations =
                repository.findAll();

        assertEquals(1, reservations.size());

        ReservationRecord loaded = reservations.get(0);

        assertEquals(
                created.reservationId(),
                loaded.reservationId()
        );
        assertEquals(
                "hamada@email.com",
                loaded.customerEmail()
        );
        assertEquals("V001", loaded.vehicleId());
        assertEquals(
                pickup.toString(),
                loaded.pickupDateTime()
        );
        assertEquals(
                returned.toString(),
                loaded.returnDateTime()
        );
        assertEquals("CONFIRMED", loaded.status());
        assertNotNull(loaded.createdAt());
    }

    @Test
    public void testCreateGeneratesReservationId() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord result = repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );

        assertNotNull(result.reservationId());
        assertTrue(
                result.reservationId().startsWith("RES-")
        );
    }

    @Test
    public void testCreateNormalizesEmail() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord result = repository.create(
                "  HAMADA@EMAIL.COM  ",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );

        assertEquals(
                "hamada@email.com",
                result.customerEmail()
        );
    }

    @Test
    public void testCreateNullEmailBecomesEmpty() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord result = repository.create(
                null,
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );

        assertEquals("", result.customerEmail());
    }

    @Test
    public void testCreatedAtUsesCurrentTime() {
        ReservationRepository repository =
                new ReservationRepository();

        LocalDateTime before = LocalDateTime.now();

        ReservationRecord result = repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );

        LocalDateTime after = LocalDateTime.now();

        LocalDateTime createdAt =
                LocalDateTime.parse(result.createdAt());

        assertFalse(createdAt.isBefore(before));
        assertFalse(createdAt.isAfter(after));
    }

    @Test
    public void testFindByCustomerEmailNormalizesEmail() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );

        repository.create(
                "other@email.com",
                "V002",
                LocalDateTime.of(2026, 7, 22, 10, 0),
                LocalDateTime.of(2026, 7, 23, 10, 0)
        );

        List<ReservationRecord> result =
                repository.findByCustomerEmail(
                        "  HAMADA@EMAIL.COM  "
                );

        assertEquals(1, result.size());
        assertEquals(
                "hamada@email.com",
                result.get(0).customerEmail()
        );
    }

    @Test
    public void testFindByCustomerEmailNullMatchesEmptyEmail() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                null,
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );

        List<ReservationRecord> result =
                repository.findByCustomerEmail(null);

        assertEquals(1, result.size());
        assertEquals("", result.get(0).customerEmail());
    }

    @Test
    public void testFindByCustomerEmailNotFound() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 0)
        );

        assertTrue(
                repository.findByCustomerEmail(
                        "missing@email.com"
                ).isEmpty()
        );
    }

    @Test
    public void testHasOverlapReturnsTrueForConfirmedReservation() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertTrue(
                repository.hasOverlap(
                        "v001",
                        LocalDateTime.of(2026, 7, 21, 9, 0),
                        LocalDateTime.of(2026, 7, 23, 9, 0)
                )
        );
    }

    @Test
    public void testHasOverlapReturnsTrueForActiveReservation() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord created = repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertTrue(
                repository.updateStatus(
                        created.reservationId(),
                        "ACTIVE"
                )
        );

        assertTrue(
                repository.hasOverlap(
                        "V001",
                        LocalDateTime.of(2026, 7, 21, 10, 0),
                        LocalDateTime.of(2026, 7, 23, 10, 0)
                )
        );
    }

    @Test
    public void testHasOverlapIgnoresCancelledReservation() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord created = repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertTrue(
                repository.updateStatus(
                        created.reservationId(),
                        "CANCELLED"
                )
        );

        assertFalse(
                repository.hasOverlap(
                        "V001",
                        LocalDateTime.of(2026, 7, 21, 10, 0),
                        LocalDateTime.of(2026, 7, 23, 10, 0)
                )
        );
    }

    @Test
    public void testHasOverlapReturnsFalseForDifferentVehicle() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertFalse(
                repository.hasOverlap(
                        "V002",
                        LocalDateTime.of(2026, 7, 21, 10, 0),
                        LocalDateTime.of(2026, 7, 23, 10, 0)
                )
        );
    }

    @Test
    public void testHasOverlapReturnsFalseWhenPeriodEndsAtExistingStart() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertFalse(
                repository.hasOverlap(
                        "V001",
                        LocalDateTime.of(2026, 7, 18, 10, 0),
                        LocalDateTime.of(2026, 7, 20, 10, 0)
                )
        );
    }

    @Test
    public void testHasOverlapReturnsFalseWhenPeriodStartsAtExistingEnd() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertFalse(
                repository.hasOverlap(
                        "V001",
                        LocalDateTime.of(2026, 7, 22, 10, 0),
                        LocalDateTime.of(2026, 7, 24, 10, 0)
                )
        );
    }

    @Test
    public void testCreateOverlappingReservationThrowsException() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.create(
                                "sara@email.com",
                                "V001",
                                LocalDateTime.of(2026, 7, 21, 10, 0),
                                LocalDateTime.of(2026, 7, 23, 10, 0)
                        )
                );

        assertEquals(
                "Vehicle is already reserved for the selected period.",
                exception.getMessage()
        );

        assertEquals(1, repository.findAll().size());
    }

    @Test
    public void testCreateNonOverlappingReservationSucceeds() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        ReservationRecord second = repository.create(
                "sara@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 22, 10, 0),
                LocalDateTime.of(2026, 7, 24, 10, 0)
        );

        assertNotNull(second);
        assertEquals(2, repository.findAll().size());
    }

    @Test
    public void testUpdateStatusSuccess() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord created = repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        boolean result = repository.updateStatus(
                created.reservationId(),
                "CANCELLED"
        );

        assertTrue(result);

        ReservationRecord updated =
                repository.findAll().get(0);

        assertEquals("CANCELLED", updated.status());
        assertEquals(
                created.reservationId(),
                updated.reservationId()
        );
        assertEquals(
                created.customerEmail(),
                updated.customerEmail()
        );
        assertEquals(
                created.vehicleId(),
                updated.vehicleId()
        );
        assertEquals(
                created.pickupDateTime(),
                updated.pickupDateTime()
        );
        assertEquals(
                created.returnDateTime(),
                updated.returnDateTime()
        );
        assertEquals(
                created.createdAt(),
                updated.createdAt()
        );
    }

    @Test
    public void testUpdateStatusIsCaseInsensitiveForId() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord created = repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertTrue(
                repository.updateStatus(
                        created.reservationId().toLowerCase(),
                        "ACTIVE"
                )
        );
    }

    @Test
    public void testUpdateStatusNotFoundReturnsFalse() {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertFalse(
                repository.updateStatus(
                        "RES-MISSING",
                        "CANCELLED"
                )
        );
    }

    @Test
    public void testUpdateStatusPreservesOtherReservations() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord first = repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        ReservationRecord second = repository.create(
                "sara@email.com",
                "V002",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        assertTrue(
                repository.updateStatus(
                        first.reservationId(),
                        "CANCELLED"
                )
        );

        List<ReservationRecord> all = repository.findAll();

        assertEquals(2, all.size());

        ReservationRecord firstUpdated = all.stream()
                .filter(r -> r.reservationId()
                        .equals(first.reservationId()))
                .findFirst()
                .orElseThrow();

        ReservationRecord secondUnchanged = all.stream()
                .filter(r -> r.reservationId()
                        .equals(second.reservationId()))
                .findFirst()
                .orElseThrow();

        assertEquals("CANCELLED", firstUpdated.status());
        assertEquals("CONFIRMED", secondUnchanged.status());
    }

    @Test
    public void testFindAllSkipsMalformedLines() throws Exception {
        ReservationRepository repository =
                new ReservationRepository();

        repository.create(
                "hamada@email.com",
                "V001",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        Files.writeString(
                reservationFile,
                "BAD\tLINE" + System.lineSeparator()
                        + "OTHER\t1\t2\t3\t4\t5\t6\t7"
                        + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<ReservationRecord> result =
                repository.findAll();

        assertEquals(1, result.size());
        assertEquals("V001", result.get(0).vehicleId());
    }

    @Test
    public void testSpecialCharactersArePreserved() {
        ReservationRepository repository =
                new ReservationRepository();

        ReservationRecord created = repository.create(
                "special@email.com",
                "V-特殊-1 | BMW",
                LocalDateTime.of(2026, 7, 20, 10, 0),
                LocalDateTime.of(2026, 7, 22, 10, 0)
        );

        ReservationRecord loaded =
                repository.findAll().get(0);

        assertEquals(
                created.vehicleId(),
                loaded.vehicleId()
        );
        assertEquals(
                "V-特殊-1 | BMW",
                loaded.vehicleId()
        );
    }

    @Test
    public void testReservationRecordAccessors() {
        ReservationRecord record =
                new ReservationRecord(
                        "RES-1",
                        "hamada@email.com",
                        "V001",
                        "2026-07-20T10:00:00",
                        "2026-07-22T10:00:00",
                        "CONFIRMED",
                        "2026-07-15T10:00:00"
                );

        assertEquals(
                "RES-1",
                record.reservationId()
        );
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
        assertEquals("V001", record.vehicleId());
        assertEquals(
                "2026-07-20T10:00:00",
                record.pickupDateTime()
        );
        assertEquals(
                "2026-07-22T10:00:00",
                record.returnDateTime()
        );
        assertEquals("CONFIRMED", record.status());
        assertEquals(
                "2026-07-15T10:00:00",
                record.createdAt()
        );
    }

    private Path getReservationFile() throws Exception {
        Field field =
                ReservationRepository.class
                        .getDeclaredField("FILE");

        field.setAccessible(true);

        return (Path) field.get(null);
    }
}