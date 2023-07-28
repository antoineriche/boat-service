package com.ariche.boatapi.service.usermanager;

import com.ariche.boatapi.entity.UserEntity;
import com.ariche.boatapi.repository.UserRepository;
import com.ariche.boatapi.repository.customset.IUserAuthoritySet;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    @Resource
    private com.ariche.boatapi.service.usermanager.UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_FindUserById() {
        when(repository.findById(anyLong()))
            .thenReturn(Optional.of(new UserEntity()));

        assertTrue(service.findUserById(14L).isPresent());

        verify(repository).findById(14L);

    }

    @Test
    void test_FindUserByLogin() {
        final IUserAuthoritySet iUserAuthority = mock(IUserAuthoritySet.class);
        when(iUserAuthority.getLogin()).thenReturn("login");
        when(repository.findUserWithAuthorities(anyString()))
            .thenReturn(Collections.singletonList(iUserAuthority));

        assertTrue(service.findUserForAuthByLogin("login").isPresent());

        verify(repository).findUserWithAuthorities("login");
    }

    @Test
    void test_FindUserForAuthByLogin() {
        when(repository.findUserWithAuthorities(anyString()))
            .thenReturn(Collections.emptyList());

        assertTrue(service.findUserForAuthByLogin("test").isEmpty());

        verify(repository).findUserWithAuthorities("test");
    }
}
