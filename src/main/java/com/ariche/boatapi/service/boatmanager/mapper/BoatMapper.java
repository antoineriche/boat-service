package com.ariche.boatapi.service.boatmanager.mapper;

import com.ariche.boatapi.entity.BoatEntity;
import com.ariche.boatapi.service.boatmanager.dto.BoatDTO;

public final class BoatMapper {

    /**
     * Map a {@link BoatEntity} to a {@link BoatDTO}
     * @param boatEntity the boat entity to be mapped
     * @return the {@link BoatDTO}
     */
    public static BoatDTO toDTO(final BoatEntity boatEntity) {
        return new BoatDTO(boatEntity.getId(), boatEntity.getName(), boatEntity.getDescription(), null);
    }

    /**
     * Map a {@link BoatDTO} to a {@link BoatEntity}
     * @param boatDTO the boat dto to be mapped
     * @return the {@link BoatEntity}
     */
    public static BoatEntity toEntity(final BoatDTO boatDTO) {
        final BoatEntity entity = new BoatEntity();
        entity.setId(boatDTO.id());
        entity.setName(boatDTO.name());
        entity.setDescription(boatDTO.description());
        return entity;
    }

    /**
     * Update the given BoatEntity with BoatDTO values
     * @param boatDTO the BoatDTO containing new values
     * @param boatEntity the BoatEntity currently stored in database
     */
    public static void updateBoat(final BoatDTO boatDTO,
                                  final BoatEntity boatEntity) {
        boatEntity.setName(boatDTO.name());
        boatEntity.setDescription(boatDTO.description());
    }

    private BoatMapper() {
    }
}
