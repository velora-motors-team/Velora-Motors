package com.velora.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WalletTransactionRepository {

    private static final String HEADER = "WALLET\ttransactionId\tcustomerEmail\ttype\tamount\tbalanceAfter\treferenceId\tcreatedAt";
    private static final Path FILE = DataStoreSupport.dataFile("wallet-transactions.tsv");

    public WalletTransactionRepository() {
        DataStoreSupport.ensureFile(FILE, HEADER);
    }

    public synchronized List<WalletTransaction> findAll() {
        List<WalletTransaction> result = new ArrayList<>();
        for (String line : DataStoreSupport.readDataLines(FILE)) {
            String[] p = line.split("\\t", -1);
            if (p.length != 8 || !"WALLET".equals(p[0])) {
                continue;
            }
            result.add(new WalletTransaction(
                    DataStoreSupport.decode(p[1]),
                    DataStoreSupport.decode(p[2]),
                    DataStoreSupport.decode(p[3]),
                    DataStoreSupport.parseDouble(p[4], 0),
                    DataStoreSupport.parseDouble(p[5], 0),
                    DataStoreSupport.decode(p[6]),
                    DataStoreSupport.decode(p[7])
            ));
        }
        return result;
    }

    public synchronized List<WalletTransaction> findByCustomerEmail(String email) {
        String normalized = normalize(email);
        return findAll().stream()
                .filter(t -> normalize(t.customerEmail()).equals(normalized))
                .toList();
    }

    public synchronized WalletTransaction add(
            String customerEmail,
            String type,
            double amount,
            double balanceAfter,
            String referenceId
    ) {
        WalletTransaction record = new WalletTransaction(
                DataStoreSupport.newId("WAL"),
                normalize(customerEmail),
                type,
                amount,
                balanceAfter,
                referenceId,
                LocalDateTime.now().toString()
        );
        DataStoreSupport.appendLine(FILE, serialize(record));
        return record;
    }

    private static String serialize(WalletTransaction t) {
        return String.join("\t",
                "WALLET",
                DataStoreSupport.encode(t.transactionId()),
                DataStoreSupport.encode(t.customerEmail()),
                DataStoreSupport.encode(t.type()),
                String.valueOf(t.amount()),
                String.valueOf(t.balanceAfter()),
                DataStoreSupport.encode(t.referenceId()),
                DataStoreSupport.encode(t.createdAt())
        );
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record WalletTransaction(
            String transactionId,
            String customerEmail,
            String type,
            double amount,
            double balanceAfter,
            String referenceId,
            String createdAt
    ) {
    }
}
