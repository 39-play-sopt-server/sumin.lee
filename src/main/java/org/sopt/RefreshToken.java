package org.sopt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

record RefreshToken(UUID sessionId, String secret) {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern FORMAT = Pattern.compile("[0-9a-f-]{36}\\.[A-Za-z0-9_-]{43}");

    static String generateSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static Optional<RefreshToken> parse(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            return Optional.empty();
        }

        try {
            UUID id = UUID.fromString(value.substring(0, 36));
            if (id.toString().equals(value.substring(0, 36))) {
                return Optional.of(new RefreshToken(id, value.substring(37)));
            }
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    static String hash(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    String value() {
        return sessionId + "." + secret;
    }
}
