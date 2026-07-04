package com.velora.repository;

import com.velora.authentication.Customer;
import com.velora.authentication.Customer.Role;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CustomerRepository {

    private static final String HEADER = "# Velora Motors accounts v1"
            + System.lineSeparator()
            + "# email\tfullName\tphone\trole\tpassword";

    private final Path accountsFile;

    public CustomerRepository() {
        this(defaultAccountsFile());
    }

    public CustomerRepository(Path accountsFile) {
        this.accountsFile = accountsFile.toAbsolutePath().normalize();
        initializeStorage();
    }

    public synchronized Optional<StoredCustomer> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return readAll().stream()
                .filter(account -> account.customer().getEmail().equals(normalizedEmail))
                .findFirst();
    }

    public synchronized void save(Customer customer, String password) {
        if (findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        List<StoredCustomer> accounts = readAll();
        accounts.add(new StoredCustomer(customer, password));
        writeAll(accounts);
    }

    public synchronized boolean updatePassword(String email, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        List<StoredCustomer> accounts = readAll();
        boolean updated = false;

        for (int i = 0; i < accounts.size(); i++) {
            StoredCustomer account = accounts.get(i);

            if (account.customer().getEmail().equals(normalizedEmail)) {
                accounts.set(i, new StoredCustomer(account.customer(), newPassword));
                updated = true;
                break;
            }
        }

        if (updated) {
            writeAll(accounts);
        }

        return updated;
    }

    public synchronized boolean deleteByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        List<StoredCustomer> accounts = readAll();
        boolean removed = accounts.removeIf(
                account -> account.customer().getEmail().equals(normalizedEmail)
        );

        if (removed) {
            writeAll(accounts);
        }

        return removed;
    }

    public synchronized List<Customer> findAll() {
        return readAll().stream()
                .map(StoredCustomer::customer)
                .toList();
    }

    public synchronized List<StoredCustomer> findAllStored() {
        return readAll();
    }

    public Path getAccountsFile() {
        return accountsFile;
    }

    private void initializeStorage() {
        try {
            Path parent = accountsFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.notExists(accountsFile)) {
                Files.writeString(
                        accountsFile,
                        defaultFileContent(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW
                );
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize the accounts file.", ex);
        }
    }

    private List<StoredCustomer> readAll() {
        try {
            List<StoredCustomer> accounts = new ArrayList<>();

            for (String line : Files.readAllLines(accountsFile, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\t", -1);

                if (parts.length != 5) {
                    // دعم بسيط لو كتبت السطر يدويًا بفواصل : بدل tab
                    parts = line.split(":", 5);
                }

                if (parts.length != 5) {
                    continue;
                }

                try {
                    Customer customer = new Customer(
                            decode(parts[1]),
                            normalizeEmail(decode(parts[0])),
                            decode(parts[2]),
                            Role.valueOf(decode(parts[3]).trim())
                    );

                    String password = decode(parts[4]);
                    accounts.add(new StoredCustomer(customer, password));
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed records without breaking all saved accounts.
                }
            }

            return accounts;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read saved accounts.", ex);
        }
    }

    private void writeAll(List<StoredCustomer> accounts) {
        StringBuilder content = new StringBuilder(HEADER).append(System.lineSeparator());

        for (StoredCustomer account : accounts) {
            Customer customer = account.customer();
            content.append(encode(customer.getEmail())).append('\t')
                    .append(encode(customer.getFullName())).append('\t')
                    .append(encode(customer.getPhone())).append('\t')
                    .append(encode(customer.getRole().name())).append('\t')
                    .append(encode(account.password()))
                    .append(System.lineSeparator());
        }

        try {
            Path tempFile = accountsFile.resolveSibling(accountsFile.getFileName() + ".tmp");
            Files.writeString(
                    tempFile,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            try {
                Files.move(
                        tempFile,
                        accountsFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveFailure) {
                Files.move(
                        tempFile,
                        accountsFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save account information.", ex);
        }
    }

    private static String defaultFileContent() {
        return HEADER + System.lineSeparator()
                + "manager@velora.com\tMain+Manager\t\tMANAGER\tManager%40123" + System.lineSeparator()
                + "admin@velora.com\tSystem+Admin\t\tMANAGER\tAdmin%40123" + System.lineSeparator()
                + "ahmad%40gmail.com\tAhmad+Ali\t\tCUSTOMER\tAhmad%40123" + System.lineSeparator()
                + "sara%40gmail.com\tsara\t0599326499\tCUSTOMER\tSara%40123" + System.lineSeparator();
    }

    private static Path defaultAccountsFile() {
        String override = System.getProperty("velora.accounts.file");

        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }

        return Path.of(System.getProperty("user.dir"), "accounts.txt");
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public record StoredCustomer(
            Customer customer,
            String password
    ) {
    }
}
