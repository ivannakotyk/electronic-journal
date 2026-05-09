package com.kpi.ejournal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long expirationMillis;
    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(secret.getBytes())));
        this.expirationMillis = expirationMinutes * 60 * 1000;
    }
    public String generateToken(Long userId, String login, String role) {
        Date now = new Date();
        return Jwts.builder().subject(login).claim("userId", userId).claim("role", role).issuedAt(now).expiration(new Date(now.getTime() + expirationMillis)).signWith(secretKey).compact();
    }
    public Claims parse(String token) { return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload(); }
}
