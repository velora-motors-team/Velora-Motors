package com.velora.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DashboardControllerTest {

    @Test
    void canBeCreated() {
        assertNotNull(new DashboardController());
    }
}
