// src/main/java/com/example/demo/auth/JwtService.java
package com.example.demo.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    // For demo only—move to config/env and keep secret safe!
    private final Key key = Keys.hmacShaKeyFor("change-this-to-32+char-secret-key-change-me".getBytes());
    private final long ttlMs = 1000L * 60 * 60; // 1 hour

    public String generate(String subject) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ttlMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateAndGetSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}
