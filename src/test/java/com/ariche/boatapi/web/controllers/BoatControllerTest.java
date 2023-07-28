package com.ariche.boatapi.web.controllers;

import com.ariche.boatapi.service.boatmanager.BoatService;
import com.ariche.boatapi.service.boatmanager.dto.BoatDTO;
import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BoatControllerTest extends AbstractRestControllerTest {

    @Mock
    private BoatService boatService;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_FindAllBoats() throws Exception {
        final BoatDTO boat1  = new BoatDTO(24L, "name", "desc");
        final BoatDTO boat2  = new BoatDTO(17L, "name-2", null);

        final Page<BoatDTO> page = new PageImpl<>(
            List.of(boat1, boat2));

        when(boatService.findALlBoats(any(Pageable.class))).thenReturn(page);

        final MvcResult result = super.restMock.perform(get(getEndpoint() +
                "?size=2" +
                "&sort=id,ASC"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

        final Page<BoatDTO> res = readMvcResultAsPage(result, BoatDTO.class);
        assertEquals(2, res.getTotalElements());

        assertEquals("name", page.getContent().get(0).name());
        assertEquals("name-2", page.getContent().get(1).name());

        final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(boatService).findALlBoats(captor.capture());

        assertEquals(2, captor.getValue().getPageSize());
        assertTrue(captor.getValue().getSort().isSorted());
        assertTrue(captor.getValue().getSort().stream().anyMatch(order ->
            order.getDirection().isAscending()
                && order.getProperty().equals("id")));
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_CreateBoat() throws Exception {
        final BoatDTO boat = new BoatDTO(24L, "name", "desc");
        when(boatService.createBoat(any(BoatDTO.class)))
            .thenReturn(boat);

        final MvcResult result = super.restMock.perform(post(getEndpoint())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(boat)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

        final String location = result.getResponse().getHeader("Location");
        assertTrue(StringUtils.isNotBlank(location));
        assertTrue(location.endsWith("api/v1/boats/24"));

        final BoatDTO res = readMvcResultAs(result, BoatDTO.class);
        assertEquals(24L, res.id());
        assertEquals("name", res.name());
        assertEquals("desc", res.description());

        final ArgumentCaptor<BoatDTO> captor = ArgumentCaptor.forClass(BoatDTO.class);
        verify(boatService).createBoat(captor.capture());
        assertEquals(24L, captor.getValue().id());
        assertEquals("name", captor.getValue().name());
        assertEquals("desc", captor.getValue().description());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_FindBoatById() throws Exception {
        final BoatDTO boat = new BoatDTO(25L, "name", "desc");
        when(boatService.findBoatById(anyLong())).thenReturn(Optional.of(boat));

        final MvcResult result = super.restMock.perform(get(getEndpoint() + "/25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

        final BoatDTO res = readMvcResultAs(result, BoatDTO.class);
        assertEquals(25L, res.id());
        assertEquals("name", res.name());
        assertEquals("desc", res.description());

        verify(boatService).findBoatById(25L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_FindBoatById_NotFound() throws Exception {
        when(boatService.findBoatById(anyLong())).thenReturn(Optional.empty());

        super.restMock.perform(get(getEndpoint() + "/25"))
            .andExpect(status().isNotFound());

        verify(boatService).findBoatById(25L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_UploadBoatImage() throws Exception {
        final InputStream stream = new ClassPathResource("/samples/boat.jpeg").getInputStream();

        doNothing()
            .when(boatService)
            .uploadImageForBoat(anyLong(), any(MultipartFile.class));

        final MvcResult result = super.restMock.perform(multipart(getEndpoint() + "/25/image")
                .file(new MockMultipartFile("file", stream))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated())
            .andReturn();

        final String location = result.getResponse().getHeader("Location");
        assertTrue(StringUtils.isNotBlank(location));
        assertTrue(location.endsWith("api/v1/boats/25/image"));

        verify(boatService).uploadImageForBoat(eq(25L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_DownloadBoatImage_NotFound() throws Exception {
        when(boatService.downloadImageForBoat(anyLong()))
            .thenReturn(Optional.empty());

        super.restMock.perform(get(getEndpoint() + "/25/image"))
            .andExpect(status().isNotFound());

        verify(boatService).downloadImageForBoat(25L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_DownloadBoatImage() throws Exception {
        final InputStream stream = new ClassPathResource("/samples/boat.jpeg").getInputStream();

        when(boatService.downloadImageForBoat(anyLong()))
            .thenReturn(Optional.of(new FileResourceDTO(MediaType.IMAGE_JPEG_VALUE, stream.readAllBytes())));

        final MvcResult result = super.restMock.perform(get(getEndpoint() + "/25/image"))
            .andExpect(status().isOk())
            .andReturn();

        final String contentType = result.getResponse().getHeader(HttpHeaders.CONTENT_TYPE);
        assertTrue(StringUtils.isNotBlank(contentType));
        assertEquals(MediaType.IMAGE_JPEG_VALUE, contentType);

        verify(boatService).downloadImageForBoat(25L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_DownloadBoatImage_UnknownMimeType() throws Exception {
        final InputStream stream = new ClassPathResource("/samples/boat.jpeg").getInputStream();

        when(boatService.downloadImageForBoat(anyLong()))
            .thenReturn(Optional.of(new FileResourceDTO(null, stream.readAllBytes())));

        final MvcResult result = super.restMock.perform(get(getEndpoint() + "/25/image"))
            .andExpect(status().isOk())
            .andReturn();

        final String contentType = result.getResponse().getHeader(HttpHeaders.CONTENT_TYPE);
        assertTrue(StringUtils.isNotBlank(contentType));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, contentType);

        verify(boatService).downloadImageForBoat(25L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_UpdateBoatById() throws Exception {
        final BoatDTO boat = new BoatDTO(25L, "name", "desc");
        when(boatService.updateBoat(anyLong(), any(BoatDTO.class)))
            .thenReturn(boat);

        final MvcResult result = super.restMock.perform(put(getEndpoint() + "/25")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(boat)))
            .andExpect(status().isOk())
            .andReturn();

        final ArgumentCaptor<BoatDTO> captor = ArgumentCaptor.forClass(BoatDTO.class);
        verify(boatService).updateBoat(eq(25L), captor.capture());

        assertEquals(25L, captor.getValue().id());
        assertEquals("name", captor.getValue().name());
        assertEquals("desc", captor.getValue().description());

        final BoatDTO res = readMvcResultAs(result, BoatDTO.class);
        assertEquals(25L, res.id());
        assertEquals("name", res.name());
        assertEquals("desc", res.description());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void test_DeleteBoatById() throws Exception {
        doNothing()
            .when(boatService)
            .deleteBoatById(anyLong());

        super.restMock.perform(delete(getEndpoint() + "/12"))
            .andExpect(status().isOk());

        verify(boatService).deleteBoatById(12L);
    }

    @Override
    public Object buildResource() {
        return new BoatController(boatService);
    }

    @Override
    public String getEndpoint() {
        return "/api/v1/boats";
    }

    @Override
    protected Map<String, RequestBuilder> getDynamicTests() {
        final BoatDTO boat = new BoatDTO(null, "name", "desc");

        final Map<String, RequestBuilder> map = new HashMap<>();
        map.put("findAllBoats", get(getEndpoint()));
        map.put("createBoat", post(getEndpoint())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(asJsonString(boat)));
        map.put("findBoatById", get(getEndpoint() + "/25"));

        try (final InputStream stream = new ClassPathResource("/samples/boat.jpeg").getInputStream()) {
            map.put("uploadBoatImage", multipart(getEndpoint() + "/25/image")
                .file(new MockMultipartFile("file", stream))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE));
        } catch (IOException ignored) {
            // Ignore the test it if the file is not found or can not be red as stream
        }

        map.put("downloadImageBoat", get(getEndpoint() + "/25/image"));
        map.put("updateBoatById", put(getEndpoint() + "/25")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(asJsonString(boat)));
        map.put("deleteBoatById", delete(getEndpoint() + "/25"));
        return map;
    }
}
