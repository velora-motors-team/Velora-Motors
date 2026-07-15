package com.velora.repository;

import com.velora.repository.PaymentRepository.PaymentRecord;

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

public class PaymentRepositoryTest {

    private Path paymentFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        paymentFile = getPaymentFile();

        originalFileExisted = Files.exists(paymentFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(paymentFile)
                : null;

        Files.deleteIfExists(paymentFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(paymentFile);

        if (originalFileExisted) {
            Files.createDirectories(paymentFile.getParent());
            Files.write(paymentFile, originalFileContent);
        }
    }

    @Test
    public void testConstructorCreatesPaymentFile() {
        assertFalse(Files.exists(paymentFile));

        new PaymentRepository();

        assertTrue(Files.exists(paymentFile));
    }

    @Test
    public void testFindAllInitiallyEmpty() {
        PaymentRepository repository = new PaymentRepository();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testCreateAndFindAll() {
        PaymentRepository repository = new PaymentRepository();

        PaymentRecord created = repository.create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                550.0,
                "CARD",
                "COMPLETED"
        );

        List<PaymentRecord> payments = repository.findAll();

        assertEquals(1, payments.size());

        PaymentRecord loaded = payments.get(0);

        assertEquals(created.paymentId(), loaded.paymentId());
        assertEquals("INV-1", loaded.invoiceId());
        assertEquals("RNT-1", loaded.rentalId());
        assertEquals("hamada@email.com", loaded.customerEmail());
        assertEquals(550.0, loaded.amount(), 0.0001);
        assertEquals("CARD", loaded.method());
        assertEquals("COMPLETED", loaded.status());
        assertNotNull(loaded.createdAt());
    }

    @Test
    public void testCreateGeneratesPaymentId() {
        PaymentRepository repository = new PaymentRepository();

        PaymentRecord result = repository.create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                100.0,
                "CASH",
                "COMPLETED"
        );

        assertNotNull(result.paymentId());
        assertTrue(result.paymentId().startsWith("PAY-"));
    }

    @Test
    public void testCreateNormalizesEmail() {
        PaymentRepository repository = new PaymentRepository();

        PaymentRecord result = repository.create(
                "INV-1",
                "RNT-1",
                "  HAMADA@EMAIL.COM  ",
                100.0,
                "CARD",
                "COMPLETED"
        );

        assertEquals(
                "hamada@email.com",
                result.customerEmail()
        );
    }

    @Test
    public void testCreateWithNullEmailUsesEmptyString() {
        PaymentRepository repository = new PaymentRepository();

        PaymentRecord result = repository.create(
                "INV-1",
                "RNT-1",
                null,
                100.0,
                "CARD",
                "COMPLETED"
        );

        assertEquals("", result.customerEmail());
    }

    @Test
    public void testCreatedAtUsesCurrentTime() {
        PaymentRepository repository = new PaymentRepository();

        LocalDateTime before = LocalDateTime.now();

        PaymentRecord result = repository.create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                100.0,
                "CARD",
                "COMPLETED"
        );

        LocalDateTime after = LocalDateTime.now();

        LocalDateTime createdAt =
                LocalDateTime.parse(result.createdAt());

        assertFalse(createdAt.isBefore(before));
        assertFalse(createdAt.isAfter(after));
    }

    @Test
    public void testFindByCustomerEmailNormalizesEmail() {
        PaymentRepository repository = new PaymentRepository();

        repository.create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                100.0,
                "CARD",
                "COMPLETED"
        );

        repository.create(
                "INV-2",
                "RNT-2",
                "other@email.com",
                200.0,
                "CASH",
                "COMPLETED"
        );

        List<PaymentRecord> result =
                repository.findByCustomerEmail(
                        "  HAMADA@EMAIL.COM  "
                );

        assertEquals(1, result.size());
        assertEquals("INV-1", result.get(0).invoiceId());
    }

    @Test
    public void testFindByCustomerEmailNullMatchesEmptyEmail() {
        PaymentRepository repository = new PaymentRepository();

        repository.create(
                "INV-1",
                "RNT-1",
                null,
                100.0,
                "CARD",
                "COMPLETED"
        );

        List<PaymentRecord> result =
                repository.findByCustomerEmail(null);

        assertEquals(1, result.size());
        assertEquals("", result.get(0).customerEmail());
    }

    @Test
    public void testFindByCustomerEmailNotFound() {
        PaymentRepository repository = new PaymentRepository();

        repository.create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                100.0,
                "CARD",
                "COMPLETED"
        );

        assertTrue(
                repository.findByCustomerEmail(
                        "missing@email.com"
                ).isEmpty()
        );
    }

    @Test
    public void testFindAllSkipsMalformedLines() throws Exception {
        PaymentRepository repository = new PaymentRepository();

        repository.create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                100.0,
                "CARD",
                "COMPLETED"
        );

        Files.writeString(
                paymentFile,
                "BAD\tLINE" + System.lineSeparator()
                        + "OTHER\t1\t2\t3\t4\t5\t6\t7\t8"
                        + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<PaymentRecord> result = repository.findAll();

        assertEquals(1, result.size());
        assertEquals("INV-1", result.get(0).invoiceId());
    }

    @Test
    public void testMultiplePaymentsAreStored() {
        PaymentRepository repository = new PaymentRepository();

        repository.create(
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                100.0,
                "CARD",
                "COMPLETED"
        );

        repository.create(
                "INV-2",
                "RNT-2",
                "hamada@email.com",
                250.0,
                "CASH",
                "COMPLETED"
        );

        repository.create(
                "INV-3",
                "RNT-3",
                "other@email.com",
                300.0,
                "BANK TRANSFER",
                "PENDING"
        );

        List<PaymentRecord> result = repository.findAll();

        assertEquals(3, result.size());
    }

    @Test
    public void testSpecialCharactersArePreserved() {
        PaymentRepository repository = new PaymentRepository();

        PaymentRecord created = repository.create(
                "INV-特殊-1",
                "RNT-1",
                "special@email.com",
                123.45,
                "BANK TRANSFER / CARD",
                "COMPLETED + VERIFIED"
        );

        PaymentRecord loaded = repository.findAll().get(0);

        assertEquals(created.invoiceId(), loaded.invoiceId());
        assertEquals(
                "BANK TRANSFER / CARD",
                loaded.method()
        );
        assertEquals(
                "COMPLETED + VERIFIED",
                loaded.status()
        );
    }

    @Test
    public void testPaymentRecordAccessors() {
        PaymentRecord record = new PaymentRecord(
                "PAY-1",
                "INV-1",
                "RNT-1",
                "hamada@email.com",
                550.0,
                "CARD",
                "COMPLETED",
                "2026-07-15T10:00:00"
        );

        assertEquals("PAY-1", record.paymentId());
        assertEquals("INV-1", record.invoiceId());
        assertEquals("RNT-1", record.rentalId());
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
        assertEquals(550.0, record.amount(), 0.0001);
        assertEquals("CARD", record.method());
        assertEquals("COMPLETED", record.status());
        assertEquals(
                "2026-07-15T10:00:00",
                record.createdAt()
        );
    }

    private Path getPaymentFile() throws Exception {
        Field field =
                PaymentRepository.class.getDeclaredField("FILE");

        field.setAccessible(true);

        return (Path) field.get(null);
    }
}