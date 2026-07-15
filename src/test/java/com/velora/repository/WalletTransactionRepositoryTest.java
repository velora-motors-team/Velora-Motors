package com.velora.repository;

import com.velora.repository.WalletTransactionRepository.WalletTransaction;

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

public class WalletTransactionRepositoryTest {

    private Path walletFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        walletFile = getWalletFile();

        originalFileExisted = Files.exists(walletFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(walletFile)
                : null;

        Files.deleteIfExists(walletFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(walletFile);

        if (originalFileExisted) {
            Files.createDirectories(walletFile.getParent());
            Files.write(walletFile, originalFileContent);
        }
    }

    @Test
    public void testConstructorCreatesWalletFile() {
        assertFalse(Files.exists(walletFile));

        new WalletTransactionRepository();

        assertTrue(Files.exists(walletFile));
    }

    @Test
    public void testFindAllInitiallyEmpty() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testAddAndFindAll() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        WalletTransaction created = repository.add(
                "hamada@email.com",
                "DEPOSIT",
                500.0,
                500.0,
                "REF-1"
        );

        List<WalletTransaction> transactions =
                repository.findAll();

        assertEquals(1, transactions.size());

        WalletTransaction loaded = transactions.get(0);

        assertEquals(
                created.transactionId(),
                loaded.transactionId()
        );
        assertEquals(
                "hamada@email.com",
                loaded.customerEmail()
        );
        assertEquals("DEPOSIT", loaded.type());
        assertEquals(500.0, loaded.amount(), 0.0001);
        assertEquals(
                500.0,
                loaded.balanceAfter(),
                0.0001
        );
        assertEquals("REF-1", loaded.referenceId());
        assertNotNull(loaded.createdAt());
    }

    @Test
    public void testAddGeneratesTransactionId() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        WalletTransaction result = repository.add(
                "hamada@email.com",
                "DEPOSIT",
                100.0,
                100.0,
                "REF-1"
        );

        assertNotNull(result.transactionId());
        assertTrue(
                result.transactionId().startsWith("WAL-")
        );
    }

    @Test
    public void testAddNormalizesEmail() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        WalletTransaction result = repository.add(
                "  HAMADA@EMAIL.COM  ",
                "DEPOSIT",
                100.0,
                100.0,
                "REF-1"
        );

        assertEquals(
                "hamada@email.com",
                result.customerEmail()
        );
    }

    @Test
    public void testAddNullEmailBecomesEmpty() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        WalletTransaction result = repository.add(
                null,
                "DEPOSIT",
                100.0,
                100.0,
                "REF-1"
        );

        assertEquals("", result.customerEmail());
    }

    @Test
    public void testCreatedAtUsesCurrentTime() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        LocalDateTime before = LocalDateTime.now();

        WalletTransaction result = repository.add(
                "hamada@email.com",
                "DEPOSIT",
                100.0,
                100.0,
                "REF-1"
        );

        LocalDateTime after = LocalDateTime.now();

        LocalDateTime createdAt =
                LocalDateTime.parse(result.createdAt());

        assertFalse(createdAt.isBefore(before));
        assertFalse(createdAt.isAfter(after));
    }

    @Test
    public void testFindByCustomerEmailNormalizesEmail() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                "hamada@email.com",
                "DEPOSIT",
                500.0,
                500.0,
                "REF-1"
        );

        repository.add(
                "other@email.com",
                "DEPOSIT",
                300.0,
                300.0,
                "REF-2"
        );

        List<WalletTransaction> result =
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
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                null,
                "DEPOSIT",
                50.0,
                50.0,
                "REF-1"
        );

        List<WalletTransaction> result =
                repository.findByCustomerEmail(null);

        assertEquals(1, result.size());
        assertEquals("", result.get(0).customerEmail());
    }

    @Test
    public void testFindByCustomerEmailNotFound() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                "hamada@email.com",
                "DEPOSIT",
                100.0,
                100.0,
                "REF-1"
        );

        assertTrue(
                repository.findByCustomerEmail(
                        "missing@email.com"
                ).isEmpty()
        );
    }

    @Test
    public void testPositiveAndNegativeAmountsArePreserved() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                "hamada@email.com",
                "DEPOSIT",
                500.0,
                500.0,
                "DEP-1"
        );

        repository.add(
                "hamada@email.com",
                "PAYMENT",
                -150.0,
                350.0,
                "PAY-1"
        );

        List<WalletTransaction> result =
                repository.findByCustomerEmail(
                        "hamada@email.com"
                );

        assertEquals(2, result.size());
        assertEquals(
                500.0,
                result.get(0).amount(),
                0.0001
        );
        assertEquals(
                -150.0,
                result.get(1).amount(),
                0.0001
        );
        assertEquals(
                350.0,
                result.get(1).balanceAfter(),
                0.0001
        );
    }

    @Test
    public void testZeroAmountIsPreserved() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                "hamada@email.com",
                "ADJUSTMENT",
                0.0,
                100.0,
                "ADJ-1"
        );

        WalletTransaction result =
                repository.findAll().get(0);

        assertEquals(0.0, result.amount(), 0.0001);
        assertEquals(
                100.0,
                result.balanceAfter(),
                0.0001
        );
    }

    @Test
    public void testFindAllSkipsMalformedLines() throws Exception {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                "hamada@email.com",
                "DEPOSIT",
                100.0,
                100.0,
                "REF-1"
        );

        Files.writeString(
                walletFile,
                "BAD\tLINE" + System.lineSeparator()
                        + "OTHER\t1\t2\t3\t4\t5\t6\t7"
                        + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<WalletTransaction> result =
                repository.findAll();

        assertEquals(1, result.size());
        assertEquals("DEPOSIT", result.get(0).type());
    }

    @Test
    public void testSpecialCharactersArePreserved() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                "special@email.com",
                "BONUS / إضافة",
                75.5,
                275.5,
                "REF-特殊-1 | BMW"
        );

        WalletTransaction loaded =
                repository.findAll().get(0);

        assertEquals(
                "BONUS / إضافة",
                loaded.type()
        );
        assertEquals(
                "REF-特殊-1 | BMW",
                loaded.referenceId()
        );
        assertEquals(
                75.5,
                loaded.amount(),
                0.0001
        );
    }

    @Test
    public void testMultipleTransactionsAreStored() {
        WalletTransactionRepository repository =
                new WalletTransactionRepository();

        repository.add(
                "hamada@email.com",
                "DEPOSIT",
                500.0,
                500.0,
                "REF-1"
        );

        repository.add(
                "hamada@email.com",
                "PAYMENT",
                -100.0,
                400.0,
                "REF-2"
        );

        repository.add(
                "other@email.com",
                "DEPOSIT",
                300.0,
                300.0,
                "REF-3"
        );

        assertEquals(3, repository.findAll().size());
    }

    @Test
    public void testWalletTransactionRecordAccessors() {
        WalletTransaction record =
                new WalletTransaction(
                        "WAL-1",
                        "hamada@email.com",
                        "DEPOSIT",
                        500.0,
                        500.0,
                        "REF-1",
                        "2026-07-15T10:00:00"
                );

        assertEquals(
                "WAL-1",
                record.transactionId()
        );
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
        assertEquals("DEPOSIT", record.type());
        assertEquals(
                500.0,
                record.amount(),
                0.0001
        );
        assertEquals(
                500.0,
                record.balanceAfter(),
                0.0001
        );
        assertEquals(
                "REF-1",
                record.referenceId()
        );
        assertEquals(
                "2026-07-15T10:00:00",
                record.createdAt()
        );
    }

    private Path getWalletFile() throws Exception {
        Field field =
                WalletTransactionRepository.class
                        .getDeclaredField("FILE");

        field.setAccessible(true);

        return (Path) field.get(null);
    }
}