package com.ariche.boatapi.web.errors.mapper;

import com.ariche.boatapi.web.errors.ProblemDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;

import java.util.List;

public final class ErrorMapper {

    public static ProblemDTO fromFieldError(final FieldError fieldError) {
        // TODO: Should be improved with depth
        return new ProblemDTO("Invalid '%s'".formatted(fieldError.getField()),
            fieldError.getDefaultMessage());
    }

    public static List<ProblemDTO> fromConstraintViolationException(final ConstraintViolationException violationException) {
        return violationException.getConstraintViolations().stream()
            .map(constraintViolation -> new ProblemDTO(constraintViolation.getMessage()))
            .toList();
    }

    private ErrorMapper() {
    }
}
