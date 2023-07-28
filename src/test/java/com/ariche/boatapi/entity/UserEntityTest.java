package com.ariche.boatapi.entity;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserEntityTest {

    @Test
    void test_basics() {
        final UserEntity entity = new UserEntity();
        entity.setId(24L);
        entity.setLogin("login");
        entity.setPassword("password");
        entity.setFirstName("first");
        entity.setLastName("last");
        entity.setEmail("email");
        entity.setActivated(true);
        entity.setAuthorities(Collections.emptyList());

        assertEquals(24L, entity.getId());
        assertEquals("login", entity.getLogin());
        assertEquals("password", entity.getPassword());
        assertEquals("first", entity.getFirstName());
        assertEquals("last", entity.getLastName());
        assertEquals("email", entity.getEmail());
        assertTrue(entity.isActivated());
        assertTrue(entity.getAuthorities().isEmpty());
    }

}
