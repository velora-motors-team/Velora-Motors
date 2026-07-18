package com.velora.repository;

import com.velora.authentication.Customer;
import com.velora.authentication.Customer.Role;
import com.velora.repository.RatingRepository.StoredRating;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class RatingRepositoryTest {

    @TempDir
    Path tempDir;

    @AfterEach
    public void clearSystemProperty() {
        System.clearProperty("velora.ratings.file");
    }

    private RatingRepository createRepository() {
        return new RatingRepository(
                tempDir.resolve("ratings.txt")
        );
    }

    private Customer createCustomer(
            String name,
            String email
    ) {
        return new Customer(
                name,
                email,
                "0599999999",
                Role.CUSTOMER
        );
    }

    @Test
    public void testConstructorCreatesRatingsFile() {
        Path file = tempDir.resolve("ratings.txt");

        assertFalse(Files.exists(file));

        RatingRepository repository =
                new RatingRepository(file);

        assertTrue(Files.exists(file));

        assertEquals(
                file.toAbsolutePath().normalize(),
                repository.getRatingsFile()
        );
    }

    @Test
    public void testDefaultConstructorUsesSystemProperty() {
        Path customFile =
                tempDir.resolve("custom-ratings.txt");

        System.setProperty(
                "velora.ratings.file",
                customFile.toString()
        );

        RatingRepository repository =
                new RatingRepository();

        assertEquals(
                customFile.toAbsolutePath().normalize(),
                repository.getRatingsFile()
        );

        assertTrue(Files.exists(customFile));
    }

    @Test
    public void testSaveRatingAndFindByEmail() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada Ahmad",
                "hamada@email.com"
        );

        repository.saveRating(
                customer,
                5,
                "Excellent service"
        );

        List<StoredRating> ratings =
                repository.findByEmail(
                        "hamada@email.com"
                );

        assertEquals(1, ratings.size());

        StoredRating rating = ratings.get(0);

        assertEquals(
                "hamada@email.com",
                rating.email()
        );
        assertEquals(
                "Hamada Ahmad",
                rating.fullName()
        );
        assertEquals(5, rating.rating());
        assertEquals(
                "Excellent service",
                rating.comment()
        );
        assertNotNull(rating.createdAt());
    }

    @Test
    public void testFindByEmailNormalizesEmail() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada Ahmad",
                "hamada@email.com"
        );

        repository.saveRating(
                customer,
                4,
                "Very good"
        );

        List<StoredRating> ratings =
                repository.findByEmail(
                        "  HAMADA@EMAIL.COM  "
                );

        assertEquals(1, ratings.size());
    }

    @Test
    public void testFindByEmailNullReturnsEmpty() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada Ahmad",
                "hamada@email.com"
        );

        repository.saveRating(
                customer,
                5,
                "Excellent"
        );

        assertTrue(
                repository.findByEmail(null).isEmpty()
        );
    }

    @Test
    public void testSaveRatingNullCustomerThrowsException() {
        RatingRepository repository = createRepository();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.saveRating(
                                null,
                                5,
                                "Excellent"
                        )
                );

        assertEquals(
                "Customer is required.",
                exception.getMessage()
        );
    }

    @Test
    public void testSaveRatingBelowMinimumThrowsException() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.saveRating(
                                customer,
                                0,
                                "Bad value"
                        )
                );

        assertEquals(
                "Rating must be between 1 and 5 stars.",
                exception.getMessage()
        );
    }

    @Test
    public void testSaveRatingAboveMaximumThrowsException() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.saveRating(
                        customer,
                        6,
                        "Invalid"
                )
        );
    }

    @Test
    public void testSaveRatingAcceptsMinimumRating() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        repository.saveRating(
                customer,
                1,
                "Needs improvement"
        );

        assertEquals(
                1,
                repository.findByEmail(
                        "hamada@email.com"
                ).get(0).rating()
        );
    }

    @Test
    public void testSaveRatingAcceptsMaximumRating() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        repository.saveRating(
                customer,
                5,
                "Excellent"
        );

        assertEquals(
                5,
                repository.findByEmail(
                        "hamada@email.com"
                ).get(0).rating()
        );
    }

    @Test
    public void testNullCommentBecomesEmpty() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        repository.saveRating(
                customer,
                4,
                null
        );

        assertEquals(
                "",
                repository.findByEmail(
                        "hamada@email.com"
                ).get(0).comment()
        );
    }

    @Test
    public void testCommentIsTrimmed() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        repository.saveRating(
                customer,
                4,
                "   Very good service   "
        );

        assertEquals(
                "Very good service",
                repository.findByEmail(
                        "hamada@email.com"
                ).get(0).comment()
        );
    }

    @Test
    public void testCreatedAtUsesCurrentTime() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        LocalDateTime before = LocalDateTime.now();

        repository.saveRating(
                customer,
                5,
                "Excellent"
        );

        LocalDateTime after = LocalDateTime.now();

        StoredRating rating =
                repository.findByEmail(
                        "hamada@email.com"
                ).get(0);

        LocalDateTime createdAt =
                LocalDateTime.parse(rating.createdAt());

        assertFalse(createdAt.isBefore(before));
        assertFalse(createdAt.isAfter(after));
    }

    @Test
    public void testAverageForEmail() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        repository.saveRating(customer, 5, "Excellent");
        repository.saveRating(customer, 4, "Very good");
        repository.saveRating(customer, 3, "Good");

        OptionalDouble average =
                repository.averageForEmail(
                        "HAMADA@EMAIL.COM"
                );

        assertTrue(average.isPresent());
        assertEquals(
                4.0,
                average.getAsDouble(),
                0.0001
        );
    }

    @Test
    public void testAverageForEmailEmptyWhenNoRatings() {
        RatingRepository repository = createRepository();

        OptionalDouble average =
                repository.averageForEmail(
                        "missing@email.com"
                );

        assertTrue(average.isEmpty());
    }

    @Test
    public void testCountForEmail() {
        RatingRepository repository = createRepository();

        Customer first = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        Customer second = createCustomer(
                "Sara",
                "sara@email.com"
        );

        repository.saveRating(first, 5, "Excellent");
        repository.saveRating(first, 4, "Very good");
        repository.saveRating(second, 3, "Good");

        assertEquals(
                2,
                repository.countForEmail(
                        "hamada@email.com"
                )
        );

        assertEquals(
                1,
                repository.countForEmail(
                        "SARA@EMAIL.COM"
                )
        );
    }

    @Test
    public void testCountForEmailReturnsZeroWhenNoRatings() {
        RatingRepository repository = createRepository();

        assertEquals(
                0,
                repository.countForEmail(
                        "missing@email.com"
                )
        );
    }

    @Test
    public void testMultipleCustomersAreSeparated() {
        RatingRepository repository = createRepository();

        Customer first = createCustomer(
                "Hamada",
                "hamada@email.com"
        );

        Customer second = createCustomer(
                "Sara",
                "sara@email.com"
        );

        repository.saveRating(first, 5, "Excellent");
        repository.saveRating(second, 2, "Not good");

        assertEquals(
                1,
                repository.findByEmail(
                        "hamada@email.com"
                ).size()
        );

        assertEquals(
                1,
                repository.findByEmail(
                        "sara@email.com"
                ).size()
        );
    }

    @Test
    public void testSpecialCharactersArePreserved() {
        RatingRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada & أحمد",
                "special@email.com"
        );

        repository.saveRating(
                customer,
                5,
                "Amazing | ممتاز + BMW & More\nSecond line"
        );

        StoredRating rating =
                repository.findByEmail(
                        "special@email.com"
                ).get(0);

        assertEquals(
                "Hamada & أحمد",
                rating.fullName()
        );

        assertEquals(
                "Amazing | ممتاز + BMW & More\nSecond line",
                rating.comment()
        );
    }

    @Test
    public void testMalformedLinesAreIgnored() throws IOException {
        Path file = tempDir.resolve("malformed-ratings.txt");

        String validLine = String.join(
                "\t",
                encode("valid@email.com"),
                encode("Valid User"),
                encode("5"),
                encode("Excellent"),
                encode("2026-07-15T10:00:00")
        );

        String invalidRating = String.join(
                "\t",
                encode("bad@email.com"),
                encode("Bad User"),
                encode("not-a-number"),
                encode("Invalid"),
                encode("2026-07-15T10:00:00")
        );

        Files.writeString(
                file,
                "# header" + System.lineSeparator()
                        + System.lineSeparator()
                        + "too-short"
                        + System.lineSeparator()
                        + invalidRating
                        + System.lineSeparator()
                        + validLine
                        + System.lineSeparator(),
                StandardCharsets.UTF_8
        );

        RatingRepository repository =
                new RatingRepository(file);

        List<StoredRating> ratings =
                repository.findByEmail(
                        "valid@email.com"
                );

        assertEquals(1, ratings.size());
        assertEquals(5, ratings.get(0).rating());
    }

    @Test
    public void testStoredRatingRecordAccessors() {
        StoredRating rating = new StoredRating(
                "hamada@email.com",
                "Hamada Ahmad",
                5,
                "Excellent",
                "2026-07-15T10:00:00"
        );

        assertEquals(
                "hamada@email.com",
                rating.email()
        );
        assertEquals(
                "Hamada Ahmad",
                rating.fullName()
        );
        assertEquals(5, rating.rating());
        assertEquals(
                "Excellent",
                rating.comment()
        );
        assertEquals(
                "2026-07-15T10:00:00",
                rating.createdAt()
        );
    }

    @Test
    public void testConstructorReportsStorageInitializationFailure()
            throws IOException {
        Path blockingFile = tempDir.resolve("not-a-directory");
        Files.writeString(blockingFile, "blocked", StandardCharsets.UTF_8);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new RatingRepository(blockingFile.resolve("ratings.txt"))
        );

        assertEquals(
                "Unable to initialize the ratings file.",
                exception.getMessage()
        );
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    public void testReadFailureIsReported() throws IOException {
        Path unreadablePath = tempDir.resolve("ratings-directory");
        Files.createDirectory(unreadablePath);
        Files.writeString(unreadablePath.resolve("keep.txt"), "keep");
        RatingRepository repository = new RatingRepository(unreadablePath);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.findByEmail("customer@velora.com")
        );

        assertEquals("Unable to read saved ratings.", exception.getMessage());
    }

    @Test
    public void testWriteFailureIsReported() throws IOException {
        RatingRepository repository = createRepository();
        Path tempFile = repository.getRatingsFile().resolveSibling("ratings.txt.tmp");
        Files.createDirectory(tempFile);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.saveRating(
                        createCustomer("Test User", "test@velora.com"),
                        5,
                        "Great"
                )
        );

        assertEquals(
                "Unable to save rating information.",
                exception.getMessage()
        );
    }

    @Test
    public void testAtomicMoveFallbackFailureIsReported() throws Exception {
        Path ratingsFile = tempDir.resolve("ratings.txt");
        Files.createDirectory(ratingsFile);
        Files.writeString(ratingsFile.resolve("keep.txt"), "keep");
        RatingRepository repository = new RatingRepository(ratingsFile);

        Method writeAll = RatingRepository.class.getDeclaredMethod(
                "writeAll", List.class
        );
        writeAll.setAccessible(true);

        InvocationTargetException invocation = assertThrows(
                InvocationTargetException.class,
                () -> writeAll.invoke(repository, List.of())
        );
        assertInstanceOf(IllegalStateException.class, invocation.getCause());
    }

    @Test
    public void testDefaultConstructorUsesUserDirectoryWhenNoOverride() {
        String previousUserDir = System.getProperty("user.dir");
        System.clearProperty("velora.ratings.file");

        try {
            System.setProperty("user.dir", tempDir.toString());
            RatingRepository repository = new RatingRepository();

            assertEquals(
                    tempDir.resolve("ratings.txt").toAbsolutePath().normalize(),
                    repository.getRatingsFile()
            );
        } finally {
            System.setProperty("user.dir", previousUserDir);
        }
    }

    @Test
    public void testBlankOverrideUsesUserDirectory() {
        String previousUserDir = System.getProperty("user.dir");
        System.setProperty("velora.ratings.file", "   ");

        try {
            System.setProperty("user.dir", tempDir.toString());
            RatingRepository repository = new RatingRepository();

            assertEquals(
                    tempDir.resolve("ratings.txt").toAbsolutePath().normalize(),
                    repository.getRatingsFile()
            );
        } finally {
            System.setProperty("user.dir", previousUserDir);
        }
    }

    @Test
    public void testPrivateEncodingHelpersAcceptNull() throws Exception {
        Method encode = RatingRepository.class.getDeclaredMethod(
                "encode", String.class
        );
        Method decode = RatingRepository.class.getDeclaredMethod(
                "decode", String.class
        );
        encode.setAccessible(true);
        decode.setAccessible(true);

        assertEquals("", encode.invoke(null, (Object) null));
        assertEquals("", decode.invoke(null, (Object) null));
    }

    @Test
    public void testWriteWorksOnZipFileSystem() throws Exception {
        URI zipUri = URI.create("jar:" + tempDir.resolve("ratings.zip").toUri());

        try (FileSystem zip = FileSystems.newFileSystem(
                zipUri, Map.of("create", "true")
        )) {
            RatingRepository repository = new RatingRepository(
                    zip.getPath("/ratings.txt")
            );

            repository.saveRating(
                    createCustomer("Zip User", "zip@velora.com"),
                    5,
                    "Excellent"
            );

            assertEquals(1, repository.countForEmail("zip@velora.com"));
        }
    }


    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
