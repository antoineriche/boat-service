package com.ariche.boatapi.web.errors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProblemDTOTest {

    @Test
    void test_basics() {
        final ProblemDTO problemDTO = new ProblemDTO("error", "detail");
        assertEquals("error", problemDTO.error());
        assertEquals("detail", problemDTO.detail());
    }
}
