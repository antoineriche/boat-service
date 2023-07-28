package com.ariche.boatapi.config;

import com.ariche.boatapi.web.errors.ErrorCodeGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class MyAuthenticationEntrypointTest {

    @Mock
    private ErrorCodeGenerator codeGenerator;

    @InjectMocks
    @Resource
    private MyAuthenticationEntrypoint authenticationEntrypoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_Commence() throws IOException {
        final MockHttpServletRequest request = spy(new MockHttpServletRequest());
        final MockHttpServletResponse response = spy(new MockHttpServletResponse());
        final AuthenticationException exception = mock(AuthenticationException.class);
        final OutputStream outputStream = mock(OutputStream.class);

        try (MockedConstruction<ServletServerHttpResponse> mockedConstruction = mockConstruction(
            ServletServerHttpResponse.class, (res, context) -> {
                doNothing().when(res).setStatusCode(any(HttpStatus.class));
                doReturn(response).when(res).getServletResponse();
                doReturn(outputStream).when(res).getBody();
            })) {

            authenticationEntrypoint.commence(request, response, exception);
            verify(codeGenerator).generateErrorCode();
            verify(response).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            verify(outputStream).write(any(byte[].class));
        }
    }

    @Test
    void test_Commence_IOException() throws IOException {
        final MockHttpServletRequest request = spy(new MockHttpServletRequest());
        final MockHttpServletResponse response = spy(new MockHttpServletResponse());
        final AuthenticationException exception = mock(AuthenticationException.class);
        final OutputStream outputStream = mock(OutputStream.class);
        doThrow(IOException.class)
            .when(outputStream)
            .write(any(byte[].class));

        try (MockedConstruction<ServletServerHttpResponse> mockedConstruction = mockConstruction(
            ServletServerHttpResponse.class, (res, context) -> {
                doNothing().when(res).setStatusCode(any(HttpStatus.class));
                doReturn(response).when(res).getServletResponse();
                doReturn(outputStream).when(res).getBody();
            })) {

            assertDoesNotThrow(() -> authenticationEntrypoint.commence(request, response, exception));
            verify(codeGenerator).generateErrorCode();
            verify(response).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            verify(outputStream).write(any(byte[].class));
        }
    }
}
