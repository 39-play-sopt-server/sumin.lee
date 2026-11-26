package org.sopt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
class JwtProvider {

    private static final String ISSUER = "sopt-assignment";
    private static final String AUDIENCE = "sopt-api";
    static final Duration ACCESS_TTL = Duration.ofMinutes(10);

    private final SecretKey key;
    private final JwtParser parser;

    JwtProvider(@Value("${app.auth.jwt-secret}") String secret) {
        byte[] decoded = Base64.getDecoder().decode(secret);
        if (decoded.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 Base64-encoded bytes");
        }

        key = new SecretKeySpec(decoded, "HmacSHA256");
        parser = Jwts.parser()
                .verifyWith(key)
                .sig().clear().add(Jwts.SIG.HS256).and()
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .require("token_use", "access")
                .build();
    }

    String issueAccessToken(long memberId, UUID sessionId, Instant now) {
        return Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .subject(Long.toString(memberId))
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ACCESS_TTL)))
                .claim("sid", sessionId.toString())
                .claim("token_use", "access")
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    AccessIdentity readAccessToken(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            String subject = claims.getSubject();
            String sessionId = claims.get("sid", String.class);
            if (claims.getExpiration() == null || subject == null || sessionId == null) {
                throw new BadCredentialsException("유효하지 않은 Access Token입니다.");
            }

            long memberId = Long.parseLong(subject);
            if (memberId <= 0) {
                throw new BadCredentialsException("유효하지 않은 Access Token입니다.");
            }
            return new AccessIdentity(memberId, UUID.fromString(sessionId));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException("유효하지 않은 Access Token입니다.", exception);
        }
    }

    record AccessIdentity(long memberId, UUID sessionId) {
    }
}
