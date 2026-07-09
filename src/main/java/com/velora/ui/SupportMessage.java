package com.velora.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SupportMessage {

    public enum MessageType {
        LIVE_CHAT,
        SUPPORT_TICKET,
        RENTAL_ISSUE,
        BILLING_ISSUE,
        TECHNICAL_ISSUE,
        EMERGENCY
    }

    public enum MessageStatus {
        OPEN,
        IN_PROGRESS,
        RESOLVED
    }

    private String messageId;
    private String customerName;
    private String customerEmail;
    private MessageType messageType;
    private String subject;
    private String message;
    private LocalDateTime dateTime;
    private boolean read;
    private MessageStatus status;

    private String adminReply;
    private LocalDateTime replyDateTime;

    public SupportMessage(
            String messageId,
            String customerName,
            String customerEmail,
            MessageType messageType,
            String subject,
            String message
    ) {

        this.messageId = messageId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.messageType = messageType;
        this.subject = subject;
        this.message = message;

        this.dateTime = LocalDateTime.now();
        this.read = false;
        this.status = MessageStatus.OPEN;

        this.adminReply = "";
        this.replyDateTime = null;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean isRead() {
        return read;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public LocalDateTime getReplyDateTime() {
        return replyDateTime;
    }

    public boolean hasAdminReply() {
        return adminReply != null && !adminReply.isBlank();
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply == null ? "" : adminReply;
    }

    public void setReplyDateTime(LocalDateTime replyDateTime) {
        this.replyDateTime = replyDateTime;
    }

    public String getFormattedDateTime() {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return dateTime.format(formatter);
    }

    public String getFormattedReplyDateTime() {

        if (replyDateTime == null) {
            return "";
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return replyDateTime.format(formatter);
    }

    @Override
    public String toString() {

        return messageId + "|"
                + safe(customerName) + "|"
                + safe(customerEmail) + "|"
                + messageType + "|"
                + safe(subject) + "|"
                + safe(message) + "|"
                + getFormattedDateTime() + "|"
                + read + "|"
                + status + "|"
                + safe(adminReply) + "|"
                + getFormattedReplyDateTime();
    }

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("|", "/")
                .replace("\r", " ")
                .replace("\n", "\\n");
    }
}