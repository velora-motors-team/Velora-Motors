package com.velora.authentication;

public final class Manager {

    private final String fullName;
    private final String email;

    public Manager(String fullName, String email) {
        this.fullName = fullName == null ? "" : fullName.trim();
        this.email = email == null ? "" : email.trim().toLowerCase();
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return fullName + " <" + email + ">";
    }
}