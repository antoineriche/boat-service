package com.ariche.boatapi.service.storage.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileResourceDTOTest {

    private static final FileResourceDTO FILE_RESOURCE =  new FileResourceDTO("test", new byte[]{1,2,3});

    @Test
    void test_TestEquals() {
        assertEquals(FILE_RESOURCE, FILE_RESOURCE);
        assertNotEquals(FILE_RESOURCE, null);
        assertNotEquals(FILE_RESOURCE, "toto");
        assertNotEquals(FILE_RESOURCE, new FileResourceDTO("toto", null));
        assertNotEquals(FILE_RESOURCE, new FileResourceDTO("test", new byte[]{1,2,3,4}));
        assertEquals(FILE_RESOURCE, new FileResourceDTO("test", new byte[]{1,2,3}));

    }

    @Test
    void test_TestHashCode() {
        assertEquals(110283216, FILE_RESOURCE.hashCode());
    }

    @Test
    void test_TestToString() {
        assertEquals("FileResourceDTO{mimeType='test', bytes=[1, 2, 3]}", FILE_RESOURCE.toString());
    }
}
