package com.velora.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagerRepositoryTest {

    @Test
    public void testConstructorCreatesObject() {
        ManagerRepository repository =
                new ManagerRepository();

        assertNotNull(repository);
    }
}