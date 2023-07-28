package com.ariche.boatapi.security;

import com.ariche.boatapi.service.usermanager.UserService;
import com.ariche.boatapi.service.usermanager.dto.UserForAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component(value = "myDetailService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isBlank(username)) {
            throw new UsernameNotFoundException("username is empty");
        }

        final UserForAuth user = userService.findUserForAuthByLogin(username)
            .orElseThrow(() -> new UsernameNotFoundException("User %s not found".formatted(username)));

        final List<SimpleGrantedAuthority> authorities = user.authorities().stream()
            .map(SimpleGrantedAuthority::new)
            .toList();

        return new User(user.login(), user.password(), authorities);
    }

}
