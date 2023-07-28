package com.ariche.boatapi.web.utils;

import com.ariche.boatapi.service.boatmanager.dto.BoatDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ResponseUtilsTest {

    @Test
    void test_WrapOrNotFound_NotFound() {
        final ResponseEntity<BoatDTO> response = ResponseUtils.wrapOrNotFound(Optional.empty());
        assertFalse(response.hasBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void test_WrapOrNotFound() {
        final ResponseEntity<BoatDTO> response = ResponseUtils
            .wrapOrNotFound(Optional.of(new BoatDTO(23L, "name", "toto")));
        assertTrue(response.hasBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
