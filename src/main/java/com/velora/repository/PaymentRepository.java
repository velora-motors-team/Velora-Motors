package com.velora.repository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PaymentRepository {

    private static final String HEADER = "PAYMENT\tpaymentId\tinvoiceId\trentalId\tcustomerEmail\tamount\tmethod\tstatus\tcreatedAt";
    private static final Path FILE = DataStoreSupport.dataFile("payments.tsv");

    public PaymentRepository() {
        DataStoreSupport.ensureFile(FILE, HEADER);
    }

    public synchronized List<PaymentRecord> findAll() {
        List<PaymentRecord> result = new ArrayList<>();
        for (String line : DataStoreSupport.readDataLines(FILE)) {
            String[] p = line.split("\\t", -1);
            if (p.length != 9 || !"PAYMENT".equals(p[0])) {
                continue;
            }
            result.add(new PaymentRecord(
                    DataStoreSupport.decode(p[1]),
                    DataStoreSupport.decode(p[2]),
                    DataStoreSupport.decode(p[3]),
                    DataStoreSupport.decode(p[4]),
                    DataStoreSupport.parseDouble(p[5], 0),
                    DataStoreSupport.decode(p[6]),
                    DataStoreSupport.decode(p[7]),
                    DataStoreSupport.decode(p[8])
            ));
        }
        return result;
    }

    public synchronized List<PaymentRecord> findByCustomerEmail(String email) {
        String normalized = normalize(email);
        return findAll().stream()
                .filter(p -> normalize(p.customerEmail()).equals(normalized))
                .toList();
    }

    public synchronized PaymentRecord create(
            String invoiceId,
            String rentalId,
            String customerEmail,
            double amount,
            String method,
            String status
    ) {
        PaymentRecord record = new PaymentRecord(
                DataStoreSupport.newId("PAY"),
                invoiceId,
                rentalId,
                normalize(customerEmail),
                amount,
                method,
                status,
                LocalDateTime.now().toString()
        );
        DataStoreSupport.appendLine(FILE, serialize(record));
        return record;
    }

    private static String serialize(PaymentRecord p) {
        return String.join("\t",
                "PAYMENT",
                DataStoreSupport.encode(p.paymentId()),
                DataStoreSupport.encode(p.invoiceId()),
                DataStoreSupport.encode(p.rentalId()),
                DataStoreSupport.encode(p.customerEmail()),
                String.valueOf(p.amount()),
                DataStoreSupport.encode(p.method()),
                DataStoreSupport.encode(p.status()),
                DataStoreSupport.encode(p.createdAt())
        );
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record PaymentRecord(
            String paymentId,
            String invoiceId,
            String rentalId,
            String customerEmail,
            double amount,
            String method,
            String status,
            String createdAt
    ) {
    }
}
