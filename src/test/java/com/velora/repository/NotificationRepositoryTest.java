package com.velora.repository;

import com.velora.repository.NotificationRepository.NotificationRecord;

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

public class NotificationRepositoryTest {

    private Path notificationFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        notificationFile = getNotificationFile();

        originalFileExisted = Files.exists(notificationFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(notificationFile)
                : null;

        Files.deleteIfExists(notificationFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(notificationFile);

        if (originalFileExisted) {
            Files.createDirectories(notificationFile.getParent());
            Files.write(notificationFile, originalFileContent);
        }
    }

    @Test
    public void testConstructorCreatesNotificationFile() {
        assertFalse(Files.exists(notificationFile));

        new NotificationRepository();

        assertTrue(Files.exists(notificationFile));
    }

    @Test
    public void testFindAllInitiallyEmpty() {
        NotificationRepository repository =
                new NotificationRepository();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void testCreateAndFindAll() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord created = repository.create(
                "hamada@email.com",
                "RENTAL_CONFIRMED",
                "Rental confirmed",
                "Your BMW X5 rental is confirmed."
        );

        List<NotificationRecord> notifications =
                repository.findAll();

        assertEquals(1, notifications.size());

        NotificationRecord loaded = notifications.get(0);

        assertEquals(
                created.notificationId(),
                loaded.notificationId()
        );
        assertEquals(
                "hamada@email.com",
                loaded.customerEmail()
        );
        assertEquals(
                "RENTAL_CONFIRMED",
                loaded.type()
        );
        assertEquals(
                "Rental confirmed",
                loaded.title()
        );
        assertEquals(
                "Your BMW X5 rental is confirmed.",
                loaded.message()
        );
        assertFalse(loaded.read());
        assertNotNull(loaded.createdAt());
    }

    @Test
    public void testCreateGeneratesNotificationId() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord result = repository.create(
                "hamada@email.com",
                "INFO",
                "Hello",
                "Test message"
        );

        assertNotNull(result.notificationId());
        assertTrue(
                result.notificationId().startsWith("NOT-")
        );
    }

    @Test
    public void testCreateNormalizesEmail() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord result = repository.create(
                "  HAMADA@EMAIL.COM  ",
                "INFO",
                "Hello",
                "Test message"
        );

        assertEquals(
                "hamada@email.com",
                result.customerEmail()
        );
    }

    @Test
    public void testCreateNullEmailBecomesEmpty() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord result = repository.create(
                null,
                "INFO",
                "Hello",
                "Test message"
        );

        assertEquals("", result.customerEmail());
    }

    @Test
    public void testCreatedAtUsesCurrentTime() {
        NotificationRepository repository =
                new NotificationRepository();

        LocalDateTime before = LocalDateTime.now();

        NotificationRecord result = repository.create(
                "hamada@email.com",
                "INFO",
                "Hello",
                "Test message"
        );

        LocalDateTime after = LocalDateTime.now();

        LocalDateTime createdAt =
                LocalDateTime.parse(result.createdAt());

        assertFalse(createdAt.isBefore(before));
        assertFalse(createdAt.isAfter(after));
    }

    @Test
    public void testFindByCustomerEmailNormalizesEmail() {
        NotificationRepository repository =
                new NotificationRepository();

        repository.create(
                "hamada@email.com",
                "INFO",
                "First",
                "Message one"
        );

        repository.create(
                "other@email.com",
                "INFO",
                "Second",
                "Message two"
        );

        List<NotificationRecord> result =
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
        NotificationRepository repository =
                new NotificationRepository();

        repository.create(
                null,
                "INFO",
                "Hello",
                "Test message"
        );

        List<NotificationRecord> result =
                repository.findByCustomerEmail(null);

        assertEquals(1, result.size());
        assertEquals("", result.get(0).customerEmail());
    }

    @Test
    public void testFindByCustomerEmailNotFound() {
        NotificationRepository repository =
                new NotificationRepository();

        repository.create(
                "hamada@email.com",
                "INFO",
                "Hello",
                "Test message"
        );

        assertTrue(
                repository.findByCustomerEmail(
                        "missing@email.com"
                ).isEmpty()
        );
    }

    @Test
    public void testFindUnreadByCustomerEmail() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord first = repository.create(
                "hamada@email.com",
                "INFO",
                "First",
                "Unread message"
        );

        NotificationRecord second = repository.create(
                "hamada@email.com",
                "INFO",
                "Second",
                "Will be read"
        );

        assertTrue(
                repository.markRead(second.notificationId())
        );

        List<NotificationRecord> unread =
                repository.findUnreadByCustomerEmail(
                        "hamada@email.com"
                );

        assertEquals(1, unread.size());
        assertEquals(
                first.notificationId(),
                unread.get(0).notificationId()
        );
        assertFalse(unread.get(0).read());
    }

    @Test
    public void testMarkReadSuccess() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord created = repository.create(
                "hamada@email.com",
                "INFO",
                "Hello",
                "Test message"
        );

        boolean result =
                repository.markRead(
                        created.notificationId()
                );

        assertTrue(result);

        NotificationRecord updated =
                repository.findAll().get(0);

        assertTrue(updated.read());
    }

    @Test
    public void testMarkReadIsCaseInsensitive() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord created = repository.create(
                "hamada@email.com",
                "INFO",
                "Hello",
                "Test message"
        );

        assertTrue(
                repository.markRead(
                        created.notificationId()
                                .toLowerCase()
                )
        );
    }

    @Test
    public void testMarkReadNotFoundReturnsFalse() {
        NotificationRepository repository =
                new NotificationRepository();

        repository.create(
                "hamada@email.com",
                "INFO",
                "Hello",
                "Test message"
        );

        assertFalse(
                repository.markRead("NOT-MISSING")
        );
    }

    @Test
    public void testMarkReadPreservesOtherNotifications() {
        NotificationRepository repository =
                new NotificationRepository();

        NotificationRecord first = repository.create(
                "hamada@email.com",
                "INFO",
                "First",
                "Message one"
        );

        NotificationRecord second = repository.create(
                "hamada@email.com",
                "INFO",
                "Second",
                "Message two"
        );

        assertTrue(
                repository.markRead(first.notificationId())
        );

        List<NotificationRecord> all =
                repository.findAll();

        assertEquals(2, all.size());

        NotificationRecord firstUpdated = all.stream()
                .filter(n -> n.notificationId()
                        .equals(first.notificationId()))
                .findFirst()
                .orElseThrow();

        NotificationRecord secondUpdated = all.stream()
                .filter(n -> n.notificationId()
                        .equals(second.notificationId()))
                .findFirst()
                .orElseThrow();

        assertTrue(firstUpdated.read());
        assertFalse(secondUpdated.read());
    }

    @Test
    public void testFindAllSkipsMalformedLines() throws Exception {
        NotificationRepository repository =
                new NotificationRepository();

        repository.create(
                "hamada@email.com",
                "INFO",
                "Hello",
                "Valid message"
        );

        Files.writeString(
                notificationFile,
                "BAD\tLINE" + System.lineSeparator()
                        + "OTHER\t1\t2\t3\t4\t5\t6\t7"
                        + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND
        );

        List<NotificationRecord> result =
                repository.findAll();

        assertEquals(1, result.size());
        assertEquals(
                "Valid message",
                result.get(0).message()
        );
    }

    @Test
    public void testSpecialCharactersArePreserved() {
        NotificationRepository repository =
                new NotificationRepository();

        repository.create(
                "special@email.com",
                "TYPE / SPECIAL",
                "Title + العربية",
                "Line 1\nLine 2 | BMW & More"
        );

        NotificationRecord loaded =
                repository.findAll().get(0);

        assertEquals(
                "TYPE / SPECIAL",
                loaded.type()
        );
        assertEquals(
                "Title + العربية",
                loaded.title()
        );
        assertEquals(
                "Line 1\nLine 2 | BMW & More",
                loaded.message()
        );
    }

    @Test
    public void testNotificationRecordAccessors() {
        NotificationRecord record =
                new NotificationRecord(
                        "NOT-1",
                        "hamada@email.com",
                        "INFO",
                        "Hello",
                        "Message",
                        "2026-07-15T10:00:00",
                        true
                );

        assertEquals(
                "NOT-1",
                record.notificationId()
        );
        assertEquals(
                "hamada@email.com",
                record.customerEmail()
        );
        assertEquals("INFO", record.type());
        assertEquals("Hello", record.title());
        assertEquals("Message", record.message());
        assertEquals(
                "2026-07-15T10:00:00",
                record.createdAt()
        );
        assertTrue(record.read());
    }

    private Path getNotificationFile() throws Exception {
        Field field =
                NotificationRepository.class.getDeclaredField(
                        "FILE"
                );

        field.setAccessible(true);

        return (Path) field.get(null);
    }
}