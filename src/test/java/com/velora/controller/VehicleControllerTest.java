package com.velora.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VehicleControllerTest {

    @Test
    void canBeCreated() {
        assertNotNull(new VehicleController());
    }
}
