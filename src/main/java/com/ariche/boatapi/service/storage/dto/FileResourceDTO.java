package com.ariche.boatapi.service.storage.dto;

import java.util.Arrays;
import java.util.Objects;

public record FileResourceDTO(String mimeType, byte[] bytes) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileResourceDTO that = (FileResourceDTO) o;
        return Objects.equals(mimeType, that.mimeType) && Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mimeType);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public String toString() {
        return "FileResourceDTO{" +
            "mimeType='" + mimeType + '\'' +
            ", bytes=" + Arrays.toString(bytes) +
            '}';
    }
}
