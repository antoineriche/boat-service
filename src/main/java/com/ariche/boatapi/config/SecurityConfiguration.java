package com.ariche.boatapi.config;

import com.ariche.boatapi.security.CAuthorityNames;
import com.ariche.boatapi.security.JWTFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String API_PATH = "/api/**";

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // TODO: Consider using LDAP OR SSO
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JWTFilter filter,
                                           MyAuthenticationEntrypoint securityProblemSupport) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable) // FIXME: DISABLED FOR LOCAL POSTMAN
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/auth/token").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/actuator/**", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.GET, API_PATH).hasAnyAuthority(CAuthorityNames.ROLE_USER, CAuthorityNames.ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, API_PATH).hasAuthority(CAuthorityNames.ROLE_ADMIN)
                .requestMatchers(HttpMethod.PUT, API_PATH).hasAuthority(CAuthorityNames.ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, API_PATH).hasAuthority(CAuthorityNames.ROLE_ADMIN)
                .requestMatchers("/actuator/**").hasAnyAuthority(CAuthorityNames.ROLE_USER, CAuthorityNames.ROLE_ADMIN)

                .anyRequest().denyAll()
            )

            .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(securityProblemSupport))
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}
