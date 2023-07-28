package com.ariche.boatapi.service.usermanager.mapper;

import com.ariche.boatapi.entity.UserEntity;
import com.ariche.boatapi.repository.customset.IUserAuthoritySet;
import com.ariche.boatapi.service.usermanager.dto.UserDTO;
import com.ariche.boatapi.service.usermanager.dto.UserForAuth;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserMapperTest {

    @Test
    void test_ToDTO() {
        final UserEntity entity = new UserEntity();
        entity.setId(14L);
        entity.setLogin("login");

        final UserDTO dto = UserMapper.toDTO(entity);
        assertEquals(14L, dto.id());
        assertEquals("login", dto.login());
    }

    @Test
    void test_ToEntity() {
        final UserDTO dto = new UserDTO(45L, "toto", Collections.emptyList());

        final UserEntity entity = UserMapper.toEntity(dto);
        assertEquals(45L, entity.getId());
        assertEquals("toto", entity.getLogin());
    }

    @Test
    void test_ToUserForAuth_EmptyList() {
        assertNull(UserMapper.toUserForAuth(Collections.emptyList()));
        assertNull(UserMapper.toUserForAuth(null));
    }

    @Test
    void test_ToUserForAuth() {
        final IUserAuthoritySet authoritySet = mock(IUserAuthoritySet.class);
        when(authoritySet.getLogin()).thenReturn("login");
        when(authoritySet.getPassword()).thenReturn("password");
        when(authoritySet.getAuthorityName()).thenReturn("authority");

        final UserForAuth auth = UserMapper.toUserForAuth(Collections.singletonList(authoritySet));
        assertNotNull(auth);
        assertEquals("login", auth.login());
        assertEquals("password", auth.password());
        assertNotNull(auth.authorities());
        assertEquals(1, auth.authorities().size());
        assertTrue(auth.authorities().contains("authority"));
    }
}
