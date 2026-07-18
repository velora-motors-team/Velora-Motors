package com.velora.repository;

import com.velora.authentication.Customer;
import com.velora.authentication.Customer.Role;
import com.velora.repository.CustomerRepository.StoredCustomer;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRepositoryTest {

    @TempDir
    Path tempDir;

    @AfterEach
    public void clearProperty() {
        System.clearProperty("velora.accounts.file");
    }

    private CustomerRepository createRepository() {
        return new CustomerRepository(tempDir.resolve("accounts.txt"));
    }

    private Customer createCustomer(
            String name,
            String email,
            String phone,
            Role role
    ) {
        return new Customer(name, email, phone, role);
    }

    @Test
    public void testConstructorCreatesAccountsFile() {
        Path file = tempDir.resolve("accounts.txt");

        assertFalse(Files.exists(file));

        CustomerRepository repository =
                new CustomerRepository(file);

        assertTrue(Files.exists(file));
        assertEquals(
                file.toAbsolutePath().normalize(),
                repository.getAccountsFile()
        );
    }

    @Test
    public void testDefaultConstructorUsesSystemProperty() {
        Path customFile =
                tempDir.resolve("custom-accounts.txt");

        System.setProperty(
                "velora.accounts.file",
                customFile.toString()
        );

        CustomerRepository repository =
                new CustomerRepository();

        assertEquals(
                customFile.toAbsolutePath().normalize(),
                repository.getAccountsFile()
        );

        assertTrue(Files.exists(customFile));
    }

    @Test
    public void testDefaultAccountsAreCreated() {
        CustomerRepository repository = createRepository();

        List<Customer> customers = repository.findAll();

        assertEquals(4, customers.size());

        assertTrue(
                customers.stream().anyMatch(
                        customer ->
                                customer.getEmail().equals("manager@velora.com")
                                        && customer.isManager()
                )
        );

        assertTrue(
                customers.stream().anyMatch(
                        customer ->
                                customer.getEmail().equals("sara@gmail.com")
                                        && customer.isCustomer()
                )
        );
    }

    @Test
    public void testFindByEmailNormalizesEmail() {
        CustomerRepository repository = createRepository();

        Optional<StoredCustomer> result =
                repository.findByEmail(
                        "  MANAGER@VELORA.COM  "
                );

        assertTrue(result.isPresent());
        assertEquals(
                "Main Manager",
                result.get().customer().getFullName()
        );
        assertEquals(
                "Manager@123",
                result.get().password()
        );
    }

    @Test
    public void testFindByEmailNotFound() {
        CustomerRepository repository = createRepository();

        assertTrue(
                repository.findByEmail(
                        "missing@email.com"
                ).isEmpty()
        );
    }

    @Test
    public void testSaveCustomer() {
        CustomerRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999",
                Role.CUSTOMER
        );

        repository.save(customer, "Password@123");

        Optional<StoredCustomer> result =
                repository.findByEmail(
                        "hamada@email.com"
                );

        assertTrue(result.isPresent());
        assertEquals(
                "Hamada Ahmad",
                result.get().customer().getFullName()
        );
        assertEquals(
                "0599999999",
                result.get().customer().getPhone()
        );
        assertEquals(
                "Password@123",
                result.get().password()
        );
    }

    @Test
    public void testSaveDuplicateEmailThrowsException() {
        CustomerRepository repository = createRepository();

        Customer customer = createCustomer(
                "Another Manager",
                "MANAGER@VELORA.COM",
                "",
                Role.MANAGER
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.save(
                                customer,
                                "password"
                        )
                );

        assertEquals(
                "An account with this email already exists.",
                exception.getMessage()
        );
    }

    @Test
    public void testUpdatePasswordSuccess() {
        CustomerRepository repository = createRepository();

        boolean result = repository.updatePassword(
                "  SARA@GMAIL.COM ",
                "NewPassword@123"
        );

        assertTrue(result);

        StoredCustomer stored =
                repository.findByEmail(
                        "sara@gmail.com"
                ).orElseThrow();

        assertEquals(
                "NewPassword@123",
                stored.password()
        );
    }

    @Test
    public void testUpdatePasswordNotFound() {
        CustomerRepository repository = createRepository();

        assertFalse(
                repository.updatePassword(
                        "missing@email.com",
                        "newPassword"
                )
        );
    }

    @Test
    public void testUpdateProfileSuccess() {
        CustomerRepository repository = createRepository();

        boolean result = repository.updateProfile(
                "sara@gmail.com",
                "  Sara Ali  ",
                "  0591111111  "
        );

        assertTrue(result);

        StoredCustomer stored =
                repository.findByEmail(
                        "sara@gmail.com"
                ).orElseThrow();

        assertEquals(
                "Sara Ali",
                stored.customer().getFullName()
        );

        assertEquals(
                "0591111111",
                stored.customer().getPhone()
        );

        assertEquals(
                Role.CUSTOMER,
                stored.customer().getRole()
        );

        assertEquals(
                "Sara@123",
                stored.password()
        );
    }

    @Test
    public void testUpdateProfileNullPhoneBecomesEmpty() {
        CustomerRepository repository = createRepository();

        assertTrue(
                repository.updateProfile(
                        "sara@gmail.com",
                        "Sara Ali",
                        null
                )
        );

        assertEquals(
                "",
                repository.findByEmail(
                        "sara@gmail.com"
                ).orElseThrow()
                        .customer()
                        .getPhone()
        );
    }

    @Test
    public void testUpdateProfileBlankNameThrowsException() {
        CustomerRepository repository = createRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateProfile(
                        "sara@gmail.com",
                        "   ",
                        "0591111111"
                )
        );
    }

    @Test
    public void testUpdateProfileNullNameThrowsException() {
        CustomerRepository repository = createRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateProfile(
                        "sara@gmail.com",
                        null,
                        "0591111111"
                )
        );
    }

    @Test
    public void testUpdateProfileNotFound() {
        CustomerRepository repository = createRepository();

        assertFalse(
                repository.updateProfile(
                        "missing@email.com",
                        "Missing User",
                        "0591111111"
                )
        );
    }

    @Test
    public void testDeleteByEmailSuccess() {
        CustomerRepository repository = createRepository();

        assertTrue(
                repository.deleteByEmail(
                        "  AHMAD@GMAIL.COM "
                )
        );

        assertTrue(
                repository.findByEmail(
                        "ahmad@gmail.com"
                ).isEmpty()
        );
    }

    @Test
    public void testDeleteByEmailNotFound() {
        CustomerRepository repository = createRepository();

        assertFalse(
                repository.deleteByEmail(
                        "missing@email.com"
                )
        );
    }

    @Test
    public void testFindAllReturnsCustomersWithoutPasswords() {
        CustomerRepository repository = createRepository();

        List<Customer> customers =
                repository.findAll();

        assertEquals(4, customers.size());

        assertTrue(
                customers.stream().allMatch(
                        customer ->
                                customer.getEmail() != null
                )
        );
    }

    @Test
    public void testFindAllStoredReturnsPasswords() {
        CustomerRepository repository = createRepository();

        List<StoredCustomer> accounts =
                repository.findAllStored();

        assertEquals(4, accounts.size());

        assertTrue(
                accounts.stream().anyMatch(
                        account ->
                                account.customer()
                                        .getEmail()
                                        .equals("admin@velora.com")
                                        && account.password()
                                        .equals("Admin@123")
                )
        );
    }

    @Test
    public void testEncodedValuesAreSavedAndLoaded() {
        CustomerRepository repository = createRepository();

        Customer customer = createCustomer(
                "Hamada & Ahmad",
                "special@email.com",
                "0599 123 456",
                Role.CUSTOMER
        );

        repository.save(
                customer,
                "Pass word+@123"
        );

        StoredCustomer stored =
                repository.findByEmail(
                        "special@email.com"
                ).orElseThrow();

        assertEquals(
                "Hamada & Ahmad",
                stored.customer().getFullName()
        );

        assertEquals(
                "0599 123 456",
                stored.customer().getPhone()
        );

        assertEquals(
                "Pass word+@123",
                stored.password()
        );
    }

    @Test
    public void testColonSeparatedRecordIsSupported()
            throws IOException {

        Path file = tempDir.resolve("colon.txt");

        String line = String.join(
                ":",
                encode("colon@email.com"),
                encode("Colon User"),
                encode("0599999999"),
                encode("CUSTOMER"),
                encode("Colon@123")
        );

        Files.writeString(
                file,
                "# header" + System.lineSeparator()
                        + line + System.lineSeparator(),
                StandardCharsets.UTF_8
        );

        CustomerRepository repository =
                new CustomerRepository(file);

        StoredCustomer stored =
                repository.findByEmail(
                        "colon@email.com"
                ).orElseThrow();

        assertEquals(
                "Colon User",
                stored.customer().getFullName()
        );

        assertEquals(
                "Colon@123",
                stored.password()
        );
    }

    @Test
    public void testMalformedLinesAreIgnored()
            throws IOException {

        Path file = tempDir.resolve("malformed.txt");

        String validLine = String.join(
                "\t",
                encode("valid@email.com"),
                encode("Valid User"),
                encode("0599999999"),
                encode("CUSTOMER"),
                encode("Valid@123")
        );

        String invalidRole = String.join(
                "\t",
                encode("invalid@email.com"),
                encode("Invalid User"),
                encode("0599999999"),
                encode("INVALID_ROLE"),
                encode("Invalid@123")
        );

        Files.writeString(
                file,
                "# header" + System.lineSeparator()
                        + System.lineSeparator()
                        + "too-short-line"
                        + System.lineSeparator()
                        + invalidRole
                        + System.lineSeparator()
                        + validLine
                        + System.lineSeparator(),
                StandardCharsets.UTF_8
        );

        CustomerRepository repository =
                new CustomerRepository(file);

        List<StoredCustomer> accounts =
                repository.findAllStored();

        assertEquals(1, accounts.size());

        assertEquals(
                "valid@email.com",
                accounts.get(0)
                        .customer()
                        .getEmail()
        );
    }

    @Test
    public void testStoredCustomerRecord() {
        Customer customer = createCustomer(
                "Hamada",
                "hamada@email.com",
                "0599999999",
                Role.CUSTOMER
        );

        StoredCustomer stored =
                new StoredCustomer(
                        customer,
                        "Password@123"
                );

        assertSame(customer, stored.customer());
        assertEquals(
                "Password@123",
                stored.password()
        );
    }

    @Test
    public void testConstructorReportsStorageInitializationFailure()
            throws IOException {
        Path blockingFile = tempDir.resolve("not-a-directory");
        Files.writeString(blockingFile, "blocked", StandardCharsets.UTF_8);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new CustomerRepository(blockingFile.resolve("accounts.txt"))
        );

        assertEquals(
                "Unable to initialize the accounts file.",
                exception.getMessage()
        );
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    public void testReadFailureIsReported() throws IOException {
        Path unreadablePath = tempDir.resolve("accounts-directory");
        Files.createDirectory(unreadablePath);
        Files.writeString(unreadablePath.resolve("keep.txt"), "keep");
        CustomerRepository repository = new CustomerRepository(unreadablePath);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                repository::findAll
        );

        assertEquals("Unable to read saved accounts.", exception.getMessage());
    }

    @Test
    public void testWriteFailureIsReported() throws IOException {
        CustomerRepository repository = createRepository();
        Path tempFile = repository.getAccountsFile().resolveSibling("accounts.txt.tmp");
        Files.createDirectory(tempFile);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.save(createCustomer(
                        "Test User", "test@velora.com", "", Role.CUSTOMER
                ), "Password@123")
        );

        assertEquals(
                "Unable to save account information.",
                exception.getMessage()
        );
    }

    @Test
    public void testAtomicMoveFallbackFailureIsReported() throws Exception {
        Path accountsFile = tempDir.resolve("accounts.txt");
        Files.createDirectory(accountsFile);
        Files.writeString(accountsFile.resolve("keep.txt"), "keep");
        CustomerRepository repository = new CustomerRepository(accountsFile);

        Method writeAll = CustomerRepository.class.getDeclaredMethod(
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
        System.clearProperty("velora.accounts.file");

        try {
            System.setProperty("user.dir", tempDir.toString());
            CustomerRepository repository = new CustomerRepository();

            assertEquals(
                    tempDir.resolve("accounts.txt").toAbsolutePath().normalize(),
                    repository.getAccountsFile()
            );
        } finally {
            System.setProperty("user.dir", previousUserDir);
        }
    }

    @Test
    public void testBlankOverrideUsesUserDirectory() {
        String previousUserDir = System.getProperty("user.dir");
        System.setProperty("velora.accounts.file", "   ");

        try {
            System.setProperty("user.dir", tempDir.toString());
            CustomerRepository repository = new CustomerRepository();

            assertEquals(
                    tempDir.resolve("accounts.txt").toAbsolutePath().normalize(),
                    repository.getAccountsFile()
            );
        } finally {
            System.setProperty("user.dir", previousUserDir);
        }
    }

    @Test
    public void testPrivateEncodingHelpersAcceptNull() throws Exception {
        Method encode = CustomerRepository.class.getDeclaredMethod(
                "encode", String.class
        );
        Method decode = CustomerRepository.class.getDeclaredMethod(
                "decode", String.class
        );
        encode.setAccessible(true);
        decode.setAccessible(true);

        assertEquals("", encode.invoke(null, (Object) null));
        assertEquals("", decode.invoke(null, (Object) null));
    }

    @Test
    public void testWriteWorksOnZipFileSystem() throws Exception {
        URI zipUri = URI.create("jar:" + tempDir.resolve("accounts.zip").toUri());

        try (FileSystem zip = FileSystems.newFileSystem(
                zipUri, Map.of("create", "true")
        )) {
            CustomerRepository repository = new CustomerRepository(
                    zip.getPath("/accounts.txt")
            );
            Customer customer = createCustomer(
                    "Zip User", "zip@velora.com", "", Role.CUSTOMER
            );

            repository.save(customer, "Password@123");

            assertTrue(repository.findByEmail("zip@velora.com").isPresent());
        }
    }


    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
