package com.velora.authentication;

public final class Customer {

    public enum Role {
        MANAGER,
        CUSTOMER
    }

    private final String fullName;
    private final String email;
    private final String phone;
    private final Role role;

    public Customer(String fullName, String email, String phone) {
        this(fullName, email, phone, Role.CUSTOMER);
    }

    public Customer(String fullName, String email, String phone, Role role) {
        this.fullName = fullName == null ? "" : fullName.trim();
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.phone = phone == null ? "" : phone.trim();
        this.role = role == null ? Role.CUSTOMER : role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    @Override
    public String toString() {
        return fullName + " <" + email + "> - " + role;
    }
}