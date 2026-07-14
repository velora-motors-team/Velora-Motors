package com.velora.authentication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    @Test
    public void testDefaultCustomerConstructor() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "HAMADA@EMAIL.COM",
                "0599999999"
        );

        assertEquals("Hamada Ahmad", customer.getFullName());
        assertEquals("hamada@email.com", customer.getEmail());
        assertEquals("0599999999", customer.getPhone());
        assertEquals(Customer.Role.CUSTOMER, customer.getRole());
        assertTrue(customer.isCustomer());
        assertFalse(customer.isManager());
    }

    @Test
    public void testManagerConstructor() {
        Customer customer = new Customer(
                "Admin User",
                "admin@velora.com",
                "0591111111",
                Customer.Role.MANAGER
        );

        assertEquals(Customer.Role.MANAGER, customer.getRole());
        assertTrue(customer.isManager());
        assertFalse(customer.isCustomer());
    }

    @Test
    public void testTrimValues() {
        Customer customer = new Customer(
                "   Hamada Ahmad   ",
                "   HAMADA@EMAIL.COM   ",
                "   0599999999   "
        );

        assertEquals("Hamada Ahmad", customer.getFullName());
        assertEquals("hamada@email.com", customer.getEmail());
        assertEquals("0599999999", customer.getPhone());
    }

    @Test
    public void testNullFullName() {
        Customer customer = new Customer(
                null,
                "test@email.com",
                "0599999999"
        );

        assertEquals("", customer.getFullName());
    }

    @Test
    public void testNullEmail() {
        Customer customer = new Customer(
                "Test User",
                null,
                "0599999999"
        );

        assertEquals("", customer.getEmail());
    }

    @Test
    public void testNullPhone() {
        Customer customer = new Customer(
                "Test User",
                "test@email.com",
                null
        );

        assertEquals("", customer.getPhone());
    }

    @Test
    public void testNullRoleDefaultsToCustomer() {
        Customer customer = new Customer(
                "Test User",
                "test@email.com",
                "0599999999",
                null
        );

        assertEquals(Customer.Role.CUSTOMER, customer.getRole());
        assertTrue(customer.isCustomer());
        assertFalse(customer.isManager());
    }

    @Test
    public void testAllNullValues() {
        Customer customer = new Customer(null, null, null, null);

        assertEquals("", customer.getFullName());
        assertEquals("", customer.getEmail());
        assertEquals("", customer.getPhone());
        assertEquals(Customer.Role.CUSTOMER, customer.getRole());
    }

    @Test
    public void testToString() {
        Customer customer = new Customer(
                "Hamada Ahmad",
                "hamada@email.com",
                "0599999999",
                Customer.Role.CUSTOMER
        );

        assertEquals(
                "Hamada Ahmad <hamada@email.com> - CUSTOMER",
                customer.toString()
        );
    }

    @Test
    public void testRoleValues() {
        Customer.Role[] roles = Customer.Role.values();

        assertEquals(2, roles.length);

        assertArrayEquals(
                new Customer.Role[]{
                        Customer.Role.MANAGER,
                        Customer.Role.CUSTOMER
                },
                roles
        );
    }

    @Test
    public void testRoleValueOf() {
        assertEquals(
                Customer.Role.MANAGER,
                Customer.Role.valueOf("MANAGER")
        );

        assertEquals(
                Customer.Role.CUSTOMER,
                Customer.Role.valueOf("CUSTOMER")
        );
    }
}