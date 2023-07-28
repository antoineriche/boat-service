package com.ariche.boatapi.service.authentication;

import com.ariche.boatapi.security.JWTToken;
import com.ariche.boatapi.security.JWTTokenProvider;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JWTTokenProvider tokenProvider;

    @InjectMocks
    @Resource
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_AuthenticateUser_AuthenticationException() {
        final CredentialsDTO credentials = new CredentialsDTO("login", "password");
        final AuthenticationException exception = mock(AuthenticationException.class);
        doThrow(exception)
            .when(authenticationManager)
            .authenticate(any(Authentication.class));

        assertThrows(AuthenticationException.class,
            () -> service.authenticateUser(credentials));

        verify(tokenProvider, never())
            .generateToken(any(Authentication.class));
    }

    @Test
    void test_AuthenticateUser() {
        final CredentialsDTO credentials = new CredentialsDTO("login", "password");
        final JWTToken token = new JWTToken("token", 25_000L, "Bearer");

        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(credentials.login(), credentials.password()));
        when(tokenProvider.generateToken(any(Authentication.class)))
            .thenReturn(token);

        assertEquals(token, service.authenticateUser(credentials));

        final ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);

        verify(tokenProvider).generateToken(captor.capture());
        assertEquals("login", captor.getValue().getPrincipal());

    }
}
