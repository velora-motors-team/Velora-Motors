package com.velora.ui;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SupportMessageTest {

    @Test
    public void testConstructorDefaultsAndGetters() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.LIVE_CHAT,
                "Need help",
                "Hello"
        );

        assertEquals("MSG-1", message.getMessageId());
        assertEquals("Hamada", message.getCustomerName());
        assertEquals("hamada@email.com", message.getCustomerEmail());
        assertEquals(
                SupportMessage.MessageType.LIVE_CHAT,
                message.getMessageType()
        );
        assertEquals("Need help", message.getSubject());
        assertEquals("Hello", message.getMessage());
        assertNotNull(message.getDateTime());
        assertFalse(message.isRead());
        assertEquals(
                SupportMessage.MessageStatus.OPEN,
                message.getStatus()
        );
        assertEquals("", message.getAdminReply());
        assertNull(message.getReplyDateTime());
        assertFalse(message.hasAdminReply());
    }

    @Test
    public void testSetters() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.SUPPORT_TICKET,
                "Subject",
                "Message"
        );

        LocalDateTime dateTime =
                LocalDateTime.of(2026, 7, 14, 10, 30);

        LocalDateTime replyDateTime =
                LocalDateTime.of(2026, 7, 14, 11, 45);

        message.setDateTime(dateTime);
        message.setRead(true);
        message.setStatus(
                SupportMessage.MessageStatus.RESOLVED
        );
        message.setAdminReply("Solved");
        message.setReplyDateTime(replyDateTime);

        assertEquals(dateTime, message.getDateTime());
        assertTrue(message.isRead());
        assertEquals(
                SupportMessage.MessageStatus.RESOLVED,
                message.getStatus()
        );
        assertEquals("Solved", message.getAdminReply());
        assertEquals(replyDateTime, message.getReplyDateTime());
        assertTrue(message.hasAdminReply());
    }

    @Test
    public void testNullAdminReplyBecomesEmpty() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.BILLING_ISSUE,
                "Subject",
                "Message"
        );

        message.setAdminReply(null);

        assertEquals("", message.getAdminReply());
        assertFalse(message.hasAdminReply());
    }

    @Test
    public void testBlankAdminReplyIsNotReply() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.BILLING_ISSUE,
                "Subject",
                "Message"
        );

        message.setAdminReply("   ");

        assertFalse(message.hasAdminReply());
    }

    @Test
    public void testFormattedDateTime() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.RENTAL_ISSUE,
                "Subject",
                "Message"
        );

        message.setDateTime(
                LocalDateTime.of(2026, 7, 14, 9, 5)
        );

        assertEquals(
                "2026-07-14 09:05",
                message.getFormattedDateTime()
        );
    }

    @Test
    public void testFormattedReplyDateTimeWhenNull() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.RENTAL_ISSUE,
                "Subject",
                "Message"
        );

        assertEquals("", message.getFormattedReplyDateTime());
    }

    @Test
    public void testFormattedReplyDateTime() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada",
                "hamada@email.com",
                SupportMessage.MessageType.RENTAL_ISSUE,
                "Subject",
                "Message"
        );

        message.setReplyDateTime(
                LocalDateTime.of(2026, 7, 14, 15, 45)
        );

        assertEquals(
                "2026-07-14 15:45",
                message.getFormattedReplyDateTime()
        );
    }

    @Test
    public void testToStringSanitizesValues() {
        SupportMessage message = new SupportMessage(
                "MSG-1",
                "Hamada|Ahmad",
                "hamada@email.com",
                SupportMessage.MessageType.TECHNICAL_ISSUE,
                "Bad|Subject",
                "Line1\r\nLine2"
        );

        message.setDateTime(
                LocalDateTime.of(2026, 7, 14, 10, 30)
        );
        message.setRead(true);
        message.setStatus(
                SupportMessage.MessageStatus.IN_PROGRESS
        );
        message.setAdminReply("Reply|One\nReply Two");
        message.setReplyDateTime(
                LocalDateTime.of(2026, 7, 14, 11, 0)
        );

        assertEquals(
                "MSG-1|Hamada/Ahmad|hamada@email.com|TECHNICAL_ISSUE"
                        + "|Bad/Subject|Line1 \\nLine2|2026-07-14 10:30"
                        + "|true|IN_PROGRESS|Reply/One\\nReply Two"
                        + "|2026-07-14 11:00",
                message.toString()
        );
    }

    @Test
    public void testAllMessageTypes() {
        assertEquals(6, SupportMessage.MessageType.values().length);
        assertEquals(
                SupportMessage.MessageType.EMERGENCY,
                SupportMessage.MessageType.valueOf("EMERGENCY")
        );
    }

    @Test
    public void testAllMessageStatuses() {
        assertEquals(3, SupportMessage.MessageStatus.values().length);
        assertEquals(
                SupportMessage.MessageStatus.RESOLVED,
                SupportMessage.MessageStatus.valueOf("RESOLVED")
        );
    }
}