package com.ariche.boatapi.service.storage;

import com.ariche.boatapi.service.storage.dto.EStorageError;
import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import com.ibm.cloud.objectstorage.SdkClientException;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CloudStorageService implements StorageService {

    private final AmazonS3 cloudCOSClient;
    private final String bucketName;

    @Override
    public void storeFile(MultipartFile multipartFile, String name, String folder, boolean replace) throws StorageException {
        log.debug("Storing file: {}", name);

        try {
            storeFile(name, folder, multipartFile.getInputStream(), multipartFile.getContentType(), replace);
            log.debug("InputStream successfully saved on the cloud");
        } catch (IOException e) {
            log.error("Could not get Input Stream from input file: {}", e.getMessage(), e);
            throw new StorageException("Error getting input stream from file", e);
        } catch (SdkClientException e) {
            log.error("Error while calling COS-Service: {}", e.getMessage(), e);
            throw new StorageException("Error while saving MultipartFile", e);
        }
    }

    @Override
    public FileResourceDTO downloadFile(String fileName, String folder) throws StorageException {
        log.debug("Downloading file '{}'", fileName);
        try {
            if (!cloudCOSClient.doesObjectExist(buildFolderPath(folder), fileName)) {
                throw new StorageException("File not found: %s".formatted(fileName));
            } else {
                final S3Object object = cloudCOSClient.getObject(buildFolderPath(folder), fileName);
                final String mimeType = Optional.ofNullable(object.getObjectMetadata())
                    .map(ObjectMetadata::getContentType)
                    .orElse(null);
                return new FileResourceDTO(mimeType, object.getObjectContent().readAllBytes());
            }
        } catch (SdkClientException e) {
            throw new StorageException(EStorageError.TRANSFER_ERROR, "Error while calling COS-Service", e);
        } catch (IOException e) {
            throw new StorageException("Error while saving InputStream", e);
        }
    }

    @Override
    public void deleteFile(String fileName, String folder) throws StorageException {
        log.debug("Deleting file {}", fileName);
        if (cloudCOSClient.doesObjectExist(buildFolderPath(folder), fileName)) {
            cloudCOSClient.deleteObject(buildFolderPath(folder), fileName);
        }

        if (!cloudCOSClient.doesObjectExist(buildFolderPath(folder), fileName)) {
            log.debug("File '{}' successfully deleted", fileName);
        } else {
            throw new StorageException("file was not deleted");
        }
    }

    public void storeFile(final String name,
                          final String folder,
                          final InputStream stream,
                          final String contentType,
                          final boolean replace) throws IOException {

        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(stream.available());
        if (!replace
            && cloudCOSClient.doesObjectExist(buildFolderPath(folder), name)) {
            throw new StorageException(EStorageError.FILE_ALREADY_EXIST, name);
        }

        cloudCOSClient.putObject(buildFolderPath(folder), name, stream, metadata);
    }

    public String buildFolderPath(final String folder) {
        return "%s/%s".formatted(bucketName, folder);
    }
}
