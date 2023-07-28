package com.ariche.boatapi.service.storage;

import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

class LocalStorageServiceTest {

    private static LocalStorageService storageService;

    @BeforeAll
    static void setUpAll() {
        storageService = spy(new LocalStorageService("samples"));
    }

    @AfterAll
    static void tearDownAll() {
        FileUtils.deleteQuietly(new File("samples"));
    }

    @Test
    void test_StoreFile_CreateParentFolder() {
        final MockMultipartFile multipartFile = new MockMultipartFile("test", new byte[]{0,1,2,3,4,5});
        assertDoesNotThrow(() -> storageService.storeFile(multipartFile, "name", "folder", true));
        FileUtils.deleteQuietly(new File(storageService.getStorageFolderPath(), "folder"));
    }

    @Test
    void test_StoreFile_DoNotCreateParentFolder() throws IOException {
        final MockMultipartFile multipartFile = new MockMultipartFile("test", new byte[]{0,1,2,3,4,5});
        Files.createDirectories(new File(storageService.getStorageFolderPath(), "folder").toPath());
        assertDoesNotThrow(() -> storageService.storeFile(multipartFile, "name", "folder", false));
        FileUtils.deleteQuietly(new File(storageService.getStorageFolderPath(), "folder"));
    }

    @Test
    void test_StoreFile_FileAlreadyExistsException() throws IOException {
        final MockMultipartFile multipartFile = new MockMultipartFile("test", new byte[]{0,1,2,3,4,5});
        final File directory = new File(storageService.getStorageFolderPath(), "folder");
        Files.createDirectories(directory.toPath());
        new File(directory, "toto").createNewFile();

        assertThrows(StorageException.class,
            () -> storageService.storeFile(multipartFile, "toto", "folder", false));

        FileUtils.deleteQuietly(directory);
    }

    @Test
    void test_StoreFile_IOException() throws IOException {
        final MockMultipartFile multipartFile = new MockMultipartFile("test", new byte[]{0,1,2,3,4,5});
        final File directory = new File(storageService.getStorageFolderPath(), "folder1");
        Files.createDirectories(directory.toPath());

        try (MockedStatic<Files> mockedStatic = mockStatic(Files.class)) {
            mockedStatic
                .when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                .thenThrow(IOException.class);
            assertThrows(StorageException.class,
                () -> storageService.storeFile(multipartFile, "static1", "folder1", true));
        }

        FileUtils.deleteQuietly(directory);
    }

    @Test
    void test_DownloadFile() throws IOException {
        final File temp = new File("samples", "test.jpeg");
        temp.createNewFile();

        final FileResourceDTO res = storageService.downloadFile("test.jpeg", "");
        assertEquals(MediaType.IMAGE_JPEG_VALUE, res.mimeType());

        FileUtils.deleteQuietly(temp);
    }

    @Test
    void test_DownloadFile_FileNotFoundException() {
        assertThrows(StorageException.class,
            () -> storageService.downloadFile("test.jpeg", ""));
    }

    @Test
    void test_DownloadFile_IOException() throws IOException {
        final File temp = new File("samples/folder4", "test4.jpeg");
        Files.createDirectories(temp.getParentFile().toPath());
        temp.createNewFile();

        try (MockedStatic<Files> mockedStatic = mockStatic(Files.class)) {
            mockedStatic
                .when(() -> Files.probeContentType(any(Path.class)))
                .thenThrow(IOException.class);

            assertThrows(StorageException.class,
                () -> storageService.downloadFile("test4.jpeg", "folder4"));

        }

        FileUtils.deleteQuietly(temp);
    }

    @Test
    void test_DeleteFile_IOException() {
        try (MockedStatic<Files> mockedStatic = mockStatic(Files.class)) {
            mockedStatic
                .when(() -> Files.deleteIfExists(any(Path.class)))
                .thenThrow(IOException.class);

            final LocalStorageService service = new LocalStorageService("samples/folder3");
            assertDoesNotThrow(() -> service.deleteFile("test", "folder3"));
        }
    }

    @Test
    void test_DeleteFile() {
        assertDoesNotThrow(() -> storageService.deleteFile("name", "folder"));
    }

    @Test
    void test_CreateStorageFolder() {
        final File test = new File("samples");

        final LocalStorageService service = new LocalStorageService("samples/test");
        service.createStorageFolder();

        assertTrue(test.exists());
        FileUtils.deleteQuietly(test);
    }

    @Test
    void test_CreateStorageFolder_AlreadyExists() throws IOException {
        final File test = new File("samples/test");
        Files.createDirectory(test.toPath());

        final LocalStorageService service = new LocalStorageService("samples/test");
        service.createStorageFolder();

        assertTrue(test.exists());
        FileUtils.deleteQuietly(test);
    }

    @Test
    void test_CreateStorageFolder_IOException() {
        try (MockedStatic<Files> mockedStatic = mockStatic(Files.class)) {
            mockedStatic
                .when(() -> Files.createDirectories(any(Path.class)))
                .thenThrow(IOException.class);

            final LocalStorageService service = new LocalStorageService("samples/toto");
            assertThrows(StorageException.class, service::createStorageFolder);
        }
    }

    @Test
    void test_GetStorageFolderPath() {
        assertEquals("samples", storageService.getStorageFolderPath());
    }

    @Test
    void test_GetStorageFolder() {
        assertTrue(storageService.getStorageFolder().exists());
    }
}
