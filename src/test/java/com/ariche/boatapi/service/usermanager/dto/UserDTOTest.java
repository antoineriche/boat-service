package com.ariche.boatapi.service.usermanager.dto;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDTOTest {

    @Test
    void test_basics() {
        final UserDTO dto = new UserDTO(12L, "login", Collections.singletonList("test"));
        assertEquals(12L, dto.id());
        assertEquals("login", dto.login());
        assertEquals(1, dto.authorities().size());
        assertTrue(dto.authorities().contains("test"));
    }
}
