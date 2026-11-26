package org.sopt;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    private static final String SECRET = "YS10ZXN0LWtleS1vbmx5LW5ldmVyLXVzZS1pbi1wcm9kdWN0aW9u";
    private final JwtProvider jwtProvider = new JwtProvider(SECRET);

    @Test
    void decodesIdentityFromValidToken() {
        UUID sessionId = UUID.randomUUID();
        String token = jwtProvider.issueAccessToken(42L, sessionId, Instant.now());

        assertEquals(new JwtProvider.AccessIdentity(42L, sessionId), jwtProvider.readAccessToken(token));
    }

    @Test
    void rejectsExpiredToken() {
        Instant issuedAt = Instant.now().minus(JwtProvider.ACCESS_TTL).minusSeconds(1);
        String token = jwtProvider.issueAccessToken(42L, UUID.randomUUID(), issuedAt);

        assertThrows(BadCredentialsException.class, () -> jwtProvider.readAccessToken(token));
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtProvider otherProvider = new JwtProvider("Yi10ZXN0LWtleS1vbmx5LW5ldmVyLXVzZS1pbi1wcm9kdWN0aW9u");
        String token = otherProvider.issueAccessToken(42L, UUID.randomUUID(), Instant.now());

        assertThrows(BadCredentialsException.class, () -> jwtProvider.readAccessToken(token));
    }
}
