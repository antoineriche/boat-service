package com.ariche.boatapi.service.storage;

import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * We could use a MongoDB base to store the object such as images to handle multi-versions
 * and prevent from custom-home-made file versioning (delete previous image and keep only the latest raw name
 * in a relational-database.
 * Saving only the id, also enables us to do not regard about the name of the incoming file.
 */
public interface StorageService {

    void storeFile(final MultipartFile multipartFile,
                   final String name,
                   final String folder,
                   final boolean replace) throws StorageException;

    default void storeFile(final MultipartFile multipartFile,
                           final String name,
                           final String folder) throws StorageException {
        storeFile(multipartFile, name, folder, false);
    }

    FileResourceDTO downloadFile(final String fileName,
                                 final String folder) throws StorageException;

    void deleteFile(final String fileName,
                    final String folder) throws StorageException;

    default void deleteFileQuietly(final String fileName,
                                   final String folder) {
        try {
            deleteFile(fileName, folder);
        } catch (StorageException ignored) {
            // ignored
        }
    }

}
