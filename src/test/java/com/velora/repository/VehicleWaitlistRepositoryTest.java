package com.velora.repository;

import com.velora.repository.VehicleWaitlistRepository.WaitlistRecord;

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

public class VehicleWaitlistRepositoryTest {

    private Path waitlistFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        waitlistFile = getWaitlistFile();

        originalFileExisted = Files.exists(waitlistFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(waitlistFile)
                : null;

        Files.deleteIfExists(waitlistFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(waitlistFile);

        if (originalFileExisted) {
            Files.createDirectories(waitlistFile.getParent());
            Files.write(waitlistFile, originalFileContent);
        }
    }

    @Test
    public void testConstructorCreatesWaitlistFile() {
        assertFalse(Files.exists(waitlistFile));

        new VehicleWaitlistRepository();

        assertTrue(Files.exists(waitlistFile));
    }

    @Test
    public void testFindAllInitiallyEmpty() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testSubscribeSuccess() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        boolean result = repository.subscribe(
                "vm-0001",
                "Hamada Ahmad",
                "HAMADA@EMAIL.COM"
        );

        assertTrue(result);

        List<WaitlistRecord> records = repository.findAll();

        assertEquals(1, records.size());

        WaitlistRecord record = records.get(0);

        assertEquals("VM-0001", record.vehicleId());
        assertEquals("Hamada Ahmad", record.customerName());
        assertEquals("hamada@email.com", record.customerEmail());
        assertNotNull(record.subscribedAt());
        assertFalse(record.subscribedAt().isBlank());
    }

    @Test
    public void testSubscribeStoresCurrentTime() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        LocalDateTime before = LocalDateTime.now();

        assertTrue(
                repository.subscribe(
                        "V001",
                        "Hamada",
                        "hamada@email.com"
                )
        );

        LocalDateTime after = LocalDateTime.now();

        LocalDateTime subscribedAt =
                LocalDateTime.parse(
                        repository.findAll()
                                .get(0)
                                .subscribedAt()
                );

        assertFalse(subscribedAt.isBefore(before));
        assertFalse(subscribedAt.isAfter(after));
    }

    @Test
    public void testSubscribeRejectsBlankVehicleId() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertFalse(
                repository.subscribe(
                        "   ",
                        "Hamada",
                        "hamada@email.com"
                )
        );

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testSubscribeRejectsNullVehicleId() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertFalse(
                repository.subscribe(
                        null,
                        "Hamada",
                        "hamada@email.com"
                )
        );
    }

    @Test
    public void testSubscribeRejectsBlankEmail() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertFalse(
                repository.subscribe(
                        "V001",
                        "Hamada",
                        "   "
                )
        );
    }

    @Test
    public void testSubscribeRejectsNullEmail() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertFalse(
                repository.subscribe(
                        "V001",
                        "Hamada",
                        null
                )
        );
    }

    @Test
    public void testSubscribeRejectsDuplicateCustomerForSameVehicle() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertTrue(
                repository.subscribe(
                        "v001",
                        "Hamada",
                        "hamada@email.com"
                )
        );

        assertFalse(
                repository.subscribe(
                        " V001 ",
                        "Hamada Again",
                        " HAMADA@EMAIL.COM "
                )
        );

        assertEquals(1, repository.findAll().size());
    }

    @Test
    public void testSameCustomerCanSubscribeToDifferentVehicles() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertTrue(
                repository.subscribe(
                        "V001",
                        "Hamada",
                        "hamada@email.com"
                )
        );

        assertTrue(
                repository.subscribe(
                        "V002",
                        "Hamada",
                        "hamada@email.com"
                )
        );

        assertEquals(2, repository.findAll().size());
    }

    @Test
    public void testDifferentCustomersCanSubscribeToSameVehicle() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertTrue(
                repository.subscribe(
                        "V001",
                        "Hamada",
                        "hamada@email.com"
                )
        );

        assertTrue(
                repository.subscribe(
                        "V001",
                        "Sara",
                        "sara@email.com"
                )
        );

        assertEquals(2, repository.findAll().size());
    }

    @Test
    public void testCustomerNameIsSanitized() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertTrue(
                repository.subscribe(
                        "V001",
                        "  Hamada\tAhmad\nStudent  ",
                        "hamada@email.com"
                )
        );

        WaitlistRecord record =
                repository.findAll().get(0);

        assertEquals(
                "Hamada Ahmad Student",
                record.customerName()
        );
    }

    @Test
    public void testNullCustomerNameBecomesEmpty() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        assertTrue(
                repository.subscribe(
                        "V001",
                        null,
                        "hamada@email.com"
                )
        );

        assertEquals(
                "",
                repository.findAll()
                        .get(0)
                        .customerName()
        );
    }

    @Test
    public void testFindByVehicleIdNormalizesVehicleId() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        repository.subscribe(
                "V002",
                "Sara",
                "sara@email.com"
        );

        List<WaitlistRecord> result =
                repository.findByVehicleId(
                        "  v001  "
                );

        assertEquals(1, result.size());
        assertEquals("V001", result.get(0).vehicleId());
    }

    @Test
    public void testFindByVehicleIdNullReturnsEmpty() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        assertTrue(
                repository.findByVehicleId(null).isEmpty()
        );
    }

    @Test
    public void testIsSubscribedReturnsTrue() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        assertTrue(
                repository.isSubscribed(
                        " v001 ",
                        " HAMADA@EMAIL.COM "
                )
        );
    }

    @Test
    public void testIsSubscribedReturnsFalse() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        assertFalse(
                repository.isSubscribed(
                        "V001",
                        "missing@email.com"
                )
        );
    }

    @Test
    public void testUnsubscribeSuccess() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        assertTrue(
                repository.unsubscribe(
                        " v001 ",
                        " HAMADA@EMAIL.COM "
                )
        );

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testUnsubscribeNotFoundReturnsFalse() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        assertFalse(
                repository.unsubscribe(
                        "V001",
                        "missing@email.com"
                )
        );

        assertEquals(1, repository.findAll().size());
    }

    @Test
    public void testUnsubscribePreservesOtherRecords() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        repository.subscribe(
                "V001",
                "Sara",
                "sara@email.com"
        );

        repository.subscribe(
                "V002",
                "Ali",
                "ali@email.com"
        );

        assertTrue(
                repository.unsubscribe(
                        "V001",
                        "hamada@email.com"
                )
        );

        List<WaitlistRecord> remaining =
                repository.findAll();

        assertEquals(2, remaining.size());

        assertTrue(
                remaining.stream().anyMatch(
                        record -> record.customerEmail()
                                .equals("sara@email.com")
                )
        );

        assertTrue(
                remaining.stream().anyMatch(
                        record -> record.customerEmail()
                                .equals("ali@email.com")
                )
        );
    }

    @Test
    public void testRemoveByVehicleIdRemovesAllMatchingRecords() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        repository.subscribe(
                "V001",
                "Sara",
                "sara@email.com"
        );

        repository.subscribe(
                "V002",
                "Ali",
                "ali@email.com"
        );

        repository.removeByVehicleId(" v001 ");

        List<WaitlistRecord> remaining =
                repository.findAll();

        assertEquals(1, remaining.size());
        assertEquals("V002", remaining.get(0).vehicleId());
    }

    @Test
    public void testRemoveByVehicleIdNotFoundKeepsRecords() {
        VehicleWaitlistRepository repository =
                new VehicleWaitlistRepository();

        repository.subscribe(
                "V001",
                "Hamada",
                "hamada@email.com"
        );

        repository.removeByVehicleId("V999");

        assertEquals(1, repository.findAll().size());
    }

    @Test
    public void testFindAllSupportsOldThreeFieldFormat() throws Exception {
        new VehicleWaitlistRepository();

        String oldFormatLine =
                "V001\thamada@email.com\t2026-07-15T10:00:00";

        Files.writeString(
                waitlistFile,
                oldFormatLine + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<WaitlistRecord> result =
                new VehicleWaitlistRepository().findAll();

        assertEquals(1, result.size());

        WaitlistRecord record = result.get(0);

        assertEquals("V001", record.vehicleId());
        assertEquals("", record.customerName());
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
        assertEquals(
                "2026-07-15T10:00:00",
                record.subscribedAt()
        );
    }

    @Test
    public void testFindAllReadsFourFieldFormat() throws Exception {
        new VehicleWaitlistRepository();

        String line =
                "V001\tHamada Ahmad\tHAMADA@EMAIL.COM\t2026-07-15T10:00:00";

        Files.writeString(
                waitlistFile,
                line + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<WaitlistRecord> result =
                new VehicleWaitlistRepository().findAll();

        assertEquals(1, result.size());

        WaitlistRecord record = result.get(0);

        assertEquals("V001", record.vehicleId());
        assertEquals("Hamada Ahmad", record.customerName());
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
    }

    @Test
    public void testFindAllSkipsMalformedLines() throws Exception {
        new VehicleWaitlistRepository();

        Files.writeString(
                waitlistFile,
                "BAD" + System.lineSeparator()
                        + "ONE\tTWO"
                        + System.lineSeparator()
                        + "A\tB\tC\tD\tE"
                        + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        assertTrue(
                new VehicleWaitlistRepository()
                        .findAll()
                        .isEmpty()
        );
    }

    @Test
    public void testWaitlistRecordAccessors() {
        WaitlistRecord record = new WaitlistRecord(
                "V001",
                "Hamada",
                "hamada@email.com",
                "2026-07-15T10:00:00"
        );

        assertEquals("V001", record.vehicleId());
        assertEquals("Hamada", record.customerName());
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
        assertEquals(
                "2026-07-15T10:00:00",
                record.subscribedAt()
        );
    }

    private Path getWaitlistFile() throws Exception {
        Field field =
                VehicleWaitlistRepository.class
                        .getDeclaredField("FILE");

        field.setAccessible(true);

        return (Path) field.get(null);
    }
}