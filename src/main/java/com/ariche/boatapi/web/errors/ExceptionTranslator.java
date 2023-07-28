package com.ariche.boatapi.web.errors;

import com.ariche.boatapi.errors.BoatAPIException;
import com.ariche.boatapi.errors.EBoatAPIError;
import com.ariche.boatapi.service.storage.StorageException;
import com.ariche.boatapi.web.errors.mapper.ErrorMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public final class ExceptionTranslator {

    private final ErrorCodeGenerator errorCodeGenerator;

    ResponseEntity<BoatAPIError> buildResponse(final String errorCode,
                                               final EBoatAPIError error,
                                               final List<ProblemDTO> problems) {
        return ResponseEntity
            .status(error.getHttpStatus())
            .body(new BoatAPIError(errorCode, error.getCategory(), problems));
    }

    ResponseEntity<BoatAPIError> buildResponse(final String errorCode,
                                               final EBoatAPIError error,
                                               final ProblemDTO problem) {
        return buildResponse(errorCode, error, Collections.singletonList(problem));
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<BoatAPIError> handleEntityNotFoundException(final EntityNotFoundException ex) {
        final String errorCode = errorCodeGenerator.generateErrorCode();
        log.error("[EntityNotFoundException] {}", errorCode, ex);
        return buildResponse(errorCode, EBoatAPIError.NOT_FOUND_ERROR, new ProblemDTO(ex.getMessage()));
    }

    @ExceptionHandler(value = StorageException.class)
    public ResponseEntity<BoatAPIError> handleStorageException(final StorageException ex) {
        final String errorCode = errorCodeGenerator.generateErrorCode();
        log.error("[StorageException] {}", errorCode, ex);
        final EBoatAPIError error = switch (ex.getError()) {
            case FILE_NOT_FOUND -> EBoatAPIError.NOT_FOUND_ERROR;
            case WRONG_FILE_FORMAT -> EBoatAPIError.INVALID_REQUEST;
            default -> EBoatAPIError.TECHNICAL_ERROR;
        };

        return buildResponse(errorCode, error, new ProblemDTO(ex.getMessage()));
    }

    @ExceptionHandler(value = BoatAPIException.class)
    public ResponseEntity<BoatAPIError> handleBoatAPIException(final BoatAPIException ex) {
        final String errorCode = errorCodeGenerator.generateErrorCode();
        log.error("[BoatAPIException] {}", errorCode, ex);
        return buildResponse(errorCode, ex.getError(), new ProblemDTO(ex.getMessage()));
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<BoatAPIError> handleAuthenticationException(final AuthenticationException ex) {
        final String errorCode = errorCodeGenerator.generateErrorCode();
        log.error("[AuthenticationException: {}] {}", ex.getClass().getSimpleName(), errorCode);
        return buildResponse(errorCode, EBoatAPIError.INVALID_REQUEST, new ProblemDTO(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BoatAPIError> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        final String errorCode = errorCodeGenerator.generateErrorCode();
        final List<ProblemDTO> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(ErrorMapper::fromFieldError)
            .toList();
        return buildResponse(errorCode, EBoatAPIError.INVALID_REQUEST, errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BoatAPIError> handleDataIntegrityViolationException(final DataIntegrityViolationException ex) {
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cex) {
            final String errorCode = errorCodeGenerator.generateErrorCode();
            log.error("[DataIntegrityViolationException: ConstraintViolationException] {}",  errorCode, ex);
            final String msg = switch (cex.getConstraintName()) {
                case "ux_user_email" -> "Email already used";
                case "ux_user_login" -> "Login already used";
                default -> EBoatAPIError.TECHNICAL_ERROR.getCategory();
            };

            return buildResponse(errorCode,
                EBoatAPIError.INVALID_REQUEST,
                new ProblemDTO(msg));
        }

        return handleGenericException(ex);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<BoatAPIError> handleTransactionSystemException(final TransactionSystemException ex) {
        if (ex.getCause() instanceof RollbackException rollbackException
            && rollbackException.getCause() instanceof ConstraintViolationException violationException) {
            final String errorCode = errorCodeGenerator.generateErrorCode();
            log.error("[ConstraintViolationException] {}",  errorCode, ex);

            return buildResponse(errorCode,
                EBoatAPIError.INVALID_REQUEST,
                ErrorMapper.fromConstraintViolationException(violationException));
        }

        return handleGenericException(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BoatAPIError> handleGenericException(final Exception ex) {
        final String errorCode = errorCodeGenerator.generateErrorCode();
        log.error("[ExceptionTranslator: {}] {}", ex.getClass().getSimpleName(), errorCode, ex);
        return buildResponse(errorCode, EBoatAPIError.INTERNAL_ERROR, new ProblemDTO(ex.getMessage()));
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BoatAPIError> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        if (ex.getCause() instanceof IllegalStateException stateException
            && stateException.getCause() instanceof SizeLimitExceededException limitExceededException) {
            final String errorCode = errorCodeGenerator.generateErrorCode();
            log.error("[SizeLimitExceededException] {}",  errorCode, ex);
            final String currentSize = FileUtils.byteCountToDisplaySize(limitExceededException.getActualSize());
            final String maxSize = FileUtils.byteCountToDisplaySize(limitExceededException.getPermittedSize());
            return buildResponse(errorCode,
                EBoatAPIError.INVALID_REQUEST,
                new ProblemDTO("File too big: %s — max size: %s".formatted(currentSize, maxSize)));
        }

        return handleGenericException(ex);
    }

}
