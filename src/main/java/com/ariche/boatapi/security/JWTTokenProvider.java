package com.ariche.boatapi.security;

import com.ariche.boatapi.errors.BoatAPIException;
import com.ariche.boatapi.errors.EBoatAPIError;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JWTTokenProvider {

    private static final String AUTHORITIES = "authorities";
    @Value("${token.secret-key:}")
    private String jwtSecretKey;

    @Value("${token.validity:}")
    private Long jwtMaxDuration = ChronoUnit.DAYS.getDuration().toSeconds();

    private Key key;

    @PostConstruct
    public void init() {
        final byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("[{}] Error while parsing JWT: {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    public JWTToken generateToken(Authentication authentication) {
        final String token = Jwts.builder()
            .claim(AUTHORITIES, authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")))
            .setSubject(authentication.getName())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + jwtMaxDuration * 1_000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return new JWTToken(token, jwtMaxDuration, "Bearer");
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
            .parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token).getBody();

        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        final User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

}
