package com.ariche.boatapi.service.usermanager.mapper;

import com.ariche.boatapi.entity.BoatEntity;
import com.ariche.boatapi.entity.UserEntity;
import com.ariche.boatapi.repository.customset.IUserAuthoritySet;
import com.ariche.boatapi.service.usermanager.dto.UserDTO;
import com.ariche.boatapi.service.usermanager.dto.UserForAuth;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public final class UserMapper {

    /**
     * Map a {@link BoatEntity} to a {@link UserDTO}
     * @param userEntity the boat entity to be mapped
     * @return the {@link UserDTO}
     */
    public static UserDTO toDTO(final UserEntity userEntity) {
        return new UserDTO(userEntity.getId(), userEntity.getLogin());
    }

    /**
     * Map a {@link UserDTO} to a {@link UserEntity}
     * @param userDTO the user dto to be mapped
     * @return the {@link UserEntity}
     */
    public static UserEntity toEntity(final UserDTO userDTO) {
        final UserEntity entity = new UserEntity();
        entity.setId(userDTO.id());
        entity.setLogin(userDTO.login());
        return entity;
    }

    /**
     * Map a list of {@link IUserAuthoritySet} to a {@link UserForAuth}
     * @param authorityList the authority list extracted from database, containing join of user and authorities
     * @return the {@link UserForAuth} the user model used for auth
     */
    public static UserForAuth toUserForAuth(final List<IUserAuthoritySet> authorityList) {
        if (CollectionUtils.isEmpty(authorityList)) {
            return null;
        }

        return new UserForAuth(authorityList.get(0).getLogin(),
            authorityList.get(0).getPassword(),
            authorityList.stream().map(IUserAuthoritySet::getAuthorityName).toList());
    }

    private UserMapper() {
    }


}
