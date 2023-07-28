package com.ariche.boatapi.service.storage;

import com.ariche.boatapi.service.storage.dto.EStorageError;
import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {
    private final String storageFolderPath;

    @Override
    public void storeFile(MultipartFile multipartFile, String name, String folder, boolean replace) throws StorageException {
        log.debug("Storing file: {} in {}", name, folder);
        try {
            final File folderDirectory = new File(getStorageFolderPath(), folder);
            if (!folderDirectory.exists()) {
                Files.createDirectories(folderDirectory.toPath());
            }
            if (replace) {
                Files.copy(multipartFile.getInputStream(), new File(folderDirectory, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(multipartFile.getInputStream(), new File(folderDirectory, name).toPath());
            }
        } catch (FileAlreadyExistsException e) {
            throw new StorageException(EStorageError.FILE_ALREADY_EXIST,
                "Could not store file: %s".formatted(e.getMessage()),
                e);
        } catch (IOException e) {
            throw new StorageException(
                EStorageError.TECHNICAL_ERROR,
                "Could not store file: %s".formatted(e.getMessage()),
                e);
        }
    }

    @Override
    public FileResourceDTO downloadFile(String fileName, String folder) throws StorageException {
        log.debug("Downloading file: {}", fileName);
        final File parentFolder = new File(getStorageFolder(), folder);
        final File imgFile = new File(parentFolder, fileName);
        try (final InputStream stream = new FileInputStream(imgFile)) {
            final String mimeType = Files.probeContentType(imgFile.toPath());
            return new FileResourceDTO(mimeType, stream.readAllBytes());
        } catch (FileNotFoundException e) {
            throw new StorageException(EStorageError.FILE_NOT_FOUND, fileName, e);
        } catch (IOException e) {
            throw new StorageException(
                "Could not download file: %s".formatted(e.getMessage()),
                e);
        }
    }

    @Override
    public void deleteFile(String fileName, String folder) {
        log.debug("Deleting file: {}", fileName);
        try {
            final File parentFolder = new File(getStorageFolder(), folder);
            Files.deleteIfExists(new File(parentFolder, fileName).toPath());
        } catch (IOException e) {
            log.warn("File %s could not be deleted: %s".formatted(fileName, e.getMessage()));
        }
    }

    @PostConstruct
    public void createStorageFolder() {
        if (!getStorageFolder().exists()) {
            try {
                Files.createDirectories(Paths.get(storageFolderPath));
                log.debug("{} folder has been created", storageFolderPath);
            } catch (IOException e) {
                log.error("Could not create directories: '{}'", storageFolderPath, e);
                throw new StorageException(EStorageError.INIT_STORAGE_SYSTEM_ERROR, "Could not create local storage directories");
            }
        }
    }


    public String getStorageFolderPath() {
        return storageFolderPath;
    }

    public File getStorageFolder() {
        return new File(storageFolderPath);
    }
}
