package org.sopt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

class AccessTokenFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RefreshSessionRepository sessions;
    private final AuthenticationEntryPoint unauthorized;

    AccessTokenFilter(JwtProvider jwtProvider, RefreshSessionRepository sessions,
                      AuthenticationEntryPoint unauthorized) {
        this.jwtProvider = jwtProvider;
        this.sessions = sessions;
        this.unauthorized = unauthorized;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PathPatternRequestMatcher.withDefaults().matcher(AuthController.PATH + "/**").matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
                throw new BadCredentialsException("Bearer 인증이 필요합니다.");
            }

            JwtProvider.AccessIdentity identity = jwtProvider.readAccessToken(header.substring(7));
            if (!sessions.existsByIdAndMemberIdAndMemberDeletedFalseAndExpiresAtAfter(
                    identity.sessionId(), identity.memberId(), Instant.now())) {
                throw new BadCredentialsException("만료되었거나 종료된 로그인입니다.");
            }

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    identity.memberId(), null, List.of()));
            SecurityContextHolder.setContext(context);
        } catch (BadCredentialsException exception) {
            unauthorized.commence(request, response, exception);
            return;
        }

        chain.doFilter(request, response);
    }
}
