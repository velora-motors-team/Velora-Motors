package com.velora.repository;

import com.velora.repository.LoyaltyTransactionRepository.LoyaltyTransaction;

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

public class LoyaltyTransactionRepositoryTest {

    private Path loyaltyFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        loyaltyFile = getLoyaltyFile();

        originalFileExisted = Files.exists(loyaltyFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(loyaltyFile)
                : null;

        Files.deleteIfExists(loyaltyFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(loyaltyFile);

        if (originalFileExisted) {
            Files.createDirectories(loyaltyFile.getParent());
            Files.write(loyaltyFile, originalFileContent);
        }
    }

    @Test
    public void testConstructorCreatesLoyaltyFile() {
        assertFalse(Files.exists(loyaltyFile));

        new LoyaltyTransactionRepository();

        assertTrue(Files.exists(loyaltyFile));
    }

    @Test
    public void testFindAllInitiallyEmpty() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testAddAndFindAll() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        LoyaltyTransaction created = repository.add(
                "hamada@email.com",
                50,
                "EARNED",
                "BMW X5 rental completed",
                "RNT-1"
        );

        List<LoyaltyTransaction> transactions =
                repository.findAll();

        assertEquals(1, transactions.size());

        LoyaltyTransaction loaded = transactions.get(0);

        assertEquals(
                created.transactionId(),
                loaded.transactionId()
        );
        assertEquals(
                "hamada@email.com",
                loaded.customerEmail()
        );
        assertEquals(50, loaded.points());
        assertEquals("EARNED", loaded.type());
        assertEquals(
                "BMW X5 rental completed",
                loaded.description()
        );
        assertEquals("RNT-1", loaded.referenceId());
        assertNotNull(loaded.createdAt());
    }

    @Test
    public void testAddGeneratesTransactionId() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        LoyaltyTransaction result = repository.add(
                "hamada@email.com",
                25,
                "EARNED",
                "Rental completed",
                "RNT-1"
        );

        assertNotNull(result.transactionId());
        assertTrue(
                result.transactionId().startsWith("LOY-")
        );
    }

    @Test
    public void testAddNormalizesEmail() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        LoyaltyTransaction result = repository.add(
                "  HAMADA@EMAIL.COM  ",
                25,
                "EARNED",
                "Rental completed",
                "RNT-1"
        );

        assertEquals(
                "hamada@email.com",
                result.customerEmail()
        );
    }

    @Test
    public void testAddNullEmailBecomesEmpty() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        LoyaltyTransaction result = repository.add(
                null,
                25,
                "EARNED",
                "Rental completed",
                "RNT-1"
        );

        assertEquals("", result.customerEmail());
    }

    @Test
    public void testCreatedAtUsesCurrentTime() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        LocalDateTime before = LocalDateTime.now();

        LoyaltyTransaction result = repository.add(
                "hamada@email.com",
                25,
                "EARNED",
                "Rental completed",
                "RNT-1"
        );

        LocalDateTime after = LocalDateTime.now();

        LocalDateTime createdAt =
                LocalDateTime.parse(result.createdAt());

        assertFalse(createdAt.isBefore(before));
        assertFalse(createdAt.isAfter(after));
    }

    @Test
    public void testFindByCustomerEmailNormalizesEmail() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                "hamada@email.com",
                50,
                "EARNED",
                "First transaction",
                "RNT-1"
        );

        repository.add(
                "other@email.com",
                30,
                "EARNED",
                "Second transaction",
                "RNT-2"
        );

        List<LoyaltyTransaction> result =
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
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                null,
                10,
                "EARNED",
                "Guest transaction",
                "RNT-1"
        );

        List<LoyaltyTransaction> result =
                repository.findByCustomerEmail(null);

        assertEquals(1, result.size());
        assertEquals("", result.get(0).customerEmail());
    }

    @Test
    public void testFindByCustomerEmailNotFound() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                "hamada@email.com",
                50,
                "EARNED",
                "Rental completed",
                "RNT-1"
        );

        assertTrue(
                repository.findByCustomerEmail(
                        "missing@email.com"
                ).isEmpty()
        );
    }

    @Test
    public void testPointsBalanceSumsTransactions() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                "hamada@email.com",
                50,
                "EARNED",
                "Rental one",
                "RNT-1"
        );

        repository.add(
                "hamada@email.com",
                30,
                "EARNED",
                "Rental two",
                "RNT-2"
        );

        repository.add(
                "other@email.com",
                100,
                "EARNED",
                "Other customer",
                "RNT-3"
        );

        assertEquals(
                80,
                repository.pointsBalance(
                        "HAMADA@EMAIL.COM"
                )
        );
    }

    @Test
    public void testPointsBalanceSupportsNegativeTransactions() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                "hamada@email.com",
                100,
                "EARNED",
                "Rental completed",
                "RNT-1"
        );

        repository.add(
                "hamada@email.com",
                -40,
                "REDEEMED",
                "Reward redeemed",
                "REWARD-1"
        );

        assertEquals(
                60,
                repository.pointsBalance(
                        "hamada@email.com"
                )
        );
    }

    @Test
    public void testPointsBalanceReturnsZeroWhenNoTransactions() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        assertEquals(
                0,
                repository.pointsBalance(
                        "missing@email.com"
                )
        );
    }

    @Test
    public void testFindAllSkipsMalformedLines() throws Exception {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                "hamada@email.com",
                50,
                "EARNED",
                "Valid transaction",
                "RNT-1"
        );

        Files.writeString(
                loyaltyFile,
                "BAD\tLINE" + System.lineSeparator()
                        + "OTHER\t1\t2\t3\t4\t5\t6\t7"
                        + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<LoyaltyTransaction> result =
                repository.findAll();

        assertEquals(1, result.size());
        assertEquals(
                "Valid transaction",
                result.get(0).description()
        );
    }

    @Test
    public void testSpecialCharactersArePreserved() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                "special@email.com",
                75,
                "EARNED / BONUS",
                "BMW X5 + العربية | Special",
                "REF-特殊-1"
        );

        LoyaltyTransaction loaded =
                repository.findAll().get(0);

        assertEquals(
                "EARNED / BONUS",
                loaded.type()
        );
        assertEquals(
                "BMW X5 + العربية | Special",
                loaded.description()
        );
        assertEquals(
                "REF-特殊-1",
                loaded.referenceId()
        );
    }

    @Test
    public void testMultipleTransactionsAreStored() {
        LoyaltyTransactionRepository repository =
                new LoyaltyTransactionRepository();

        repository.add(
                "hamada@email.com",
                10,
                "EARNED",
                "First",
                "RNT-1"
        );

        repository.add(
                "hamada@email.com",
                20,
                "EARNED",
                "Second",
                "RNT-2"
        );

        repository.add(
                "hamada@email.com",
                -5,
                "REDEEMED",
                "Third",
                "REWARD-1"
        );

        assertEquals(3, repository.findAll().size());
    }

    @Test
    public void testLoyaltyTransactionRecordAccessors() {
        LoyaltyTransaction record =
                new LoyaltyTransaction(
                        "LOY-1",
                        "hamada@email.com",
                        50,
                        "EARNED",
                        "Rental completed",
                        "RNT-1",
                        "2026-07-15T10:00:00"
                );

        assertEquals(
                "LOY-1",
                record.transactionId()
        );
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
        assertEquals(50, record.points());
        assertEquals("EARNED", record.type());
        assertEquals(
                "Rental completed",
                record.description()
        );
        assertEquals(
                "RNT-1",
                record.referenceId()
        );
        assertEquals(
                "2026-07-15T10:00:00",
                record.createdAt()
        );
    }

    private Path getLoyaltyFile() throws Exception {
        Field field =
                LoyaltyTransactionRepository.class
                        .getDeclaredField("FILE");

        field.setAccessible(true);

        return (Path) field.get(null);
    }
}