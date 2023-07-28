package com.ariche.boatapi.web.errors;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeGeneratorTest {

    private final ErrorCodeGenerator codeGenerator = new ErrorCodeGenerator();

    @Test
    void test_GenerateErrorCode() {
        final String code1 = codeGenerator.generateErrorCode();
        final String code2 = codeGenerator.generateErrorCode();
        assertTrue(code1.startsWith(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))));
        assertTrue(code2.startsWith(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))));
        assertNotEquals(code1, code2);
    }

}
