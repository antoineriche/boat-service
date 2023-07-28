package com.ariche.boatapi.service.storage;

import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import com.ibm.cloud.objectstorage.SdkClientException;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudStorageServiceTest {

    @Mock
    private AmazonS3 cloudCOSClient;

    private final String bucketName = "bucket";

    private CloudStorageService storageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storageService = spy(new CloudStorageService(cloudCOSClient, bucketName));
    }

    @Test
    void test_StoreFile() throws IOException {
        final InputStream stream = mock(InputStream.class);
        final MockMultipartFile multipartFile = spy(new MockMultipartFile("test", stream));
        when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        doNothing()
            .when(storageService)
            .storeFile(anyString(), anyString(), any(InputStream.class), anyString(), anyBoolean());

        assertDoesNotThrow(() -> storageService.storeFile(multipartFile, "test", "folder"));
        verify(storageService)
            .storeFile(eq("test"), eq("folder"), any(InputStream.class), eq(MediaType.APPLICATION_JSON_VALUE), anyBoolean());
    }

    @Test
    void test_StoreFile_IOException() throws IOException {
        final InputStream stream = mock(InputStream.class);
        final MockMultipartFile multipartFile = spy(new MockMultipartFile("test", stream));
        when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        doThrow(IOException.class)
            .when(storageService)
            .storeFile(anyString(), anyString(), any(InputStream.class), anyString(), anyBoolean());

        assertThrows(StorageException.class,
            () -> storageService.storeFile(multipartFile, "test", "folder", true));
    }

    @Test
    void test_StoreFile_SdkClientException() throws IOException {
        final InputStream stream = mock(InputStream.class);
        final MockMultipartFile multipartFile = spy(new MockMultipartFile("test", stream));
        when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        doThrow(SdkClientException.class)
            .when(storageService)
            .storeFile(anyString(), anyString(), any(InputStream.class), anyString(), anyBoolean());

        assertThrows(StorageException.class,
            () -> storageService.storeFile(multipartFile, "test", "folder", true));
    }

    @Test
    void test_downloadFile() throws IOException {
        when(cloudCOSClient.doesObjectExist(anyString(), anyString())).thenReturn(true);

        final S3Object s3Object = mock(S3Object.class);
        final ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getContentType()).thenReturn(MediaType.APPLICATION_PDF_VALUE);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);
        final S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        when(stream.readAllBytes()).thenReturn(new byte[]{0,1,2,3});
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(cloudCOSClient.getObject(anyString(), anyString()))
            .thenReturn(s3Object);

        final FileResourceDTO res = storageService.downloadFile("file.pdf", "folder");
        assertNotNull(res.bytes());
        assertEquals(MediaType.APPLICATION_PDF_VALUE, res.mimeType());
        verify(cloudCOSClient).getObject("bucket/folder", "file.pdf");
    }

    @Test
    void test_downloadFile_NotExistException() {
        when(cloudCOSClient.doesObjectExist(anyString(), anyString())).thenReturn(false);
        assertThrows(StorageException.class, () -> storageService.downloadFile("file", "folder"));
        verify(cloudCOSClient, never()).getObject(anyString(), anyString());
    }

    @Test
    void test_downloadFile_IOException() throws IOException {
        when(cloudCOSClient.doesObjectExist(anyString(), anyString())).thenReturn(true);

        final S3Object s3Object = mock(S3Object.class);
        final ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getContentType()).thenReturn(MediaType.APPLICATION_PDF_VALUE);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);
        final S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        doThrow(IOException.class)
            .when(stream)
            .readAllBytes();
        //when(stream.readAllBytes()).thenReturn(new byte[]{0,1,2,3});
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(cloudCOSClient.getObject(anyString(), anyString()))
            .thenReturn(s3Object);

        assertThrows(StorageException.class,
            () -> storageService.downloadFile("file.pdf", "folder"));

        verify(cloudCOSClient).getObject("bucket/folder", "file.pdf");
    }

    @Test
    void test_downloadFile_SdkClientException() {
        when(cloudCOSClient.doesObjectExist(anyString(), anyString()))
            .thenThrow(mock(SdkClientException.class));
        assertThrows(StorageException.class, () -> storageService.downloadFile("file", "folder"));
        verify(cloudCOSClient, never()).getObject(anyString(), anyString());
    }

    @Test
    void test_deleteFile_NotExist() throws StorageException {
        when(cloudCOSClient.doesObjectExist(anyString(), anyString())).thenReturn(false);
        storageService.deleteFile("file", "folder");

        verify(cloudCOSClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void test_deleteFile_Exists() {
        when(cloudCOSClient.doesObjectExist(anyString(), anyString())).thenReturn(true);
        assertThrows(StorageException.class, () -> storageService.deleteFile("file", "folder"));

        verify(cloudCOSClient).deleteObject("bucket/folder", "file");
    }

    @Test
    void test_TestStoreFile() throws IOException {
        final InputStream stream = mock(InputStream.class);
        when(stream.available()).thenReturn(250);
        storageService.storeFile("name", "folder", stream, MediaType.APPLICATION_JSON_VALUE, true);

        final ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);

        verify(cloudCOSClient)
            .putObject(eq(bucketName.concat("/folder")), eq("name"), eq(stream), captor.capture());

        assertEquals(MediaType.APPLICATION_JSON_VALUE, captor.getValue().getContentType());
        assertEquals(250, captor.getValue().getContentLength());
    }

    @Test
    void test_BuildFolderPath() {
        assertEquals("%s/test".formatted(bucketName), storageService.buildFolderPath("test"));
    }

    @Test
    void test_TestStoreFile_Replace_False_NoExist() throws IOException {
        final InputStream stream = mock(InputStream.class);
        when(stream.available()).thenReturn(250);
        when(cloudCOSClient.doesObjectExist(anyString(), anyString()))
            .thenReturn(false);

        storageService.storeFile("name", "folder", stream, MediaType.APPLICATION_JSON_VALUE, false);

        final ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);

        verify(cloudCOSClient)
            .putObject(eq(bucketName.concat("/folder")), eq("name"), eq(stream), captor.capture());
        verify(cloudCOSClient)
            .doesObjectExist(bucketName.concat("/folder"), "name");
        assertEquals(MediaType.APPLICATION_JSON_VALUE, captor.getValue().getContentType());
        assertEquals(250, captor.getValue().getContentLength());
    }

    @Test
    void test_TestStoreFile_Replace_False_AlreadyExist() throws IOException {
        final InputStream stream = mock(InputStream.class);
        when(stream.available()).thenReturn(250);
        when(cloudCOSClient.doesObjectExist(anyString(), anyString()))
            .thenReturn(true);


        assertThrows(StorageException.class,
            () -> storageService.storeFile("name", "folder", stream, MediaType.APPLICATION_JSON_VALUE, false));

        verify(cloudCOSClient, never())
            .putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(cloudCOSClient)
            .doesObjectExist(bucketName.concat("/folder"), "name");
    }

    @Test
    void test_deleteFileQuietly_Exception() {
        doThrow(StorageException.class)
            .when(storageService)
            .deleteFile(anyString(), anyString());

        assertDoesNotThrow(() -> storageService.deleteFileQuietly("test", "folder"));

        verify(storageService).deleteFile("test", "folder");
    }

    @Test
    void test_deleteFileQuietly() {
        doNothing()
            .when(storageService)
            .deleteFile(anyString(), anyString());

        assertDoesNotThrow(() -> storageService.deleteFileQuietly("test", "folder"));

        verify(storageService).deleteFile("test", "folder");
    }
}
