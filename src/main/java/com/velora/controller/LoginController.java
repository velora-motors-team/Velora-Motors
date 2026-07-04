package com.velora.controller;

import com.velora.authentication.Customer;
import com.velora.authentication.Customer.Role;
import com.velora.service.AuthenticationService;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class LoginController {

    private final AuthenticationService authenticationService;

    public LoginController() {
        this.authenticationService = new AuthenticationService();
    }

    public LoginController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public Optional<Customer> login(String email, String password) {
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return Optional.empty();
        }

        return authenticationService.authenticateCustomer(
                email,
                password.toCharArray()
        );
    }

    public Customer createCustomerAccount(
            String fullName,
            String email,
            String phone,
            String password
    ) {
        validateAccountFields(fullName, email, password);

        return authenticationService.registerCustomer(
                fullName,
                email,
                phone == null ? "" : phone,
                password.toCharArray()
        );
    }

    public Customer createManagerAccount(
            String fullName,
            String email,
            String phone,
            String password
    ) {
        validateAccountFields(fullName, email, password);

        return authenticationService.registerAccount(
                fullName,
                email,
                phone == null ? "" : phone,
                Role.MANAGER,
                password.toCharArray()
        );
    }

    public boolean resetPassword(
            String email,
            String newPassword,
            String confirmPassword
    ) {
        if (email == null || email.isBlank()) {
            return false;
        }

        if (newPassword == null || newPassword.isBlank()) {
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            return false;
        }

        return authenticationService.resetPassword(
                email,
                newPassword.toCharArray()
        );
    }

    public boolean deleteAccount(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return authenticationService.deleteAccount(email);
    }

    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return authenticationService.emailExists(email);
    }

    public List<Customer> getAllAccounts() {
        return authenticationService.getAllAccounts();
    }

    public Path getAccountsFile() {
        return authenticationService.getAccountsFile();
    }

    public boolean isManager(Customer customer) {
        return customer != null && customer.getRole() == Role.MANAGER;
    }

    public boolean isCustomer(Customer customer) {
        return customer != null && customer.getRole() == Role.CUSTOMER;
    }

    private void validateAccountFields(
            String fullName,
            String email,
            String password
    ) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }
}