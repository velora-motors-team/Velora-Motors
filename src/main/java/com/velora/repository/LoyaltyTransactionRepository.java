package com.velora.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LoyaltyTransactionRepository {

    private static final String HEADER = "LOYALTY\ttransactionId\tcustomerEmail\tpoints\ttype\tdescription\treferenceId\tcreatedAt";
    private static final Path FILE = DataStoreSupport.dataFile("loyalty-transactions.tsv");

    public LoyaltyTransactionRepository() {
        DataStoreSupport.ensureFile(FILE, HEADER);
    }

    public synchronized List<LoyaltyTransaction> findAll() {
        List<LoyaltyTransaction> result = new ArrayList<>();
        for (String line : DataStoreSupport.readDataLines(FILE)) {
            String[] p = line.split("\\t", -1);
            if (p.length != 8 || !"LOYALTY".equals(p[0])) {
                continue;
            }
            result.add(new LoyaltyTransaction(
                    DataStoreSupport.decode(p[1]),
                    DataStoreSupport.decode(p[2]),
                    DataStoreSupport.parseInt(p[3], 0),
                    DataStoreSupport.decode(p[4]),
                    DataStoreSupport.decode(p[5]),
                    DataStoreSupport.decode(p[6]),
                    DataStoreSupport.decode(p[7])
            ));
        }
        return result;
    }

    public synchronized List<LoyaltyTransaction> findByCustomerEmail(String email) {
        String normalized = normalize(email);
        return findAll().stream()
                .filter(t -> normalize(t.customerEmail()).equals(normalized))
                .toList();
    }

    public synchronized int pointsBalance(String email) {
        return findByCustomerEmail(email).stream().mapToInt(LoyaltyTransaction::points).sum();
    }

    public synchronized LoyaltyTransaction add(
            String customerEmail,
            int points,
            String type,
            String description,
            String referenceId
    ) {
        LoyaltyTransaction record = new LoyaltyTransaction(
                DataStoreSupport.newId("LOY"),
                normalize(customerEmail),
                points,
                type,
                description,
                referenceId,
                LocalDateTime.now().toString()
        );
        DataStoreSupport.appendLine(FILE, serialize(record));
        return record;
    }

    private static String serialize(LoyaltyTransaction t) {
        return String.join("\t",
                "LOYALTY",
                DataStoreSupport.encode(t.transactionId()),
                DataStoreSupport.encode(t.customerEmail()),
                String.valueOf(t.points()),
                DataStoreSupport.encode(t.type()),
                DataStoreSupport.encode(t.description()),
                DataStoreSupport.encode(t.referenceId()),
                DataStoreSupport.encode(t.createdAt())
        );
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record LoyaltyTransaction(
            String transactionId,
            String customerEmail,
            int points,
            String type,
            String description,
            String referenceId,
            String createdAt
    ) {
    }
}
