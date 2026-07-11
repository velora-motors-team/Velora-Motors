package com.velora.service;

import com.velora.authentication.Customer;
import com.velora.authentication.Customer.Role;
import com.velora.repository.CustomerRepository;
import com.velora.repository.CustomerRepository.StoredCustomer;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class AuthenticationService {

    private final CustomerRepository customerRepository;

    public AuthenticationService() {
        this(new CustomerRepository());
    }

    public AuthenticationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        createDefaultAccounts();
    }

    public Customer registerCustomer(
            String fullName,
            String email,
            String phone,
            char[] password
    ) {
        return registerAccount(
                fullName,
                email,
                phone,
                Role.CUSTOMER,
                password
        );
    }

    public Optional<Customer> authenticateCustomer(String email, char[] password) {
        String normalizedEmail = normalizeEmail(email);
        Optional<StoredCustomer> stored = customerRepository.findByEmail(normalizedEmail);

        if (stored.isEmpty()) {
            return Optional.empty();
        }

        StoredCustomer account = stored.get();
        String typedPassword = passwordToString(password);

        return account.password().equals(typedPassword)
                ? Optional.of(account.customer())
                : Optional.empty();
    }

    public boolean resetPassword(String email, char[] newPassword) {
        String normalizedEmail = normalizeEmail(email);

        if (customerRepository.findByEmail(normalizedEmail).isEmpty()) {
            return false;
        }

        String plainPassword = passwordToString(newPassword);
        return customerRepository.updatePassword(normalizedEmail, plainPassword);
    }

    public boolean updateProfile(String email, String fullName, String phone) {
        String normalizedEmail = normalizeEmail(email);

        if (customerRepository.findByEmail(normalizedEmail).isEmpty()) {
            return false;
        }

        return customerRepository.updateProfile(normalizedEmail, fullName, phone);
    }

    public Path getAccountsFile() {
        return customerRepository.getAccountsFile();
    }

    public boolean emailExists(String email) {
        return customerRepository.findByEmail(normalizeEmail(email)).isPresent();
    }

    public boolean deleteAccount(String email) {
        return customerRepository.deleteByEmail(normalizeEmail(email));
    }

    public List<Customer> getAllAccounts() {
        return customerRepository.findAll();
    }

    public Customer registerAccount(
            String fullName,
            String email,
            String phone,
            Role role,
            char[] password
    ) {
        String normalizedName = safeTrim(fullName);
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = safeTrim(phone);
        String plainPassword = passwordToString(password);

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (customerRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        Customer customer = new Customer(
                normalizedName,
                normalizedEmail,
                normalizedPhone,
                role
        );

        customerRepository.save(customer, plainPassword);
        return customer;
    }

    private void createDefaultAccounts() {
        createDefaultAccount(
                "Main Manager",
                "manager@velora.com",
                "",
                Role.MANAGER,
                "Manager@123"
        );
        createDefaultAccount(
                "System Admin",
                "admin@velora.com",
                "",
                Role.MANAGER,
                "Admin@123"
        );
        createDefaultAccount(
                "Ahmad Ali",
                "ahmad@gmail.com",
                "",
                Role.CUSTOMER,
                "Ahmad@123"
        );
        createDefaultAccount(
                "Sara",
                "sara@gmail.com",
                "0599326499",
                Role.CUSTOMER,
                "Sara@123"
        );
    }

    private void createDefaultAccount(
            String fullName,
            String email,
            String phone,
            Role role,
            String password
    ) {
        if (customerRepository.findByEmail(email).isPresent()) {
            return;
        }

        registerAccount(
                fullName,
                email,
                phone,
                role,
                password.toCharArray()
        );
    }

    private static String normalizeEmail(String email) {
        return safeTrim(email).toLowerCase(Locale.ROOT);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String passwordToString(char[] password) {
        return password == null ? "" : new String(password);
    }
}
