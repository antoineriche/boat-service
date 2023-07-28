package com.ariche.boatapi.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EBoatAPIError {
    INVALID_REQUEST("Invalid data", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("Internal error", HttpStatus.INTERNAL_SERVER_ERROR),
    TECHNICAL_ERROR("Technical error", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND_ERROR("Resource not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ERROR("Unauthorized", HttpStatus.UNAUTHORIZED);

    private final String category;
    private final HttpStatus httpStatus;
}
