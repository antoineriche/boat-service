package com.ariche.boatapi.service.usermanager;

import com.ariche.boatapi.repository.UserRepository;
import com.ariche.boatapi.repository.customset.IUserAuthoritySet;
import com.ariche.boatapi.service.usermanager.dto.UserDTO;
import com.ariche.boatapi.service.usermanager.dto.UserForAuth;
import com.ariche.boatapi.service.usermanager.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Find a user by id
     * @param id the user id
     * @return the {@link UserDTO} identified by its id
     */
    public Optional<UserDTO> findUserById(final Long id) {
        return userRepository.findById(id)
            .map(UserMapper::toDTO);
    }

    /**
     * Find a user by login
     * @param login the user login
     * @return the {@link UserDTO} identified by its login
     */
    public Optional<UserForAuth> findUserForAuthByLogin(final String login) {
        final List<IUserAuthoritySet> authorityList = userRepository.findUserWithAuthorities(login);
        if (CollectionUtils.isEmpty(authorityList)) {
            return Optional.empty();
        }

        return Optional.ofNullable(UserMapper.toUserForAuth(authorityList));
    }
}
