package com.ariche.boatapi.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoatEntityTest {

    @Test
    void test_basics() {
        final BoatEntity entity = new BoatEntity();
        entity.setId(24L);
        entity.setName("name");
        entity.setDescription("description");
        entity.setImgName("img");

        assertEquals(24L, entity.getId());
        assertEquals("name", entity.getName());
        assertEquals("description", entity.getDescription());
        assertEquals("img", entity.getImgName());
    }

}
