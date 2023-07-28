package com.ariche.boatapi.web.errors;

import com.ariche.boatapi.errors.BoatAPIException;
import com.ariche.boatapi.errors.EBoatAPIError;
import com.ariche.boatapi.service.storage.StorageException;
import com.ariche.boatapi.service.storage.dto.EStorageError;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ExceptionTranslatorTest {

    @Mock
    private ErrorCodeGenerator errorCodeGenerator;
    @InjectMocks
    @Resource
    private ExceptionTranslator translator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_HandleEntityNotFoundException() {
        final EntityNotFoundException exception = mock(EntityNotFoundException.class);
        when(exception.getMessage()).thenReturn("error");
        final ResponseEntity<BoatAPIError> res = translator.handleEntityNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.NOT_FOUND_ERROR.getCategory(), res.getBody().category());
        assertEquals("error", errors.get(0).error());
    }

    @Test
    void test_HandleMethodArgumentNotValidException() {
        final FieldError error1 = spy(new FieldError("test", "name", "should not be blank"));
        when(error1.getRejectedValue()).thenReturn("name");
        final FieldError error2 = spy(new FieldError("test", "description", "too long"));
        when(error2.getRejectedValue()).thenReturn("description");

        final BindingResult bindingResult = mock(BindingResult.class);
        final MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        final ResponseEntity<BoatAPIError> res = translator.handleMethodArgumentNotValidException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Invalid data", res.getBody().category());
        assertEquals("Invalid 'name'", errors.get(0).error());
        assertEquals("Invalid 'description'", errors.get(1).error());
    }

    @Test
    void test_HandleTransactionSystemException_ConstraintViolationException() {
        final ConstraintViolation<String> constraint = mock(ConstraintViolation.class);
        when(constraint.getMessage()).thenReturn("constraint");

        final TransactionSystemException transactionSystemException = mock(TransactionSystemException.class);
        final RollbackException rollbackException = spy(new RollbackException());
        when(transactionSystemException.getCause()).thenReturn(rollbackException);
        final ConstraintViolationException constraintException = mock(ConstraintViolationException.class);
        when(constraintException.getConstraintViolations())
            .thenReturn(new HashSet<>(Collections.singletonList(constraint)));
        when(rollbackException.getCause()).thenReturn(constraintException);

        final ResponseEntity<BoatAPIError> res = translator.handleTransactionSystemException(transactionSystemException);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);

        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INVALID_REQUEST.getCategory(), res.getBody().category());
        assertEquals("constraint", errors.get(0).error());
    }

    @Test
    void test_HandleTransactionSystemException_NoConstraintViolationException() {
        final TransactionSystemException transactionSystemException = mock(TransactionSystemException.class);
        when(transactionSystemException.getMessage()).thenReturn("transactionSystemException");
        final RollbackException rollbackException = spy(new RollbackException());
        when(transactionSystemException.getCause()).thenReturn(rollbackException);
        when(rollbackException.getCause()).thenReturn(new RuntimeException());

        final ResponseEntity<BoatAPIError> res = translator.handleTransactionSystemException(transactionSystemException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INTERNAL_ERROR.getCategory(), res.getBody().category());
        assertEquals("transactionSystemException", errors.get(0).error());
    }

    @Test
    void test_HandleTransactionSystemException_NoRollbackException() {
        final TransactionSystemException transactionSystemException = mock(TransactionSystemException.class);
        when(transactionSystemException.getMessage()).thenReturn("run-time");
        when(transactionSystemException.getCause()).thenReturn(new RuntimeException());

        final ResponseEntity<BoatAPIError> res = translator.handleTransactionSystemException(transactionSystemException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INTERNAL_ERROR.getCategory(), res.getBody().category());
        assertEquals("run-time", errors.get(0).error());
    }

    @Test
    void test_HandleGenericException() {
        final RuntimeException exception = new RuntimeException("nope!");

        final ResponseEntity<BoatAPIError> res = translator.handleGenericException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertNotNull(res.getBody());

        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);

        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INTERNAL_ERROR.getCategory(), res.getBody().category());
        assertEquals("nope!", errors.get(0).error());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "TECHNICAL_ERROR",
        "FILE_ALREADY_EXIST",
        "INIT_STORAGE_SYSTEM_ERROR",
        "TRANSFER_ERROR"
    })
    void test_HandleStorageException_TECHNICAL_ERROR(final EStorageError storageError) {
        final StorageException exception = new StorageException(storageError, "error");
        final ResponseEntity<BoatAPIError> res = translator.handleStorageException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.TECHNICAL_ERROR.getCategory(), res.getBody().category());
        assertEquals("error", errors.get(0).error());
    }

    @Test
    void test_HandleStorageException_INVALID_REQUEST() {
        final StorageException exception = new StorageException(EStorageError.WRONG_FILE_FORMAT, "error");
        final ResponseEntity<BoatAPIError> res = translator.handleStorageException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INVALID_REQUEST.getCategory(), res.getBody().category());
        assertEquals("error", errors.get(0).error());
    }

    @Test
    void test_HandleStorageException_NOT_FOUND_ERROR() {
        final StorageException exception = new StorageException(EStorageError.FILE_NOT_FOUND, "error");
        final ResponseEntity<BoatAPIError> res = translator.handleStorageException(exception);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.NOT_FOUND_ERROR.getCategory(), res.getBody().category());
        assertEquals("error", errors.get(0).error());
    }

    @Test
    void test_HandleBoatAPIException() {
        final BoatAPIException exception = new BoatAPIException(EBoatAPIError.INVALID_REQUEST, "invalid id");
        final ResponseEntity<BoatAPIError> res = translator.handleBoatAPIException(exception);
        assertEquals(EBoatAPIError.INVALID_REQUEST.getHttpStatus(), res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(exception.getError().getCategory(), res.getBody().category());
        assertEquals("invalid id", errors.get(0).error());
    }

    @Test
    void test_HandleAuthenticationException() {
        final AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("oups!");
        final ResponseEntity<BoatAPIError> res = translator.handleAuthenticationException(exception);
        assertEquals(EBoatAPIError.INVALID_REQUEST.getHttpStatus(), res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INVALID_REQUEST.getCategory(), res.getBody().category());
        assertEquals("oups!", errors.get(0).error());
    }

    @ParameterizedTest
    @CsvSource({
        "ux_user_email,Email already used",
        "ux_user_login,Login already used",
        "any,Technical error",
    })
    void test_HandleDataIntegrityViolationException_Email(final String constraintName,
                                                          final String message) {
        final DataIntegrityViolationException integrityViolationException = mock(DataIntegrityViolationException.class);
        final org.hibernate.exception.ConstraintViolationException cex =
            mock(org.hibernate.exception.ConstraintViolationException.class);
        when(cex.getConstraintName()).thenReturn(constraintName);
        when(integrityViolationException.getCause()).thenReturn(cex);

        final ResponseEntity<BoatAPIError> res = translator
            .handleDataIntegrityViolationException(integrityViolationException);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);

        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INVALID_REQUEST.getCategory(), res.getBody().category());
        assertEquals(message, errors.get(0).error());
    }


    @Test
    void test_HandleDataIntegrityViolationException_NoConstraintViolationException() {
        final DataIntegrityViolationException integrityViolationException = mock(DataIntegrityViolationException.class);
        when(integrityViolationException.getMessage()).thenReturn("oops parent!");
        when(integrityViolationException.getCause()).thenReturn(new RuntimeException("oops!"));

        final ResponseEntity<BoatAPIError> res = translator
            .handleDataIntegrityViolationException(integrityViolationException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INTERNAL_ERROR.getCategory(), res.getBody().category());
        assertEquals("oops parent!", errors.get(0).error());
    }

    @Test
    void test_HandleMaxSizeException() {
        final MaxUploadSizeExceededException maxException = mock(MaxUploadSizeExceededException.class);
        final IllegalStateException stateException = new IllegalStateException(
            new SizeLimitExceededException("error", 12L, 2L));
        when(maxException.getCause()).thenReturn(stateException);

        final ResponseEntity<BoatAPIError> res = translator
            .handleMaxSizeException(maxException);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INVALID_REQUEST.getCategory(), res.getBody().category());
        assertEquals("File too big: 12 bytes — max size: 2 bytes", errors.get(0).error());
    }

    @Test
    void test_HandleMaxSizeException_NoSizeLimitCause() {
        final MaxUploadSizeExceededException maxException = mock(MaxUploadSizeExceededException.class);
        when(maxException.getMessage()).thenReturn("nope!");
        final IllegalStateException stateException = new IllegalStateException(
            new RuntimeException("error"));
        when(maxException.getCause()).thenReturn(stateException);

        final ResponseEntity<BoatAPIError> res = translator
            .handleMaxSizeException(maxException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INTERNAL_ERROR.getCategory(), res.getBody().category());
        assertEquals("nope!", errors.get(0).error());
    }

    @Test
    void test_HandleMaxSizeException_NoStateCause() {
        final MaxUploadSizeExceededException maxException = mock(MaxUploadSizeExceededException.class);
        when(maxException.getMessage()).thenReturn("nope nope!");
        final RuntimeException cause = new RuntimeException("error");
        when(maxException.getCause()).thenReturn(cause);

        final ResponseEntity<BoatAPIError> res = translator
            .handleMaxSizeException(maxException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());

        assertNotNull(res.getBody());
        final List<ProblemDTO> errors = res.getBody().problems();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(EBoatAPIError.INTERNAL_ERROR.getCategory(), res.getBody().category());
        assertEquals("nope nope!", errors.get(0).error());
    }
}
