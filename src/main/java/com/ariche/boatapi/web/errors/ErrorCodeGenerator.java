package com.ariche.boatapi.web.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ErrorCodeGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final AtomicInteger CPT = new AtomicInteger(0);


    /**
     * Generate a unique error code based on current date-time and an alpha-character
     * @return the error code to identify the error
     */
    public String generateErrorCode() {
        final StringBuilder sb = new StringBuilder(13);
        sb.append(FORMATTER.format(LocalDateTime.now()));
        final int value = CPT.getAndIncrement();
        sb.append((char)(value % 26 + 65)); // get an alpha-character
        return sb.toString();
    }
}
