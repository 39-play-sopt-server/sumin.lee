package org.sopt;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
class AuthService {

    private static final Duration REFRESH_TTL = Duration.ofDays(7);
    private static final Pattern USERNAME = Pattern.compile("[a-z0-9._-]{3,50}");

    private final MemberRepository members;
    private final RefreshSessionRepository sessions;
    private final PasswordEncoder passwords;
    private final JwtProvider jwtProvider;
    private final String dummyPasswordHash;

    AuthService(MemberRepository members, RefreshSessionRepository sessions,
                PasswordEncoder passwords, JwtProvider jwtProvider) {
        this.members = members;
        this.sessions = sessions;
        this.passwords = passwords;
        this.jwtProvider = jwtProvider;
        this.dummyPasswordHash = passwords.encode(RefreshToken.generateSecret());
    }

    @Transactional
    public long register(String name, String username, String password) {
        String loginId = normalizeUsername(username);
        if (!validPassword(password)) {
            throw new IllegalArgumentException("비밀번호는 8자 이상, UTF-8 기준 72바이트 이하여야 합니다.");
        }
        Member member = new Member(name, loginId, passwords.encode(password));
        try {
            return members.saveAndFlush(member).getId();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUsernameException(exception);
        }
    }

    @Transactional
    public IssuedTokens login(String username, String password) {
        String loginId;
        try {
            loginId = normalizeUsername(username);
        } catch (IllegalArgumentException exception) {
            throw invalidLogin();
        }
        if (!validPassword(password)) {
            throw invalidLogin();
        }

        Member member = members.findByUsernameAndDeletedFalse(loginId).orElse(null);
        String hash = member == null ? dummyPasswordHash : member.getPasswordHash();
        boolean matches = passwords.matches(password, hash);
        if (!matches || member == null) {
            throw invalidLogin();
        }

        // 탈퇴와 세션 발급을 직렬화하되 BCrypt 연산 동안에는 행을 잠그지 않는다.
        Member activeMember = members.findActiveForUpdate(member.getId()).orElseThrow(AuthService::invalidLogin);
        Instant now = Instant.now();
        String secret = RefreshToken.generateSecret();
        RefreshSession session = sessions.save(new RefreshSession(activeMember, RefreshToken.hash(secret),
                now.plus(REFRESH_TTL)));
        return issueTokens(session, secret, now);
    }

    @Transactional
    public IssuedTokens refresh(String cookie) {
        RefreshToken token = RefreshToken.parse(cookie).orElseThrow(AuthService::invalidRefresh);
        RefreshSession session = sessions.findForUpdate(token.sessionId()).orElseThrow(AuthService::invalidRefresh);
        Instant now = Instant.now();
        if (!session.accepts(RefreshToken.hash(token.secret()), now)) {
            throw invalidRefresh();
        }

        String secret = RefreshToken.generateSecret();
        session.rotate(RefreshToken.hash(secret));
        return issueTokens(session, secret, now);
    }

    @Transactional
    public void logout(String cookie) {
        RefreshToken.parse(cookie).flatMap(token -> sessions.findForUpdate(token.sessionId())
                .filter(session -> session.accepts(RefreshToken.hash(token.secret()), Instant.now())))
                .ifPresent(sessions::delete);
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    @Transactional
    public void deleteExpiredSessions() {
        sessions.deleteExpired(Instant.now());
    }

    private IssuedTokens issueTokens(RefreshSession session, String secret, Instant now) {
        return new IssuedTokens(jwtProvider.issueAccessToken(session.memberId(), session.id(), now),
                new RefreshToken(session.id(), secret).value(), Duration.between(now, session.expiresAt()));
    }

    private static String normalizeUsername(String username) {
        String normalized = username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
        if (!USERNAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("아이디는 영문, 숫자, 마침표, 밑줄, 하이픈으로 3~50자여야 합니다.");
        }
        return normalized;
    }

    private static boolean validPassword(String password) {
        return password != null && password.length() <= 72 && !password.isBlank()
                && password.codePointCount(0, password.length()) >= 8
                && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    private static BadCredentialsException invalidLogin() {
        return new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    private static BadCredentialsException invalidRefresh() {
        return new BadCredentialsException("유효하지 않은 Refresh Token입니다.");
    }

    record IssuedTokens(String accessToken, String refreshToken, Duration refreshMaxAge) {
    }
}
