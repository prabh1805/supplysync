package com.supplysync.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Handles JWT token generation, validation, and claim extraction.
 *
 * How it works:
 * - generateToken() creates a signed JWT with user info (email, role, tenantId)
 * - extractEmail() reads the "sub" (subject) claim from the token
 * - isTokenValid() checks signature + expiration
 * - The secret key is used to sign and verify — only this server knows it
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // build the signing key from our secret string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for a user.
     * The token contains: email (as subject), role, tenantId, issued time, expiry time.
     * It's signed with our secret key — any tampering invalidates the signature.
     */
    public String generateToken(String email, String role, String tenantId) {
        return Jwts.builder()
                .subject(email)                                    // "sub" claim — who this token is for
                .claims(Map.of(                                    // custom claims
                        "role", role,
                        "tenantId", tenantId
                ))
                .issuedAt(new Date())                              // "iat" — when token was created
                .expiration(new Date(System.currentTimeMillis() + expiration))  // "exp" — when it expires
                .signWith(getSigningKey())                         // sign with our secret
                .compact();                                        // build the final token string
    }

    /**
     * Extract all claims (payload data) from a token.
     * This also validates the signature — if the token was tampered with,
     * this throws an exception.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())    // verify signature with our secret
                .build()
                .parseSignedClaims(token)       // parse and validate
                .getPayload();                  // return the claims (payload)
    }

    // get email from token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // get role from token
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // get tenantId from token
    public String extractTenantId(String token) {
        return extractClaims(token).get("tenantId", String.class);
    }

    // check if token is expired
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // full validation: signature is valid (extractClaims didn't throw) + not expired
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token); // this throws if signature is invalid
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
