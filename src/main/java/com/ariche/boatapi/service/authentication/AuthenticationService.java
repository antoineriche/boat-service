package com.ariche.boatapi.service.authentication;

import com.ariche.boatapi.security.JWTToken;
import com.ariche.boatapi.security.JWTTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider tokenProvider;

    public JWTToken authenticateUser(final CredentialsDTO credentials) {
        final UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(credentials.login(), credentials.password());

        final Authentication authentication = this.authenticationManager
            .authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }
}
