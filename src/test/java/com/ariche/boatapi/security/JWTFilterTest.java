package com.ariche.boatapi.security;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JWTFilterTest {

    @Mock
    private JWTTokenProvider tokenProvider;

    @InjectMocks
    @Resource
    private JWTFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_DoFilter() throws ServletException, IOException {
        final String token = "eyJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6IlJPTEVfVVNFUiIsInN1YiI6InRlc3QiLCJpYXQiOjE2OTAzNDY2MTIsImV4cCI6MTY5MDM0NjY3Mn0.iISu1_e3my7NhFUIlA8G8IzRXqois40rMr7_dAQnInw";
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "test-user",
            "test-password",
            Collections.singletonList(new SimpleGrantedAuthority(CAuthorityNames.ROLE_ADMIN))
        );

        when(tokenProvider.isTokenValid(anyString())).thenReturn(true);
        when(tokenProvider.getAuthentication(anyString())).thenReturn(authentication);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString()))
            .thenReturn("Bearer %s".formatted(token));

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(tokenProvider).isTokenValid(token);
        verify(tokenProvider).getAuthentication(token);
    }

    @Test
    void test_DoFilter_InvalidToken() throws ServletException, IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString()))
            .thenReturn("Bearer any-token");

        when(tokenProvider.isTokenValid(anyString())).thenReturn(false);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(tokenProvider).isTokenValid("any-token");
        verify(tokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    void test_DoFilter_BlankToken() throws ServletException, IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString()))
            .thenReturn("any-header");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(tokenProvider, never()).isTokenValid(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    void test_ResolveToken_False() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString()))
            .thenReturn("any-header");

        assertNull(JWTFilter.resolveToken(request));
    }

    @Test
    void test_ResolveToken_True() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString()))
            .thenReturn("Bearer any-token");

        assertEquals("any-token", JWTFilter.resolveToken(request));
    }
}
