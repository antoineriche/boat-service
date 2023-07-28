package com.ariche.boatapi.security;

import com.ariche.boatapi.MyBoatApplication;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MyBoatApplication.class)
@ActiveProfiles("local")
class JWTTokenProviderTest {

    @Autowired
    private JWTTokenProvider tokenProvider;

    @Test
    void test_Init() {
        assertDoesNotThrow(() -> tokenProvider.init());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "any",
        "eyJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6IlJPTEVfVVNFUiIsInN1YiI6InRlc3QiLCJpYXQiOjE2OTAzNDY2MTIsImV4cCI6MTY5MDM0NjY3Mn0.iISu1_e3my7NhFUIlA8G8IzRXqois40rMr7_dAQnInw"
    })
    void test_IsTokenValid_False(final String token) {
        assertFalse(tokenProvider.isTokenValid(token));
    }

    @Test
    void test_IsTokenValid_True() {
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "test-user",
            "test-password",
            Collections.singletonList(new SimpleGrantedAuthority(CAuthorityNames.ROLE_ADMIN))
        );

        final JWTToken token = tokenProvider.generateToken(authentication);
        assertTrue(tokenProvider.isTokenValid(token.accessToken()));
    }

    @Test
    void test_GenerateToken() {
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "test-user",
            "test-password",
            Collections.singletonList(new SimpleGrantedAuthority(CAuthorityNames.ROLE_ADMIN))
        );

        final JWTToken token = tokenProvider.generateToken(authentication);
        assertEquals("Bearer", token.tokenType());
        assertNotNull(token.accessToken());
        assertTrue(token.expiredIn() > 0);
    }

    @Test
    void test_GetAuthentication() {
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "test-user",
            "test-password",
            Collections.singletonList(new SimpleGrantedAuthority(CAuthorityNames.ROLE_ADMIN))
        );

        final JWTToken token = tokenProvider.generateToken(authentication);

        final Authentication res = tokenProvider.getAuthentication(token.accessToken());
        assertTrue(res.getPrincipal() instanceof User);
        assertEquals("test-user", ((User) res.getPrincipal()).getUsername());
        assertEquals("", ((User) res.getPrincipal()).getPassword());

        assertEquals(token.accessToken(), res.getCredentials());
        final Collection<? extends GrantedAuthority> auth = res.getAuthorities();
        assertEquals(1, auth.size());
        assertTrue(auth.stream().anyMatch(a -> StringUtils.equals(a.getAuthority(), CAuthorityNames.ROLE_ADMIN)));
    }
}
