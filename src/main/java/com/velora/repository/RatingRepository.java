package com.velora.repository;

import com.velora.authentication.Customer;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;

public final class RatingRepository {

    private static final String HEADER = "# Velora Motors ratings v1"
            + System.lineSeparator()
            + "# email\tfullName\trating\tcomment\tcreatedAt";

    private final Path ratingsFile;

    public RatingRepository() {
        this(defaultRatingsFile());
    }

    public RatingRepository(Path ratingsFile) {
        this.ratingsFile = ratingsFile.toAbsolutePath().normalize();
        initializeStorage();
    }

    public synchronized void saveRating(Customer customer, int rating, String comment) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }

        List<StoredRating> ratings = readAll();
        ratings.add(new StoredRating(
                customer.getEmail(),
                customer.getFullName(),
                rating,
                comment == null ? "" : comment.trim(),
                LocalDateTime.now().toString()
        ));
        writeAll(ratings);
    }

    public synchronized List<StoredRating> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return readAll().stream()
                .filter(rating -> rating.email().equals(normalizedEmail))
                .toList();
    }

    public synchronized OptionalDouble averageForEmail(String email) {
        return findByEmail(email).stream()
                .mapToInt(StoredRating::rating)
                .average();
    }

    public synchronized int countForEmail(String email) {
        return findByEmail(email).size();
    }

    public Path getRatingsFile() {
        return ratingsFile;
    }

    private void initializeStorage() {
        try {
            Path parent = ratingsFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.notExists(ratingsFile)) {
                Files.writeString(
                        ratingsFile,
                        HEADER + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW
                );
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize the ratings file.", ex);
        }
    }

    private List<StoredRating> readAll() {
        try {
            List<StoredRating> ratings = new ArrayList<>();
            for (String line : Files.readAllLines(ratingsFile, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\t", -1);
                if (parts.length != 5) {
                    continue;
                }

                try {
                    ratings.add(new StoredRating(
                            normalizeEmail(decode(parts[0])),
                            decode(parts[1]),
                            Integer.parseInt(decode(parts[2])),
                            decode(parts[3]),
                            decode(parts[4])
                    ));
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed records and keep the rest of the file usable.
                }
            }
            return ratings;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read saved ratings.", ex);
        }
    }

    private void writeAll(List<StoredRating> ratings) {
        StringBuilder content = new StringBuilder(HEADER).append(System.lineSeparator());
        for (StoredRating rating : ratings) {
            content.append(encode(rating.email())).append('\t')
                    .append(encode(rating.fullName())).append('\t')
                    .append(encode(String.valueOf(rating.rating()))).append('\t')
                    .append(encode(rating.comment())).append('\t')
                    .append(encode(rating.createdAt()))
                    .append(System.lineSeparator());
        }

        try {
            Path tempFile = ratingsFile.resolveSibling(ratingsFile.getFileName() + ".tmp");
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
                        ratingsFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveFailure) {
                Files.move(tempFile, ratingsFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save rating information.", ex);
        }
    }

    private static Path defaultRatingsFile() {
        String override = System.getProperty("velora.ratings.file");
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        return Path.of(System.getProperty("user.dir"), "ratings.txt");
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public record StoredRating(
            String email,
            String fullName,
            int rating,
            String comment,
            String createdAt
    ) {
    }
}
