package com.ariche.boatapi.service.storage;

import com.ariche.boatapi.service.storage.dto.EStorageError;
import lombok.Getter;

@Getter
public class StorageException extends RuntimeException {

    private final EStorageError error;

    public StorageException(String message) {
        this(EStorageError.TECHNICAL_ERROR, message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.error = EStorageError.TECHNICAL_ERROR;
    }

    public StorageException(EStorageError error, String message) {
        super(message);
        this.error = error;
    }

    public StorageException(EStorageError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }
}
