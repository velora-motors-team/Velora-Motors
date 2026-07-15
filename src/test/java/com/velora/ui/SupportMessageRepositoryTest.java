package com.velora.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class SupportMessageRepositoryTest {

    @TempDir
    Path tempDirectory;

    private Path file;
    private SupportMessageRepository repository;

    @BeforeEach
    public void setUp() {
        file = tempDirectory.resolve("support_messages.txt");
        repository = new SupportMessageRepository(file);
    }

    private SupportMessage createMessage(
            String id,
            boolean read,
            SupportMessage.MessageStatus status
    ) {
        SupportMessage message = new SupportMessage(
                id,
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.SUPPORT_TICKET,
                "Need help",
                "First line\nSecond line"
        );

        message.setDateTime(
                LocalDateTime.of(2026, 7, 14, 10, 30)
        );
        message.setRead(read);
        message.setStatus(status);

        return message;
    }

    @Test
    public void testLoadMessagesWhenFileDoesNotExist() {
        assertTrue(repository.loadMessages().isEmpty());
    }

    @Test
    public void testSaveAndLoadMessage() {
        SupportMessage message = createMessage(
                "MSG-1",
                true,
                SupportMessage.MessageStatus.RESOLVED
        );

        message.setAdminReply("Solved\nPlease try again.");
        message.setReplyDateTime(
                LocalDateTime.of(2026, 7, 14, 11, 45)
        );

        repository.saveMessage(message);

        List<SupportMessage> result = repository.loadMessages();

        assertEquals(1, result.size());

        SupportMessage loaded = result.get(0);

        assertEquals("MSG-1", loaded.getMessageId());
        assertEquals("Hamada", loaded.getCustomerName());
        assertEquals("hamada@email.com", loaded.getCustomerEmail());
        assertEquals(
                SupportMessage.MessageType.SUPPORT_TICKET,
                loaded.getMessageType()
        );
        assertEquals("Need help", loaded.getSubject());
        assertEquals("First line\nSecond line", loaded.getMessage());
        assertEquals(
                LocalDateTime.of(2026, 7, 14, 10, 30),
                loaded.getDateTime()
        );
        assertTrue(loaded.isRead());
        assertEquals(
                SupportMessage.MessageStatus.RESOLVED,
                loaded.getStatus()
        );
        assertEquals(
                "Solved\nPlease try again.",
                loaded.getAdminReply()
        );
        assertEquals(
                LocalDateTime.of(2026, 7, 14, 11, 45),
                loaded.getReplyDateTime()
        );
    }

    @Test
    public void testLoadOldNineFieldMessage() throws IOException {
        String line =
                "OLD-1|Hamada|hamada@email.com|LIVE_CHAT|Hello"
                        + "|Line1\\nLine2|2026-07-14 10:00|false|OPEN";

        Files.writeString(
                file,
                line + System.lineSeparator(),
                StandardCharsets.UTF_8
        );

        List<SupportMessage> result = repository.loadMessages();

        assertEquals(1, result.size());
        assertEquals("Line1\nLine2", result.get(0).getMessage());
        assertEquals("", result.get(0).getAdminReply());
        assertNull(result.get(0).getReplyDateTime());
    }

    @Test
    public void testLoadMessagesSkipsBlankShortAndInvalidLines()
            throws IOException {

        String content =
                System.lineSeparator()
                        + "too|short|line"
                        + System.lineSeparator()
                        + "BAD-1|Name|email|NOT_A_TYPE|Subject|Message"
                        + "|2026-07-14 10:00|false|OPEN"
                        + System.lineSeparator();

        Files.writeString(
                file,
                content,
                StandardCharsets.UTF_8
        );

        assertTrue(repository.loadMessages().isEmpty());
    }

    @Test
    public void testSaveAllMessagesOverwritesFile() {
        SupportMessage first = createMessage(
                "MSG-1",
                false,
                SupportMessage.MessageStatus.OPEN
        );

        SupportMessage second = createMessage(
                "MSG-2",
                true,
                SupportMessage.MessageStatus.RESOLVED
        );

        repository.saveAllMessages(List.of(first, second));

        List<SupportMessage> result = repository.loadMessages();

        assertEquals(2, result.size());
        assertEquals("MSG-1", result.get(0).getMessageId());
        assertEquals("MSG-2", result.get(1).getMessageId());
    }

    @Test
    public void testUpdateMessageFound() {
        SupportMessage original = createMessage(
                "MSG-1",
                false,
                SupportMessage.MessageStatus.OPEN
        );

        repository.saveAllMessages(List.of(original));

        SupportMessage updated = createMessage(
                "MSG-1",
                true,
                SupportMessage.MessageStatus.RESOLVED
        );

        updated.setAdminReply("Done");

        assertTrue(repository.updateMessage(updated));

        SupportMessage loaded = repository.loadMessages().get(0);

        assertTrue(loaded.isRead());
        assertEquals(
                SupportMessage.MessageStatus.RESOLVED,
                loaded.getStatus()
        );
        assertEquals("Done", loaded.getAdminReply());
    }

    @Test
    public void testUpdateMessageNotFound() {
        SupportMessage existing = createMessage(
                "MSG-1",
                false,
                SupportMessage.MessageStatus.OPEN
        );

        repository.saveAllMessages(List.of(existing));

        SupportMessage missing = createMessage(
                "MSG-999",
                true,
                SupportMessage.MessageStatus.RESOLVED
        );

        assertFalse(repository.updateMessage(missing));
    }

    @Test
    public void testMarkAsReadSuccess() {
        repository.saveAllMessages(
                List.of(createMessage(
                        "MSG-1",
                        false,
                        SupportMessage.MessageStatus.OPEN
                ))
        );

        assertTrue(repository.markAsRead("MSG-1"));
        assertTrue(repository.loadMessages().get(0).isRead());
    }

    @Test
    public void testMarkAsReadAlreadyReadReturnsFalse() {
        repository.saveAllMessages(
                List.of(createMessage(
                        "MSG-1",
                        true,
                        SupportMessage.MessageStatus.OPEN
                ))
        );

        assertFalse(repository.markAsRead("MSG-1"));
    }

    @Test
    public void testMarkAsReadMissingReturnsFalse() {
        repository.saveAllMessages(
                List.of(createMessage(
                        "MSG-1",
                        false,
                        SupportMessage.MessageStatus.OPEN
                ))
        );

        assertFalse(repository.markAsRead("MSG-999"));
    }

    @Test
    public void testUpdateStatusSuccess() {
        repository.saveAllMessages(
                List.of(createMessage(
                        "MSG-1",
                        false,
                        SupportMessage.MessageStatus.OPEN
                ))
        );

        assertTrue(
                repository.updateStatus(
                        "MSG-1",
                        SupportMessage.MessageStatus.IN_PROGRESS
                )
        );

        assertEquals(
                SupportMessage.MessageStatus.IN_PROGRESS,
                repository.loadMessages().get(0).getStatus()
        );
    }

    @Test
    public void testUpdateStatusMissingReturnsFalse() {
        repository.saveAllMessages(
                List.of(createMessage(
                        "MSG-1",
                        false,
                        SupportMessage.MessageStatus.OPEN
                ))
        );

        assertFalse(
                repository.updateStatus(
                        "MSG-999",
                        SupportMessage.MessageStatus.RESOLVED
                )
        );
    }

    @Test
    public void testGetUnreadCount() {
        SupportMessage first = createMessage(
                "MSG-1",
                false,
                SupportMessage.MessageStatus.OPEN
        );

        SupportMessage second = createMessage(
                "MSG-2",
                true,
                SupportMessage.MessageStatus.RESOLVED
        );

        SupportMessage third = createMessage(
                "MSG-3",
                false,
                SupportMessage.MessageStatus.IN_PROGRESS
        );

        repository.saveAllMessages(
                List.of(first, second, third)
        );

        assertEquals(2, repository.getUnreadCount());
    }
}
