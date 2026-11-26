package org.sopt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

class AuthRequestFilter extends OncePerRequestFilter {

    static final String CSRF_HEADER = "X-CSRF-Protection";
    private static final RequestMatcher AUTH_POSTS = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, AuthController.PATH + "/**");

    private final AccessDeniedHandler forbidden;

    AuthRequestFilter(AccessDeniedHandler forbidden) {
        this.forbidden = forbidden;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 브라우저의 교차 출처 요청은 이 헤더를 보내기 전에 CORS 사전 요청을 통과해야 한다.
        if (AUTH_POSTS.matches(request) && !"1".equals(request.getHeader(CSRF_HEADER))) {
            forbidden.handle(request, response, new AccessDeniedException("CSRF 헤더가 필요합니다."));
            return;
        }
        chain.doFilter(request, response);
    }
}
