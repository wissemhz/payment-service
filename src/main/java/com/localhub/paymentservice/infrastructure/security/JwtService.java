package com.localhub.paymentservice.infrastructure.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

/**
 * JWT utility service for token validation.
 * Uses HMAC-based shared secret (HS256) for inter-service communication.
 */
@Service
@Slf4j
public class JwtService {

    private final JwtDecoder jwtDecoder;
    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(this.secretKey).build();
    }

    public String extractUsername(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getExpiresAt() == null || jwt.getExpiresAt().isAfter(java.time.Instant.now());
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Date extractExpiration(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getExpirationTime();
        } catch (ParseException e) {
            log.error("Failed to parse JWT: {}", e.getMessage());
            return null;
        }
    }
}
