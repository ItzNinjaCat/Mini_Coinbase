package com.authservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject) {

        Claims claims = Jwts.claims().setSubject(subject);
        Date expirationDate = Date.from(Instant.now().plusMillis(expiration));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }
    public Boolean validateToken(String token, String userId) {
        final String tokenUserId = parseToken(token).getSubject();
        final Date expirationDate = parseToken(token).getExpiration();
        return (userId.equals(tokenUserId) && expirationDate.after(new Date()));
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getTokenExpiration(String token) {
        return parseToken(token).getExpiration().getTime();
    }
}