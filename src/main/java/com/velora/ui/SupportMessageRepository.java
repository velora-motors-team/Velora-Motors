package com.velora.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SupportMessageRepository {

    private static final Path DEFAULT_FILE_PATH = Paths.get("support_messages.txt");

    private final Path filePath;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SupportMessageRepository() {
        this(DEFAULT_FILE_PATH);
    }

    public SupportMessageRepository(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath");
    }

    public void saveMessage(SupportMessage message) {
        try {
            Files.write(
                    filePath,
                    (message.toString() + System.lineSeparator())
                            .getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println(
                    "Error saving support message: " + e.getMessage()
            );
        }
    }

   public List<SupportMessage> loadMessages() {

    List<SupportMessage> messages = new ArrayList<>();

    Path path = filePath;

    if (!Files.exists(path)) {
        return messages;
    }

    try {

        List<String> lines =
                Files.readAllLines(
                        path,
                        StandardCharsets.UTF_8
                );

        for (String line : lines) {

            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\|", -1);

            // الرسائل القديمة فيها 9 حقول
            // الرسائل الجديدة فيها 11 حقل
            if (parts.length < 9) {
                continue;
            }

            try {

                String messageId = parts[0];

                String customerName = parts[1];

                String customerEmail = parts[2];

                SupportMessage.MessageType messageType =
                        SupportMessage.MessageType.valueOf(
                                parts[3]
                        );

                String subject = parts[4];

                String messageText =
                        parts[5].replace(
                                "\\n",
                                "\n"
                        );

                LocalDateTime dateTime =
                        LocalDateTime.parse(
                                parts[6],
                                FORMATTER
                        );

                boolean read =
                        Boolean.parseBoolean(
                                parts[7]
                        );

                SupportMessage.MessageStatus status =
                        SupportMessage.MessageStatus.valueOf(
                                parts[8]
                        );

                // ==============================
                // ADMIN REPLY
                // ==============================

                String adminReply = "";

                if (parts.length >= 10) {

                    adminReply =
                            parts[9].replace(
                                    "\\n",
                                    "\n"
                            );
                }

                // ==============================
                // REPLY DATE
                // ==============================

                LocalDateTime replyDateTime = null;

                if (parts.length >= 11
                        && !parts[10].isBlank()) {

                    replyDateTime =
                            LocalDateTime.parse(
                                    parts[10],
                                    FORMATTER
                            );
                }

                // ==============================
                // CREATE MESSAGE
                // ==============================

                SupportMessage message =
                        new SupportMessage(
                                messageId,
                                customerName,
                                customerEmail,
                                messageType,
                                subject,
                                messageText
                        );

                message.setDateTime(dateTime);

                message.setRead(read);

                message.setStatus(status);

                message.setAdminReply(adminReply);

                message.setReplyDateTime(replyDateTime);

                messages.add(message);

            } catch (IllegalArgumentException ex) {

                System.out.println(
                        "Skipping invalid support message line: "
                        + ex.getMessage()
                );
            }
        }

    } catch (IOException e) {

        System.out.println(
                "Error loading support messages: "
                + e.getMessage()
        );
    }

    return messages;
}
    public void saveAllMessages(List<SupportMessage> messages) {
        Path path = filePath;

        List<String> lines = new ArrayList<>();

        for (SupportMessage message : messages) {
            lines.add(message.toString());
        }

        try {
            Files.write(
                    path,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            System.out.println(
                    "Error saving all support messages: " + e.getMessage()
            );
        }
    }

    public boolean updateMessage(SupportMessage updatedMessage) {
        List<SupportMessage> messages = loadMessages();
        boolean found = false;

        for (int i = 0; i < messages.size(); i++) {
            SupportMessage current = messages.get(i);

            if (current.getMessageId().equals(updatedMessage.getMessageId())) {
                messages.set(i, updatedMessage);
                found = true;
                break;
            }
        }

        if (found) {
            saveAllMessages(messages);
        }

        return found;
    }

    public boolean markAsRead(String messageId) {
        List<SupportMessage> messages = loadMessages();
        boolean changed = false;

        for (SupportMessage message : messages) {
            if (message.getMessageId().equals(messageId)) {
                if (!message.isRead()) {
                    message.setRead(true);
                    changed = true;
                }
                break;
            }
        }

        if (changed) {
            saveAllMessages(messages);
        }

        return changed;
    }

    public boolean updateStatus(
            String messageId,
            SupportMessage.MessageStatus newStatus
    ) {
        List<SupportMessage> messages = loadMessages();
        boolean changed = false;

        for (SupportMessage message : messages) {
            if (message.getMessageId().equals(messageId)) {
                message.setStatus(newStatus);
                changed = true;
                break;
            }
        }

        if (changed) {
            saveAllMessages(messages);
        }

        return changed;
    }

    public int getUnreadCount() {
        int count = 0;

        for (SupportMessage message : loadMessages()) {
            if (!message.isRead()) {
                count++;
            }
        }

        return count;
    }
}
