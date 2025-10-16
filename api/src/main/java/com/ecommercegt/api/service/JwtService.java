package com.ecommercegt.api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expMinutes}") long expMinutes) {

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expMinutes = expMinutes;
    }

    public String generate(Long uid, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("uid", uid)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expMinutes, ChronoUnit.MINUTES)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public String getRole(String token) {
        var claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        Object role = claims.get("role");
        return role != null ? role.toString() : null;
    }
}