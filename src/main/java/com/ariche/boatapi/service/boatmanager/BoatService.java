package com.ariche.boatapi.service.boatmanager;

import com.ariche.boatapi.entity.BoatEntity;
import com.ariche.boatapi.errors.BoatAPIException;
import com.ariche.boatapi.errors.EBoatAPIError;
import com.ariche.boatapi.repository.BoatRepository;
import com.ariche.boatapi.repository.customset.IBoatImgSet;
import com.ariche.boatapi.service.boatmanager.dto.BoatDTO;
import com.ariche.boatapi.service.boatmanager.mapper.BoatMapper;
import com.ariche.boatapi.service.storage.StorageException;
import com.ariche.boatapi.service.storage.StorageService;
import com.ariche.boatapi.service.storage.dto.EStorageError;
import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoatService {

    private final BoatRepository boatRepository;
    private final StorageService storageService;

    /**
     * Find a boat by id
     * @param id the id of the boat
     * @return an optional representation of the BoatDTO matching given id
     */
    public Optional<BoatDTO> findBoatById(final Long id) {
        return boatRepository.findById(id)
            .map(BoatMapper::toDTO);
    }

    /**`
     * Find all boats
     * @param pageable the pageable to paginate and sort boats
     * @return representation of all BoatDTO
     */
    public Page<BoatDTO> findALlBoats(final Pageable pageable) {
        return boatRepository.findAll(pageable)
            .map(BoatMapper::toDTO);
    }

    /**
     * Delete the boat matching given id
     * @param boatId the id of the boat
     */
    @Transactional(readOnly = false)
    public void deleteBoatById(final Long boatId) {
        boatRepository.findBoatImgById(boatId).ifPresent(iBoatImgSet -> {
            boatRepository.deleteById(iBoatImgSet.getId());
            if (StringUtils.isNotBlank(iBoatImgSet.getImgName())) {
                storageService.deleteFileQuietly(iBoatImgSet.getImgName(), buildBoatStorageFolder(iBoatImgSet.getId()));
            }
        });
    }

    /**
     * Create new Boat from incoming dto
     * @param boat the boat to be created
     * @return the created boat
     */
    @Transactional(readOnly = false)
    public BoatDTO createBoat(final BoatDTO boat) {
        if (Objects.nonNull(boat.id())) {
            throw new BoatAPIException(EBoatAPIError.INVALID_REQUEST, "Id must be blank");
        }
        BoatEntity entity = BoatMapper.toEntity(boat);
        entity = boatRepository.save(entity);
        return BoatMapper.toDTO(entity);
    }

    /**
     * Update Boat identified by its id with incoming BoatDTO values
     * @param id the id of th e boat to be updated
     * @param boat the BoatDTO containing new values
     * @return the updated BoatEntity representation
     */
    @Transactional(readOnly = false)
    public BoatDTO updateBoat(final Long id,
                              final BoatDTO boat) {
        if (!Objects.equals(id, boat.id())) {
            throw new BoatAPIException(EBoatAPIError.INVALID_REQUEST, "Inconsistent ids");
        }
        final BoatEntity entity = boatRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Boat not found"));

        BoatMapper.updateBoat(boat, entity);
        return BoatMapper.toDTO(boatRepository.save(entity));
    }

    /**
     * Upload an image for boat identified by its id
     * @param boatId the boat id
     * @param file the image file
     * @apiNote the current solution force us to manually-handle the versioning of the file and to regard on the file name
     * with a embedded-versioning database such as MongoDB, we could avoid this by fetching only the latest version of
     * the image and keeping only the document id in the database.
     * @see {@link StorageService}
     */
    @Transactional(readOnly = false)
    public void uploadImageForBoat(Long boatId, MultipartFile file) {
        ensureIncomingFile(file, List.of(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE));
        final IBoatImgSet imgSet = boatRepository.findBoatImgById(boatId)
            .orElseThrow(() -> new EntityNotFoundException("Boat not found"));

        final String name = file.getOriginalFilename();
        final String folder = buildBoatStorageFolder(boatId);
        storageService.storeFile(file, name, folder, true);

        boatRepository.updateImgNameByBoatId(boatId, name);

        if (StringUtils.isNotBlank(imgSet.getImgName())
            && !StringUtils.equals(imgSet.getImgName(), name)) {    // prevent from removing just-added image
            storageService.deleteFileQuietly(imgSet.getImgName(), folder);
        }
    }

    /**
     * Download image for boat identified by its id
     * @param boatId the boat id
     * @return true if the image can be stored, false otherwise
     */
    public Optional<FileResourceDTO> downloadImageForBoat(Long boatId) {
        final Optional<String> optBoat = boatRepository.fetchImgNameById(boatId);
        if (optBoat.isEmpty()) {
            log.warn("Boat ({}) not found", boatId);
            return Optional.empty();
        }

        try {
            return Optional.of(storageService.downloadFile(optBoat.get(), buildBoatStorageFolder(boatId)));
        } catch (StorageException e) {
            log.warn("Could not get image: {}", e.getMessage());
            return Optional.empty();
        }
    }

    static String buildBoatStorageFolder(final Long boatId) {
        return "boat-%09d".formatted(boatId);
    }

    static void ensureIncomingFile(final MultipartFile multipartFile,
                                   final Collection<String> acceptedContentTypes) {
        if (multipartFile.isEmpty()) {
            log.error("Empty file was sent");
            throw new StorageException(EStorageError.WRONG_FILE_FORMAT, "Empty file");
        }

        if (StringUtils.isBlank(multipartFile.getContentType())
            || !acceptedContentTypes.contains(multipartFile.getContentType())) {
            log.error("Invalid file was sent (Content-Type: {})", multipartFile.getContentType());
            throw new StorageException(EStorageError.WRONG_FILE_FORMAT, "Invalid file format");
        }
    }
}
