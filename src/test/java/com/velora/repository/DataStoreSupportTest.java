package com.velora.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class DataStoreSupportTest {

    @TempDir
    Path tempDir;

    @Test
    public void testPrivateConstructorCanBeInvokedByReflection()
            throws Exception {

        Constructor<DataStoreSupport> constructor =
                DataStoreSupport.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        DataStoreSupport instance = constructor.newInstance();

        assertNotNull(instance);
    }

    @Test
    public void testDataFileReturnsNormalizedAbsolutePath() {
        Path result = DataStoreSupport.dataFile("test-data.tsv");

        assertTrue(result.isAbsolute());

        assertEquals(
                Path.of(
                        System.getProperty("user.dir"),
                        "data",
                        "test-data.tsv"
                ).toAbsolutePath().normalize(),
                result
        );
    }

    @Test
    public void testEnsureFileCreatesParentAndHeader() throws Exception {
        Path file = tempDir
                .resolve("nested")
                .resolve("sample.tsv");

        assertFalse(Files.exists(file));

        DataStoreSupport.ensureFile(
                file,
                "TEST\theader"
        );

        assertTrue(Files.exists(file));

        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8
        );

        assertEquals(1, lines.size());
        assertEquals("# TEST\theader", lines.get(0));
    }

    @Test
    public void testEnsureFileWithNullHeaderCreatesEmptyFile()
            throws Exception {

        Path file = tempDir.resolve("null-header.tsv");

        DataStoreSupport.ensureFile(file, null);

        assertTrue(Files.exists(file));
        assertTrue(Files.readAllLines(file).isEmpty());
    }

    @Test
    public void testEnsureFileWithBlankHeaderCreatesEmptyFile()
            throws Exception {

        Path file = tempDir.resolve("blank-header.tsv");

        DataStoreSupport.ensureFile(file, "   ");

        assertTrue(Files.exists(file));
        assertTrue(Files.readAllLines(file).isEmpty());
    }

    @Test
    public void testEnsureFileDoesNotOverwriteExistingFile()
            throws Exception {

        Path file = tempDir.resolve("existing.tsv");

        Files.writeString(
                file,
                "original-content",
                StandardCharsets.UTF_8
        );

        DataStoreSupport.ensureFile(
                file,
                "NEW HEADER"
        );

        assertEquals(
                "original-content",
                Files.readString(
                        file,
                        StandardCharsets.UTF_8
                )
        );
    }

    @Test
    public void testEnsureFileThrowsWhenParentCannotBeCreated()
            throws Exception {

        Path regularFile = tempDir.resolve("regular-file");

        Files.writeString(
                regularFile,
                "not a directory",
                StandardCharsets.UTF_8
        );

        Path impossibleChild =
                regularFile.resolve("child.tsv");

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> DataStoreSupport.ensureFile(
                                impossibleChild,
                                "HEADER"
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "Unable to initialize data file"
                )
        );

        assertNotNull(exception.getCause());
    }

    @Test
    public void testReadDataLinesCreatesMissingFile() {
        Path file = tempDir.resolve("missing.tsv");

        List<String> result =
                DataStoreSupport.readDataLines(file);

        assertTrue(result.isEmpty());
        assertTrue(Files.exists(file));
    }

    @Test
    public void testReadDataLinesSkipsBlankAndCommentLines()
            throws Exception {

        Path file = tempDir.resolve("read.tsv");

        Files.write(
                file,
                List.of(
                        "# header",
                        "",
                        "   ",
                        "FIRST",
                        "# another comment",
                        "SECOND"
                ),
                StandardCharsets.UTF_8
        );

        List<String> result =
                DataStoreSupport.readDataLines(file);

        assertEquals(
                List.of("FIRST", "SECOND"),
                result
        );
    }

    @Test
    public void testReadDataLinesThrowsWhenPathIsDirectory() {
        Path directory = tempDir.resolve("folder");

        assertDoesNotThrow(
                () -> Files.createDirectories(directory)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> DataStoreSupport.readDataLines(
                                directory
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "Unable to read data file"
                )
        );
    }

    @Test
    public void testAppendLineCreatesFileAndAppendsData()
            throws Exception {

        Path file = tempDir.resolve("append.tsv");

        DataStoreSupport.appendLine(file, "FIRST");
        DataStoreSupport.appendLine(file, "SECOND");

        List<String> result =
                DataStoreSupport.readDataLines(file);

        assertEquals(
                List.of("FIRST", "SECOND"),
                result
        );
    }

    @Test
    public void testAppendLineThrowsWhenPathIsDirectory()
            throws Exception {

        Path directory = tempDir.resolve("append-folder");

        Files.createDirectories(directory);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> DataStoreSupport.appendLine(
                                directory,
                                "DATA"
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "Unable to append to data file"
                )
        );
    }

    @Test
    public void testWriteAllWithHeader() throws Exception {
        Path file = tempDir.resolve("write-with-header.tsv");

        DataStoreSupport.writeAll(
                file,
                List.of("A", "B"),
                "MY HEADER"
        );

        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8
        );

        assertEquals(
                List.of("# MY HEADER", "A", "B"),
                lines
        );
    }

    @Test
    public void testWriteAllWithNullHeader() throws Exception {
        Path file = tempDir.resolve("write-null-header.tsv");

        DataStoreSupport.writeAll(
                file,
                List.of("A", "B"),
                null
        );

        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8
        );

        assertEquals(List.of("A", "B"), lines);
    }

    @Test
    public void testWriteAllWithBlankHeader() throws Exception {
        Path file = tempDir.resolve("write-blank-header.tsv");

        DataStoreSupport.writeAll(
                file,
                List.of("A"),
                "   "
        );

        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8
        );

        assertEquals(List.of("A"), lines);
    }

    @Test
    public void testWriteAllReplacesOldContent() throws Exception {
        Path file = tempDir.resolve("replace.tsv");

        Files.writeString(
                file,
                "OLD CONTENT",
                StandardCharsets.UTF_8
        );

        DataStoreSupport.writeAll(
                file,
                List.of("NEW"),
                "HEADER"
        );

        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8
        );

        assertEquals(
                List.of("# HEADER", "NEW"),
                lines
        );
    }

    @Test
    public void testWriteAllThrowsWhenPathIsDirectory()
            throws Exception {

        Path directory = tempDir.resolve("write-folder");

        Files.createDirectories(directory);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> DataStoreSupport.writeAll(
                                directory,
                                List.of("DATA"),
                                "HEADER"
                        )
                );

        assertTrue(
                exception.getMessage().contains(
                        "Unable to write data file"
                )
        );
    }

    @Test
    public void testEncodeAndDecodeRoundTrip() {
        String original =
                "BMW X5 | العربية + Special\nSecond Line";

        String encoded = DataStoreSupport.encode(original);
        String decoded = DataStoreSupport.decode(encoded);

        assertNotEquals(original, encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testEncodeNullReturnsEncodedEmptyString() {
        String encoded = DataStoreSupport.encode(null);

        assertNotNull(encoded);
        assertEquals("", DataStoreSupport.decode(encoded));
    }

    @Test
    public void testDecodeNullReturnsEmptyString() {
        assertEquals("", DataStoreSupport.decode(null));
    }

    @Test
    public void testDecodeBlankReturnsEmptyString() {
        assertEquals("", DataStoreSupport.decode("   "));
    }

    @Test
    public void testDecodeInvalidBase64ReturnsEmptyString() {
        assertEquals(
                "",
                DataStoreSupport.decode("%%%INVALID%%%")
        );
    }

    @Test
    public void testParseIntValidValue() {
        assertEquals(
                123,
                DataStoreSupport.parseInt("123", -1)
        );
    }

    @Test
    public void testParseIntInvalidValueReturnsFallback() {
        assertEquals(
                99,
                DataStoreSupport.parseInt(
                        "not-a-number",
                        99
                )
        );
    }

    @Test
    public void testParseDoubleValidValue() {
        assertEquals(
                123.45,
                DataStoreSupport.parseDouble(
                        "123.45",
                        -1.0
                ),
                0.0001
        );
    }

    @Test
    public void testParseDoubleInvalidValueReturnsFallback() {
        assertEquals(
                88.5,
                DataStoreSupport.parseDouble(
                        "invalid",
                        88.5
                ),
                0.0001
        );
    }

    @Test
    public void testParseBooleanTrue() {
        assertTrue(
                DataStoreSupport.parseBoolean("true")
        );
    }

    @Test
    public void testParseBooleanCaseInsensitiveTrue() {
        assertTrue(
                DataStoreSupport.parseBoolean("TRUE")
        );
    }

    @Test
    public void testParseBooleanFalse() {
        assertFalse(
                DataStoreSupport.parseBoolean("false")
        );
    }

    @Test
    public void testParseBooleanInvalidReturnsFalse() {
        assertFalse(
                DataStoreSupport.parseBoolean("anything")
        );
    }

    @Test
    public void testNewIdUsesPrefixAndExpectedFormat() {
        String id = DataStoreSupport.newId("PAY");

        assertNotNull(id);
        assertTrue(id.startsWith("PAY-"));

        String[] parts = id.split("-");

        assertEquals(3, parts.length);
        assertEquals("PAY", parts[0]);
        assertTrue(parts[1].matches("\\d{14}"));
        assertTrue(parts[2].matches("[A-F0-9]{6}"));
    }

    @Test
    public void testNewIdGeneratesDifferentValues() {
        String first = DataStoreSupport.newId("RES");
        String second = DataStoreSupport.newId("RES");

        assertNotEquals(first, second);
    }
}