package com.velora.service;

import com.velora.authentication.Customer;
import com.velora.authentication.Customer.Role;
import com.velora.repository.CustomerRepository;
import com.velora.repository.CustomerRepository.StoredCustomer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    @Test
    public void testDefaultConstructorCreatesDefaultAccounts(
            @TempDir Path tempDirectory
    ) {
        String previous = System.getProperty("velora.accounts.file");
        Path accountsFile = tempDirectory.resolve("accounts.txt");

        try {
            System.setProperty("velora.accounts.file", accountsFile.toString());
            AuthenticationService defaultService = new AuthenticationService();

            assertEquals(accountsFile.toAbsolutePath(), defaultService.getAccountsFile());
            assertTrue(defaultService.emailExists("manager@velora.com"));
        } finally {
            if (previous == null) {
                System.clearProperty("velora.accounts.file");
            } else {
                System.setProperty("velora.accounts.file", previous);
            }
        }
    }

    @Test
    public void testExistingDefaultAccountsAreNotCreatedAgain() {
        Customer existing = new Customer(
                "Existing", "existing@velora.com", "", Role.MANAGER
        );
        StoredCustomer stored = new StoredCustomer(existing, "password");
        CustomerRepository repository = mock(CustomerRepository.class);
        when(repository.findByEmail(anyString()))
                .thenReturn(Optional.of(stored));

        assertNotNull(new AuthenticationService(repository));

        verify(repository, never()).save(any(Customer.class), anyString());
    }

    private CustomerRepository customerRepository;
    private AuthenticationService service;

    @BeforeEach
    public void setUp() {
        customerRepository = mock(CustomerRepository.class);

        service = new AuthenticationService(customerRepository);

        clearInvocations(customerRepository);
    }

    @Test
    public void testRegisterCustomerSuccess() {
        when(customerRepository.findByEmail("hamada@email.com"))
                .thenReturn(Optional.empty());

        Customer result = service.registerCustomer(
                "  Hamada Ahmad  ",
                "  HAMADA@EMAIL.COM  ",
                "  0599999999  ",
                "password123".toCharArray()
        );

        assertEquals("Hamada Ahmad", result.getFullName());
        assertEquals("hamada@email.com", result.getEmail());
        assertEquals("0599999999", result.getPhone());
        assertEquals(Role.CUSTOMER, result.getRole());

        verify(customerRepository).findByEmail("hamada@email.com");
        verify(customerRepository).save(result, "password123");
    }

    @Test
    public void testRegisterManagerSuccess() {
        when(customerRepository.findByEmail("manager@velora.com"))
                .thenReturn(Optional.empty());

        Customer result = service.registerAccount(
                "Main Manager",
                "manager@velora.com",
                "",
                Role.MANAGER,
                "Manager@123".toCharArray()
        );

        assertEquals("Main Manager", result.getFullName());
        assertEquals("manager@velora.com", result.getEmail());
        assertEquals("", result.getPhone());
        assertEquals(Role.MANAGER, result.getRole());
        assertTrue(result.isManager());

        verify(customerRepository).save(result, "Manager@123");
    }

    @Test
    public void testRegisterAccountNullFullNameThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAccount(
                        null,
                        "user@email.com",
                        "0599999999",
                        Role.CUSTOMER,
                        "password123".toCharArray()
                )
        );

        verify(customerRepository, never()).save(any(), anyString());
    }

    @Test
    public void testRegisterAccountBlankFullNameThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAccount(
                        "   ",
                        "user@email.com",
                        "0599999999",
                        Role.CUSTOMER,
                        "password123".toCharArray()
                )
        );

        verify(customerRepository, never()).save(any(), anyString());
    }

    @Test
    public void testRegisterAccountNullEmailThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAccount(
                        "Test User",
                        null,
                        "0599999999",
                        Role.CUSTOMER,
                        "password123".toCharArray()
                )
        );

        verify(customerRepository, never()).save(any(), anyString());
    }

    @Test
    public void testRegisterAccountBlankEmailThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAccount(
                        "Test User",
                        "   ",
                        "0599999999",
                        Role.CUSTOMER,
                        "password123".toCharArray()
                )
        );

        verify(customerRepository, never()).save(any(), anyString());
    }

    @Test
    public void testRegisterAccountNullPasswordThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAccount(
                        "Test User",
                        "user@email.com",
                        "0599999999",
                        Role.CUSTOMER,
                        null
                )
        );

        verify(customerRepository, never()).save(any(), anyString());
    }

    @Test
    public void testRegisterAccountBlankPasswordThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAccount(
                        "Test User",
                        "user@email.com",
                        "0599999999",
                        Role.CUSTOMER,
                        "   ".toCharArray()
                )
        );

        verify(customerRepository, never()).save(any(), anyString());
    }

    @Test
    public void testRegisterAccountDuplicateEmailThrowsException() {
        Customer existingCustomer = new Customer(
                "Existing User",
                "user@email.com",
                "0599999999"
        );

        when(customerRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(new StoredCustomer(existingCustomer, "oldpass")));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAccount(
                        "New User",
                        "USER@EMAIL.COM",
                        "0591111111",
                        Role.CUSTOMER,
                        "password123".toCharArray()
                )
        );

        verify(customerRepository).findByEmail("user@email.com");
        verify(customerRepository, never()).save(any(), anyString());
    }

    @Test
    public void testAuthenticateCustomerSuccess() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999"
        );

        StoredCustomer storedCustomer =
                new StoredCustomer(customer, "password123");

        when(customerRepository.findByEmail("hamada@email.com"))
                .thenReturn(Optional.of(storedCustomer));

        Optional<Customer> result = service.authenticateCustomer(
                "  HAMADA@EMAIL.COM  ",
                "password123".toCharArray()
        );

        assertTrue(result.isPresent());
        assertSame(customer, result.get());

        verify(customerRepository).findByEmail("hamada@email.com");
    }

    @Test
    public void testAuthenticateCustomerWrongPassword() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999"
        );

        StoredCustomer storedCustomer =
                new StoredCustomer(customer, "password123");

        when(customerRepository.findByEmail("hamada@email.com"))
                .thenReturn(Optional.of(storedCustomer));

        Optional<Customer> result = service.authenticateCustomer(
                "hamada@email.com",
                "wrongpass".toCharArray()
        );

        assertTrue(result.isEmpty());
    }

    @Test
    public void testAuthenticateCustomerEmailNotFound() {
        when(customerRepository.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        Optional<Customer> result = service.authenticateCustomer(
                "missing@email.com",
                "password123".toCharArray()
        );

        assertTrue(result.isEmpty());
    }

    @Test
    public void testAuthenticateCustomerNullPassword() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999"
        );

        StoredCustomer storedCustomer =
                new StoredCustomer(customer, "");

        when(customerRepository.findByEmail("hamada@email.com"))
                .thenReturn(Optional.of(storedCustomer));

        Optional<Customer> result = service.authenticateCustomer(
                "hamada@email.com",
                null
        );

        assertTrue(result.isPresent());
    }

    @Test
    public void testResetPasswordSuccess() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999"
        );

        when(customerRepository.findByEmail("hamada@email.com"))
                .thenReturn(Optional.of(new StoredCustomer(customer, "oldpass")));

        when(customerRepository.updatePassword(
                "hamada@email.com",
                "newpass123"
        )).thenReturn(true);

        boolean result = service.resetPassword(
                "HAMADA@EMAIL.COM",
                "newpass123".toCharArray()
        );

        assertTrue(result);

        verify(customerRepository).updatePassword(
                "hamada@email.com",
                "newpass123"
        );
    }

    @Test
    public void testResetPasswordEmailNotFound() {
        when(customerRepository.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        boolean result = service.resetPassword(
                "missing@email.com",
                "newpass123".toCharArray()
        );

        assertFalse(result);

        verify(customerRepository, never())
                .updatePassword(anyString(), anyString());
    }

    @Test
    public void testResetPasswordNullPasswordBecomesEmpty() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999"
        );

        when(customerRepository.findByEmail("hamada@email.com"))
                .thenReturn(Optional.of(new StoredCustomer(customer, "oldpass")));

        when(customerRepository.updatePassword(
                "hamada@email.com",
                ""
        )).thenReturn(true);

        boolean result = service.resetPassword(
                "hamada@email.com",
                null
        );

        assertTrue(result);

        verify(customerRepository).updatePassword(
                "hamada@email.com",
                ""
        );
    }

    @Test
    public void testUpdateProfileSuccess() {
        Customer customer = new Customer(
                "Old Name",
                "user@email.com",
                "0599999999"
        );

        when(customerRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(new StoredCustomer(customer, "pass")));

        when(customerRepository.updateProfile(
                "user@email.com",
                "New Name",
                "0591111111"
        )).thenReturn(true);

        boolean result = service.updateProfile(
                " USER@EMAIL.COM ",
                "New Name",
                "0591111111"
        );

        assertTrue(result);

        verify(customerRepository).updateProfile(
                "user@email.com",
                "New Name",
                "0591111111"
        );
    }

    @Test
    public void testUpdateProfileEmailNotFound() {
        when(customerRepository.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        boolean result = service.updateProfile(
                "missing@email.com",
                "New Name",
                "0591111111"
        );

        assertFalse(result);

        verify(customerRepository, never())
                .updateProfile(anyString(), anyString(), anyString());
    }

    @Test
    public void testGetAccountsFile() {
        Path path = Path.of("database", "accounts.txt");

        when(customerRepository.getAccountsFile()).thenReturn(path);

        assertEquals(path, service.getAccountsFile());

        verify(customerRepository).getAccountsFile();
    }

    @Test
    public void testEmailExistsTrue() {
        Customer customer = new Customer(
                "Hamada",
                "hamada@email.com",
                "0599999999"
        );

        when(customerRepository.findByEmail("hamada@email.com"))
                .thenReturn(Optional.of(new StoredCustomer(customer, "pass")));

        assertTrue(service.emailExists(" HAMADA@EMAIL.COM "));
    }

    @Test
    public void testEmailExistsFalse() {
        when(customerRepository.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertFalse(service.emailExists("missing@email.com"));
    }

    @Test
    public void testDeleteAccount() {
        when(customerRepository.deleteByEmail("hamada@email.com"))
                .thenReturn(true);

        boolean result = service.deleteAccount(" HAMADA@EMAIL.COM ");

        assertTrue(result);

        verify(customerRepository).deleteByEmail("hamada@email.com");
    }

    @Test
    public void testGetAllAccounts() {
        List<Customer> accounts = List.of(
                new Customer(
                        "Hamada",
                        "hamada@email.com",
                        "0599999999"
                ),
                new Customer(
                        "Admin",
                        "admin@velora.com",
                        "",
                        Role.MANAGER
                )
        );

        when(customerRepository.findAll()).thenReturn(accounts);

        List<Customer> result = service.getAllAccounts();

        assertSame(accounts, result);

        verify(customerRepository).findAll();
    }
}
