package com.velora.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

final class DataStoreSupport {

    private static final DateTimeFormatter ID_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private DataStoreSupport() {
    }

    static Path dataFile(String fileName) {
        return Path.of(System.getProperty("user.dir"), "data", fileName)
                .toAbsolutePath()
                .normalize();
    }

    static void ensureFile(Path file, String header) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(file)) {
                List<String> lines = new ArrayList<>();
                if (header != null && !header.isBlank()) {
                    lines.add("# " + header);
                }
                Files.write(file, lines, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize data file: " + file, ex);
        }
    }

    static List<String> readDataLines(Path file) {
        try {
            ensureFile(file, "Velora Motors data file");
            List<String> result = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                result.add(line);
            }
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read data file: " + file, ex);
        }
    }

    static void appendLine(Path file, String line) {
        try {
            ensureFile(file, "Velora Motors data file");
            Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to append to data file: " + file, ex);
        }
    }

    static void writeAll(Path file, List<String> lines, String header) {
        try {
            ensureFile(file, header);
            List<String> output = new ArrayList<>();
            if (header != null && !header.isBlank()) {
                output.add("# " + header);
            }
            output.addAll(lines);
            Files.write(file, output, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write data file: " + file, ex);
        }
    }

    static String encode(String value) {
        return Base64.getEncoder().encodeToString(
                (value == null ? "" : value).getBytes(StandardCharsets.UTF_8)
        );
    }

    static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    static boolean parseBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    static String newId(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + LocalDateTime.now().format(ID_TIME) + "-" + suffix;
    }
}
