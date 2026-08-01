package com.onestop.identity.security;

import com.onestop.identity.domain.Role;
import com.onestop.identity.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/** Issues and verifies HS256 JWT access tokens. */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration ttl;

    public JwtService(
            @Value("${onestop.security.jwt.secret}") String secret,
            @Value("${onestop.security.jwt.access-token-ttl-minutes:60}") long ttlMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public long getTtlSeconds() {
        return ttl.toSeconds();
    }
}
