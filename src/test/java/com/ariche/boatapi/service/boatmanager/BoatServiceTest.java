package com.ariche.boatapi.service.boatmanager;

import com.ariche.boatapi.entity.BoatEntity;
import com.ariche.boatapi.errors.BoatAPIException;
import com.ariche.boatapi.repository.BoatRepository;
import com.ariche.boatapi.repository.customset.IBoatImgSet;
import com.ariche.boatapi.service.boatmanager.dto.BoatDTO;
import com.ariche.boatapi.service.storage.StorageException;
import com.ariche.boatapi.service.storage.StorageService;
import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class BoatServiceTest {

    @Mock
    private BoatRepository boatRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    @Resource
    private BoatService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_FindBoatById() {
        when(boatRepository.findById(anyLong()))
            .thenReturn(Optional.of(new BoatEntity()));

        assertTrue(service.findBoatById(12L).isPresent());

        verify(boatRepository).findById(12L);
    }

    @Test
    void test_FindALlBoats() {
        final Pageable pageable = Pageable.ofSize(19);
        when(boatRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(new BoatEntity())));

        assertEquals(1, service.findALlBoats(pageable).getTotalElements());

        verify(boatRepository).findAll(pageable);
    }

    @Test
    void test_DeleteBoatById() {
        final IBoatImgSet imgSet = mock(IBoatImgSet.class);
        when(imgSet.getImgName()).thenReturn("test");
        when(imgSet.getId()).thenReturn(12L);
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.of(imgSet));

        service.deleteBoatById(12L);
        verify(boatRepository).deleteById(12L);
        verify(storageService).deleteFileQuietly("test", BoatService.buildBoatStorageFolder(12L));
    }

    @Test
    void test_DeleteBoatById_NoImg() {
        final IBoatImgSet imgSet = mock(IBoatImgSet.class);
        when(imgSet.getImgName()).thenReturn(null);
        when(imgSet.getId()).thenReturn(12L);
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.of(imgSet));

        service.deleteBoatById(12L);
        verify(boatRepository).deleteById(12L);
        verify(storageService, never()).deleteFileQuietly(anyString(), anyString());
    }

    @Test
    void test_DeleteBoatById_NoBoat() {
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.empty());

        service.deleteBoatById(12L);
        verify(boatRepository, never()).deleteById(anyLong());
        verify(storageService, never()).deleteFileQuietly(anyString(), anyString());
    }

    @Test
    void test_CreateBoat() {
        final BoatEntity entity = new BoatEntity();
        entity.setId(19L);

        when(boatRepository.save(any(BoatEntity.class)))
            .thenReturn(entity);

        assertEquals(19L, service.createBoat(new BoatDTO(null, "name", "desc.")).id());

        final ArgumentCaptor<BoatEntity> captor = ArgumentCaptor.forClass(BoatEntity.class);

        verify(boatRepository).save(captor.capture());

        assertNull(captor.getValue().getId());
        assertEquals("name", captor.getValue().getName());
        assertEquals("desc.", captor.getValue().getDescription());

    }

    @Test
    void test_UpdateBoat() {
        final BoatEntity entity = new BoatEntity();
        entity.setId(19L);

        when(boatRepository.findById(anyLong()))
            .thenReturn(Optional.of(entity));
        when(boatRepository.save(any(BoatEntity.class)))
            .thenReturn(entity);

        final BoatDTO dto = service.updateBoat(19L, new BoatDTO(19L, "name", "desc"));

        assertEquals(19L, dto.id());
        assertEquals("name", dto.name());
        assertEquals("desc", dto.description());

        verify(boatRepository).findById(19L);
        verify(boatRepository).save(entity);
    }

    @Test
    void test_UpdateBoat_NotFound() {
        final BoatEntity entity = new BoatEntity();
        entity.setId(19L);

        when(boatRepository.findById(anyLong()))
            .thenReturn(Optional.empty());

        final BoatDTO dto = new BoatDTO(19L, "name", "desc");
        assertThrows(EntityNotFoundException.class,
            () -> service.updateBoat(19L, dto));

        verify(boatRepository).findById(19L);
        verify(boatRepository, never()).updateImgNameByBoatId(anyLong(), anyString());
    }

    @Test
    void test_UpdateBoat_BoatAPIException() {
        final BoatEntity entity = new BoatEntity();
        entity.setId(19L);

        final BoatDTO dto = new BoatDTO(12L, "name", "desc");
        assertThrows(BoatAPIException.class,
            () -> service.updateBoat(19L, dto));

        verify(boatRepository, never()).findById(19L);
        verify(boatRepository, never()).updateImgNameByBoatId(anyLong(), anyString());
    }

    @Test
    void test_UploadImageForBoat_NotFound() {
        final MockMultipartFile file = spy(new MockMultipartFile("test", new byte[0]));
        when(file.getContentType()).thenReturn(MediaType.IMAGE_JPEG_VALUE);
        when(file.isEmpty()).thenReturn(false);
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> service.uploadImageForBoat(12L, file));

        verify(boatRepository).findBoatImgById(12L);
        verify(storageService, never()).storeFile(any(MultipartFile.class), anyString(), anyString());
        verify(boatRepository, never()).updateImgNameByBoatId(anyLong(), anyString());
    }

    @Test
    void test_UploadImageForBoat_StorageException() {
        final MockMultipartFile file = spy(new MockMultipartFile("test", new byte[0]));
        when(file.getOriginalFilename()).thenReturn("original.png");
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(MediaType.IMAGE_PNG_VALUE);

        final IBoatImgSet imgSet = mock(IBoatImgSet.class);
        when(imgSet.getImgName()).thenReturn("an-image.png");
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.of(imgSet));
        doThrow(StorageException.class)
            .when(storageService)
            .storeFile(any(MultipartFile.class), anyString(), anyString(), anyBoolean());
        doNothing()
            .when(storageService)
            .deleteFile(anyString(), anyString());

        assertThrows(StorageException.class,
            () -> service.uploadImageForBoat(12L, file));

        verify(boatRepository).findBoatImgById(12L);
        verify(storageService, never()).deleteFile(anyString(), anyString());
        verify(storageService).storeFile(file, "original.png", "boat-000000012", true);
        verify(boatRepository, never()).updateImgNameByBoatId(anyLong(), anyString());
    }

    @Test
    void test_UploadImageForBoat() {
        final MockMultipartFile file = spy(new MockMultipartFile("test", new byte[]{1,2,3}));
        when(file.getOriginalFilename()).thenReturn("original.png");
        when(file.getContentType()).thenReturn(MediaType.IMAGE_PNG_VALUE);
        when(file.isEmpty()).thenReturn(false);

        final IBoatImgSet imgSet = mock(IBoatImgSet.class);
        when(imgSet.getImgName()).thenReturn("an-image.png");
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.of(imgSet));
        doNothing()
            .when(storageService)
            .storeFile(any(MultipartFile.class), anyString(), anyString());
        doNothing()
            .when(storageService)
            .deleteFileQuietly(anyString(), anyString());

        assertDoesNotThrow(() -> service.uploadImageForBoat(12L, file));

        verify(boatRepository).findBoatImgById(12L);
        verify(storageService).storeFile(file, "original.png", "boat-000000012", true);
        verify(storageService).deleteFileQuietly("an-image.png", "boat-000000012");
        verify(boatRepository).updateImgNameByBoatId(12L, "original.png");
    }

    @Test
    void test_UploadImageForBoat_NoPreviousImg() {
        final MockMultipartFile file = spy(new MockMultipartFile("test", new byte[]{1,2,3}));
        when(file.getOriginalFilename()).thenReturn("original.png");
        when(file.getContentType()).thenReturn(MediaType.IMAGE_PNG_VALUE);
        when(file.isEmpty()).thenReturn(false);

        final IBoatImgSet imgSet = mock(IBoatImgSet.class);
        when(imgSet.getImgName()).thenReturn(null);
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.of(imgSet));
        doNothing()
            .when(storageService)
            .storeFile(any(MultipartFile.class), anyString(), anyString());

        assertDoesNotThrow(() -> service.uploadImageForBoat(12L, file));

        verify(boatRepository).findBoatImgById(12L);
        verify(storageService).storeFile(file, "original.png", "boat-000000012", true);
        verify(storageService, never()).deleteFile(anyString(), anyString());
        verify(boatRepository).updateImgNameByBoatId(12L, "original.png");
    }

    @Test
    void test_UploadImageForBoat_PreviousImgSameName() {
        final MockMultipartFile file = spy(new MockMultipartFile("test", new byte[]{1,2,3}));
        when(file.getOriginalFilename()).thenReturn("original.png");
        when(file.getContentType()).thenReturn(MediaType.IMAGE_PNG_VALUE);
        when(file.isEmpty()).thenReturn(false);

        final IBoatImgSet imgSet = mock(IBoatImgSet.class);
        when(imgSet.getImgName()).thenReturn("original.png");
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.of(imgSet));
        doNothing()
            .when(storageService)
            .storeFile(any(MultipartFile.class), anyString(), anyString());

        assertDoesNotThrow(() -> service.uploadImageForBoat(12L, file));

        verify(boatRepository).findBoatImgById(12L);
        verify(storageService).storeFile(file, "original.png", "boat-000000012", true);
        verify(storageService, never()).deleteFile(anyString(), anyString());
        verify(boatRepository).updateImgNameByBoatId(12L, "original.png");
    }

    @Test
    void test_UploadImageForBoat_DeleteException() {
        final MockMultipartFile file = spy(new MockMultipartFile("test", new byte[]{1,2,3}));
        when(file.getOriginalFilename()).thenReturn("original.png");
        when(file.getContentType()).thenReturn(MediaType.IMAGE_PNG_VALUE);
        when(file.isEmpty()).thenReturn(false);

        final IBoatImgSet imgSet = mock(IBoatImgSet.class);
        when(imgSet.getImgName()).thenReturn("an-image.png");
        when(boatRepository.findBoatImgById(anyLong())).thenReturn(Optional.of(imgSet));
        doNothing()
            .when(storageService)
            .storeFile(any(MultipartFile.class), anyString(), anyString());

        assertDoesNotThrow(() -> service.uploadImageForBoat(12L, file));

        verify(boatRepository).findBoatImgById(12L);
        verify(storageService).storeFile(file, "original.png", "boat-000000012", true);
        verify(storageService).deleteFileQuietly("an-image.png", "boat-000000012");
        verify(boatRepository).updateImgNameByBoatId(12L, "original.png");
    }

    @Test
    void test_DownloadImageForBoat() {
        when(boatRepository.fetchImgNameById(anyLong())).thenReturn(Optional.of("image-name.jpeg"));
        when(storageService.downloadFile(anyString(), anyString()))
            .thenReturn(new FileResourceDTO(MediaType.IMAGE_JPEG_VALUE, new byte[0]));

        final Optional<FileResourceDTO> optRes = service.downloadImageForBoat(12L);
        assertTrue(optRes.isPresent());
        assertEquals(MediaType.IMAGE_JPEG_VALUE, optRes.get().mimeType());

        verify(boatRepository).fetchImgNameById(12L);
        verify(storageService).downloadFile("image-name.jpeg", "boat-000000012");
    }

    @Test
    void test_DownloadImageForBoat_Exception() {
        when(boatRepository.fetchImgNameById(anyLong())).thenReturn(Optional.of("image-name.jpeg"));
        doThrow(StorageException.class)
            .when(storageService)
            .downloadFile(anyString(), anyString());

        final Optional<FileResourceDTO> optRes = service.downloadImageForBoat(13L);
        assertTrue(optRes.isEmpty());

        verify(boatRepository).fetchImgNameById(13L);
        verify(storageService).downloadFile("image-name.jpeg", "boat-000000013");
    }

    @Test
    void test_DownloadImageForBoat_Empty() {
        when(boatRepository.fetchImgNameById(anyLong())).thenReturn(Optional.empty());
        assertTrue(service.downloadImageForBoat(12L).isEmpty());
        verify(storageService, never()).downloadFile(anyString(), anyString());
    }

    @Test
    void test_BuildBoatStorageFolder() {
        assertEquals("boat-000000118", BoatService.buildBoatStorageFolder(118L));
    }

    @Test
    void test_TestCreateBoat_BoatAPIException() {
        final BoatDTO boatDTO = new BoatDTO(23L, "test", null);
        assertThrows(BoatAPIException.class, () -> service.createBoat(boatDTO));
    }

    @Test
    void test_EnsureIncomingFile_Empty() {
        final MockMultipartFile multipartFile = mock(MockMultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(true);
        final List<String> mediaTypes = Collections.emptyList();
        assertThrows(StorageException.class,
            () -> BoatService.ensureIncomingFile(multipartFile, mediaTypes));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        MediaType.APPLICATION_JSON_VALUE
    })
    void test_EnsureIncomingFile_Unsupported(final String contentType) {
        final MockMultipartFile multipartFile = mock(MockMultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn(contentType);
        final List<String> mediaTypes = Collections.singletonList(MediaType.IMAGE_JPEG_VALUE);
        assertThrows(StorageException.class,
            () -> BoatService.ensureIncomingFile(multipartFile, mediaTypes));
    }
}
