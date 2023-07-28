package com.ariche.boatapi.security;

import com.ariche.boatapi.entity.AuthorityEntity;
import com.ariche.boatapi.entity.UserEntity;
import com.ariche.boatapi.service.usermanager.UserService;
import com.ariche.boatapi.service.usermanager.dto.UserForAuth;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @Resource
    @InjectMocks
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_LoadUserByUsername_BlankLogin() {
        assertThrows(UsernameNotFoundException.class,
            () -> service.loadUserByUsername(null));

        assertThrows(UsernameNotFoundException.class,
            () -> service.loadUserByUsername(""));

        verify(userService, never())
            .findUserForAuthByLogin(anyString());
    }

    @Test
    void test_LoadUserByUsername_NotFound() {
        when(userService.findUserForAuthByLogin(anyString()))
            .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> service.loadUserByUsername("login"));

        verify(userService)
            .findUserForAuthByLogin("login");
    }

    @Test
    void test_LoadUserByUsername() {
        final UserEntity entity = new UserEntity();
        entity.setLogin("login");
        entity.setPassword("password");
        final AuthorityEntity authority = new AuthorityEntity();
        authority.setName(CAuthorityNames.ROLE_USER);

        entity.setAuthorities(Collections.singletonList(authority));

        final UserForAuth userForAuth = new UserForAuth("login", "password", CAuthorityNames.ROLE_USER);

        when(userService.findUserForAuthByLogin(anyString()))
            .thenReturn(Optional.of(userForAuth));

        final UserDetails res = service.loadUserByUsername("login");
        assertEquals("login", res.getUsername());
        assertEquals("password", res.getPassword());
        assertEquals(1, res.getAuthorities().size());
        assertEquals(CAuthorityNames.ROLE_USER, new ArrayList<>(res.getAuthorities()).get(0).getAuthority());
    }
}
