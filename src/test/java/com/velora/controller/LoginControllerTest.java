package com.velora.controller;

import com.velora.authentication.Customer;
import com.velora.authentication.Customer.Role;
import com.velora.service.AuthenticationService;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LoginControllerTest {

    @Test
    public void testDefaultConstructor(@TempDir Path tempDirectory) {
        String previous = System.getProperty("velora.accounts.file");

        try {
            System.setProperty(
                    "velora.accounts.file",
                    tempDirectory.resolve("accounts.txt").toString()
            );
            assertNotNull(new LoginController());
        } finally {
            if (previous == null) {
                System.clearProperty("velora.accounts.file");
            } else {
                System.setProperty("velora.accounts.file", previous);
            }
        }
    }

    private AuthenticationService authenticationService;
    private LoginController controller;

    @BeforeEach
    public void setUp() {
        authenticationService = mock(AuthenticationService.class);
        controller = new LoginController(authenticationService);
    }

    @Test
    public void testLoginSuccess() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999"
        );

        when(authenticationService.authenticateCustomer(
                eq("hamada@email.com"),
                argThat(password -> new String(password).equals("password123"))
        )).thenReturn(Optional.of(customer));

        Optional<Customer> result =
                controller.login("hamada@email.com", "password123");

        assertTrue(result.isPresent());
        assertSame(customer, result.get());

        verify(authenticationService).authenticateCustomer(
                eq("hamada@email.com"),
                argThat(password -> new String(password).equals("password123"))
        );
    }

    @Test
    public void testLoginNullEmail() {
        Optional<Customer> result = controller.login(null, "password123");

        assertTrue(result.isEmpty());
        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testLoginBlankEmail() {
        Optional<Customer> result = controller.login("   ", "password123");

        assertTrue(result.isEmpty());
        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testLoginNullPassword() {
        Optional<Customer> result = controller.login("user@email.com", null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testLoginBlankPassword() {
        Optional<Customer> result = controller.login("user@email.com", "   ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testLoginFailure() {
        when(authenticationService.authenticateCustomer(
                eq("wrong@email.com"),
                any(char[].class)
        )).thenReturn(Optional.empty());

        Optional<Customer> result =
                controller.login("wrong@email.com", "wrongpass");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreateCustomerAccountSuccess() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999"
        );

        when(authenticationService.registerCustomer(
                eq("Hamada Ahmad"),
                eq("hamada@email.com"),
                eq("0599999999"),
                any(char[].class)
        )).thenReturn(customer);

        Customer result = controller.createCustomerAccount(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999",
                "password123"
        );

        assertSame(customer, result);

        verify(authenticationService).registerCustomer(
                eq("Hamada Ahmad"),
                eq("hamada@email.com"),
                eq("0599999999"),
                argThat(password -> new String(password).equals("password123"))
        );
    }

    @Test
    public void testCreateCustomerAccountNullPhone() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                ""
        );

        when(authenticationService.registerCustomer(
                eq("Hamada Ahmad"),
                eq("hamada@email.com"),
                eq(""),
                any(char[].class)
        )).thenReturn(customer);

        Customer result = controller.createCustomerAccount(
                "Hamada Ahmad",
                "hamada@email.com",
                null,
                "password123"
        );

        assertSame(customer, result);

        verify(authenticationService).registerCustomer(
                eq("Hamada Ahmad"),
                eq("hamada@email.com"),
                eq(""),
                any(char[].class)
        );
    }

    @Test
    public void testCreateManagerAccountSuccess() {
        Customer manager = new Customer(
                "Admin User",
                "admin@velora.com",
                "0591111111",
                Role.MANAGER
        );

        when(authenticationService.registerAccount(
                eq("Admin User"),
                eq("admin@velora.com"),
                eq("0591111111"),
                eq(Role.MANAGER),
                any(char[].class)
        )).thenReturn(manager);

        Customer result = controller.createManagerAccount(
                "Admin User",
                "admin@velora.com",
                "0591111111",
                "manager123"
        );

        assertSame(manager, result);

        verify(authenticationService).registerAccount(
                eq("Admin User"),
                eq("admin@velora.com"),
                eq("0591111111"),
                eq(Role.MANAGER),
                argThat(password -> new String(password).equals("manager123"))
        );
    }

    @Test
    public void testCreateManagerAccountNullPhone() {
        Customer manager = new Customer(
                "Admin User",
                "admin@velora.com",
                "",
                Role.MANAGER
        );

        when(authenticationService.registerAccount(
                eq("Admin User"),
                eq("admin@velora.com"),
                eq(""),
                eq(Role.MANAGER),
                any(char[].class)
        )).thenReturn(manager);

        Customer result = controller.createManagerAccount(
                "Admin User",
                "admin@velora.com",
                null,
                "manager123"
        );

        assertSame(manager, result);

        verify(authenticationService).registerAccount(
                eq("Admin User"),
                eq("admin@velora.com"),
                eq(""),
                eq(Role.MANAGER),
                any(char[].class)
        );
    }

    @Test
    public void testCreateAccountNullFullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        null,
                        "user@email.com",
                        "0599999999",
                        "password123"
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountBlankFullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "   ",
                        "user@email.com",
                        "0599999999",
                        "password123"
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountNullEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "Test User",
                        null,
                        "0599999999",
                        "password123"
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "Test User",
                        "   ",
                        "0599999999",
                        "password123"
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountEmailWithoutAt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "Test User",
                        "useremail.com",
                        "0599999999",
                        "password123"
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountEmailWithoutDot() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "Test User",
                        "user@emailcom",
                        "0599999999",
                        "password123"
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "Test User",
                        "user@email.com",
                        "0599999999",
                        null
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountBlankPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "Test User",
                        "user@email.com",
                        "0599999999",
                        "   "
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testCreateAccountShortPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createCustomerAccount(
                        "Test User",
                        "user@email.com",
                        "0599999999",
                        "12345"
                )
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testResetPasswordSuccess() {
        when(authenticationService.resetPassword(
                eq("user@email.com"),
                any(char[].class)
        )).thenReturn(true);

        boolean result = controller.resetPassword(
                "user@email.com",
                "newpass123",
                "newpass123"
        );

        assertTrue(result);

        verify(authenticationService).resetPassword(
                eq("user@email.com"),
                argThat(password -> new String(password).equals("newpass123"))
        );
    }

    @Test
    public void testResetPasswordNullEmail() {
        assertFalse(controller.resetPassword(
                null,
                "newpass123",
                "newpass123"
        ));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testResetPasswordBlankEmail() {
        assertFalse(controller.resetPassword(
                "   ",
                "newpass123",
                "newpass123"
        ));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testResetPasswordNullNewPassword() {
        assertFalse(controller.resetPassword(
                "user@email.com",
                null,
                null
        ));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testResetPasswordBlankNewPassword() {
        assertFalse(controller.resetPassword(
                "user@email.com",
                "   ",
                "   "
        ));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testResetPasswordMismatch() {
        assertFalse(controller.resetPassword(
                "user@email.com",
                "password1",
                "password2"
        ));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testDeleteAccountSuccess() {
        when(authenticationService.deleteAccount("user@email.com"))
                .thenReturn(true);

        assertTrue(controller.deleteAccount("user@email.com"));

        verify(authenticationService).deleteAccount("user@email.com");
    }

    @Test
    public void testDeleteAccountNullEmail() {
        assertFalse(controller.deleteAccount(null));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testDeleteAccountBlankEmail() {
        assertFalse(controller.deleteAccount("   "));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testEmailExistsTrue() {
        when(authenticationService.emailExists("user@email.com"))
                .thenReturn(true);

        assertTrue(controller.emailExists("user@email.com"));

        verify(authenticationService).emailExists("user@email.com");
    }

    @Test
    public void testEmailExistsNullEmail() {
        assertFalse(controller.emailExists(null));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testEmailExistsBlankEmail() {
        assertFalse(controller.emailExists("   "));

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void testGetAllAccounts() {
        List<Customer> accounts = List.of(
                new Customer(
                        "User One",
                        "one@email.com",
                        "0591111111"
                ),
                new Customer(
                        "User Two",
                        "two@email.com",
                        "0592222222",
                        Role.MANAGER
                )
        );

        when(authenticationService.getAllAccounts()).thenReturn(accounts);

        List<Customer> result = controller.getAllAccounts();

        assertSame(accounts, result);
        verify(authenticationService).getAllAccounts();
    }

    @Test
    public void testGetAccountsFile() {
        Path path = Path.of("database", "accounts.txt");

        when(authenticationService.getAccountsFile()).thenReturn(path);

        Path result = controller.getAccountsFile();

        assertEquals(path, result);
        verify(authenticationService).getAccountsFile();
    }

    @Test
    public void testIsManagerTrue() {
        Customer manager = new Customer(
                "Admin User",
                "admin@velora.com",
                "0591111111",
                Role.MANAGER
        );

        assertTrue(controller.isManager(manager));
    }

    @Test
    public void testIsManagerFalseForCustomer() {
        Customer customer = new Customer(
                "Customer User",
                "customer@email.com",
                "0592222222",
                Role.CUSTOMER
        );

        assertFalse(controller.isManager(customer));
    }

    @Test
    public void testIsManagerFalseForNull() {
        assertFalse(controller.isManager(null));
    }

    @Test
    public void testIsCustomerTrue() {
        Customer customer = new Customer(
                "Customer User",
                "customer@email.com",
                "0592222222",
                Role.CUSTOMER
        );

        assertTrue(controller.isCustomer(customer));
    }

    @Test
    public void testIsCustomerFalseForManager() {
        Customer manager = new Customer(
                "Admin User",
                "admin@velora.com",
                "0591111111",
                Role.MANAGER
        );

        assertFalse(controller.isCustomer(manager));
    }

    @Test
    public void testIsCustomerFalseForNull() {
        assertFalse(controller.isCustomer(null));
    }
}
