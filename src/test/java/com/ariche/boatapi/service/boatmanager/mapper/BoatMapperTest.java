package com.ariche.boatapi.service.boatmanager.mapper;

import com.ariche.boatapi.entity.BoatEntity;
import com.ariche.boatapi.service.boatmanager.dto.BoatDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoatMapperTest {

    @Test
    void test_ToDTO() {
        final BoatEntity entity = new BoatEntity();
        entity.setId(19L);
        entity.setName("name");
        entity.setDescription("description");

        final BoatDTO dto = BoatMapper.toDTO(entity);
        assertEquals(19L, dto.id());
        assertEquals("name", dto.name());
        assertEquals("description", dto.description());
    }

    @Test
    void test_ToEntity() {
        final BoatDTO dto = new BoatDTO(4L, "name", "description");

        final BoatEntity entity = BoatMapper.toEntity(dto);
        assertEquals(4L, entity.getId());
        assertEquals("name", entity.getName());
        assertEquals("description", entity.getDescription());
    }

    @Test
    void test_UpdateBoat() {
        final BoatDTO dto = new BoatDTO(null, "name", "description");
        final BoatEntity entity = new BoatEntity();
        entity.setId(19L);
        entity.setName("name");
        entity.setDescription("description");

        BoatMapper.updateBoat(dto, entity);

        assertEquals(19L, entity.getId());
        assertEquals(dto.name(), entity.getName());
        assertEquals(dto.description(), entity.getDescription());
    }
}
