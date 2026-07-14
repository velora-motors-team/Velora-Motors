package com.velora.authentication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagerTest {

    @Test
    public void testNormalConstructor() {
        Manager manager = new Manager(
                "Hamada Ahmad",
                "hamada@velora.com"
        );

        assertEquals("Hamada Ahmad", manager.getFullName());
        assertEquals("hamada@velora.com", manager.getEmail());
    }

    @Test
    public void testTrimAndLowercase() {
        Manager manager = new Manager(
                "   Hamada Ahmad   ",
                "   HAMADA@VELORA.COM   "
        );

        assertEquals("Hamada Ahmad", manager.getFullName());
        assertEquals("hamada@velora.com", manager.getEmail());
    }

    @Test
    public void testNullFullName() {
        Manager manager = new Manager(
                null,
                "manager@velora.com"
        );

        assertEquals("", manager.getFullName());
        assertEquals("manager@velora.com", manager.getEmail());
    }

    @Test
    public void testNullEmail() {
        Manager manager = new Manager(
                "Manager User",
                null
        );

        assertEquals("Manager User", manager.getFullName());
        assertEquals("", manager.getEmail());
    }

    @Test
    public void testAllNullValues() {
        Manager manager = new Manager(null, null);

        assertEquals("", manager.getFullName());
        assertEquals("", manager.getEmail());
    }

    @Test
    public void testToString() {
        Manager manager = new Manager(
                "Hamada Ahmad",
                "HAMADA@VELORA.COM"
        );

        assertEquals(
                "Hamada Ahmad <hamada@velora.com>",
                manager.toString()
        );
    }
}