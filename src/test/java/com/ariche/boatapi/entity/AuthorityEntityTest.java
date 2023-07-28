package com.ariche.boatapi.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorityEntityTest {

    @Test
    void test_basics() {
        final AuthorityEntity entity = new AuthorityEntity();
        entity.setName("name");
        assertEquals("name", entity.getName());
    }
}
