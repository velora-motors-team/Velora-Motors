package com.velora.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NotificationRepository {

    private static final String HEADER = "NOTIFICATION\tnotificationId\tcustomerEmail\ttype\ttitle\tmessage\tcreatedAt\tread";
    private static final Path FILE = DataStoreSupport.dataFile("notifications.tsv");

    public NotificationRepository() {
        DataStoreSupport.ensureFile(FILE, HEADER);
    }

    public synchronized List<NotificationRecord> findAll() {
        List<NotificationRecord> result = new ArrayList<>();
        for (String line : DataStoreSupport.readDataLines(FILE)) {
            String[] p = line.split("\\t", -1);
            if (p.length != 8 || !"NOTIFICATION".equals(p[0])) {
                continue;
            }
            result.add(new NotificationRecord(
                    DataStoreSupport.decode(p[1]),
                    DataStoreSupport.decode(p[2]),
                    DataStoreSupport.decode(p[3]),
                    DataStoreSupport.decode(p[4]),
                    DataStoreSupport.decode(p[5]),
                    DataStoreSupport.decode(p[6]),
                    DataStoreSupport.parseBoolean(p[7])
            ));
        }
        return result;
    }

    public synchronized List<NotificationRecord> findByCustomerEmail(String email) {
        String normalized = normalize(email);
        return findAll().stream()
                .filter(n -> normalize(n.customerEmail()).equals(normalized))
                .toList();
    }

    public synchronized List<NotificationRecord> findUnreadByCustomerEmail(String email) {
        return findByCustomerEmail(email).stream().filter(n -> !n.read()).toList();
    }

    public synchronized NotificationRecord create(
            String customerEmail,
            String type,
            String title,
            String message
    ) {
        NotificationRecord record = new NotificationRecord(
                DataStoreSupport.newId("NOT"),
                normalize(customerEmail),
                type,
                title,
                message,
                LocalDateTime.now().toString(),
                false
        );
        DataStoreSupport.appendLine(FILE, serialize(record));
        return record;
    }

    public synchronized boolean markRead(String notificationId) {
        List<NotificationRecord> all = findAll();
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        for (NotificationRecord n : all) {
            NotificationRecord out = n;
            if (n.notificationId().equalsIgnoreCase(notificationId)) {
                out = new NotificationRecord(
                        n.notificationId(), n.customerEmail(), n.type(), n.title(),
                        n.message(), n.createdAt(), true
                );
                updated = true;
            }
            lines.add(serialize(out));
        }
        if (updated) {
            DataStoreSupport.writeAll(FILE, lines, HEADER);
        }
        return updated;
    }

    private static String serialize(NotificationRecord n) {
        return String.join("\t",
                "NOTIFICATION",
                DataStoreSupport.encode(n.notificationId()),
                DataStoreSupport.encode(n.customerEmail()),
                DataStoreSupport.encode(n.type()),
                DataStoreSupport.encode(n.title()),
                DataStoreSupport.encode(n.message()),
                DataStoreSupport.encode(n.createdAt()),
                String.valueOf(n.read())
        );
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record NotificationRecord(
            String notificationId,
            String customerEmail,
            String type,
            String title,
            String message,
            String createdAt,
            boolean read
    ) {
    }
}
