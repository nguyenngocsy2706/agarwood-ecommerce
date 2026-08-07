package com.sybanh.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.internal.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(String email) {
        // Implementation for generating JWT token
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
    }

    public boolean isTokenValid(String token, String username) {
        return extractClaim(token, Claims::getSubject).equals(username) &&
                !extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
