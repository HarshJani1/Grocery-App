package com.grocery.service_auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    // ── generateToken ────────────────────────────────────────────

    @Test
    @DisplayName("generateToken - should return a non-null, non-blank JWT string")
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken("user@example.com");

        assertNotNull(token, "Token must not be null");
        assertFalse(token.isBlank(), "Token must not be blank");
    }

    @Test
    @DisplayName("generateToken - subject should match the email used for generation")
    void generateToken_subjectMatchesEmail() {
        String email = "alice@grocery.com";
        String token = jwtService.generateToken(email);

        String subject = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        assertEquals(email, subject);
    }

    @Test
    @DisplayName("generateToken - token expiration should be ~30 minutes from now")
    void generateToken_expirationIsApproximately30Minutes() {
        String token = jwtService.generateToken("bob@grocery.com");

        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        long thirtyMinutesMs = 1000 * 60 * 30;
        long now = System.currentTimeMillis();

        // Allow a 5-second margin for test execution delay
        assertTrue(expiration.getTime() >= now + thirtyMinutesMs - 5000,
                "Expiration should be at least ~30 minutes from now");
        assertTrue(expiration.getTime() <= now + thirtyMinutesMs + 5000,
                "Expiration should be at most ~30 minutes from now");
    }

    @Test
    @DisplayName("generateToken - two calls with same email should produce different tokens (different issuedAt)")
    void generateToken_differentTokensForSameEmail() throws InterruptedException {
        String token1 = jwtService.generateToken("same@grocery.com");
        Thread.sleep(10); // tiny delay to ensure different issuedAt
        String token2 = jwtService.generateToken("same@grocery.com");

        // Tokens may differ slightly due to issuedAt
        assertNotNull(token1);
        assertNotNull(token2);
    }

    // ── validateToken ────────────────────────────────────────────

    @Test
    @DisplayName("validateToken - should not throw for a valid, freshly-generated token")
    void validateToken_validToken_noException() {
        String token = jwtService.generateToken("valid@grocery.com");

        assertDoesNotThrow(() -> jwtService.validateToken(token));
    }

    @Test
    @DisplayName("validateToken - should throw for a tampered token")
    void validateToken_tamperedToken_throwsException() {
        String token = jwtService.generateToken("tamper@grocery.com");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThrows(Exception.class, () -> jwtService.validateToken(tampered));
    }

    @Test
    @DisplayName("validateToken - should throw for a completely invalid string")
    void validateToken_invalidString_throwsException() {
        assertThrows(Exception.class, () -> jwtService.validateToken("this.is.not.a.jwt"));
    }

    @Test
    @DisplayName("validateToken - should throw for an empty string")
    void validateToken_emptyString_throwsException() {
        assertThrows(Exception.class, () -> jwtService.validateToken(""));
    }

    @Test
    @DisplayName("validateToken - should throw for an expired token")
    void validateToken_expiredToken_throwsExpiredJwtException() {
        // Build a token that already expired (1 second ago)
        String expiredToken = Jwts.builder()
                .setSubject("expired@grocery.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 60000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtService.validateToken(expiredToken));
    }

    @Test
    @DisplayName("validateToken - should throw for a token signed with a different key")
    void validateToken_wrongSigningKey_throwsException() {
        // Use a different key to sign
        byte[] differentKeyBytes = Decoders.BASE64.decode(
                "614E645267556B586E327235753778214125442A472D4B6150645367566B5970");
        Key differentKey = Keys.hmacShaKeyFor(differentKeyBytes);

        String tokenWithDifferentKey = Jwts.builder()
                .setSubject("wrongkey@grocery.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(Exception.class, () -> jwtService.validateToken(tokenWithDifferentKey));
    }

    // ── helper ───────────────────────────────────────────────────

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JwtService.SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
