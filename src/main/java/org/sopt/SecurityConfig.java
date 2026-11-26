package org.sopt;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Configuration
@EnableScheduling
@SecurityScheme(name = SecurityConfig.BEARER_SCHEME, type = SecuritySchemeType.HTTP, scheme = "bearer",
        bearerFormat = "JWT")
class SecurityConfig {

    static final String BEARER_SCHEME = "accessToken";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProvider jwtProvider,
                                            RefreshSessionRepository sessions,
                                            CorsConfigurationSource corsConfigurationSource,
                                            JsonMapper jsonMapper) throws Exception {
        AuthenticationEntryPoint unauthorized = (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            writeError(response, jsonMapper, HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
        };
        AccessDeniedHandler forbidden = (request, response, exception) ->
                writeError(response, jsonMapper, HttpServletResponse.SC_FORBIDDEN, exception.getMessage());

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 쿠키를 사용하는 인증 요청은 AuthRequestFilter가 검사한다.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .authorizeHttpRequests(routes -> routes
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members", AuthController.PATH + "/login",
                                AuthController.PATH + "/refresh", AuthController.PATH + "/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts", "/api/v1/posts/{postId}",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/{postId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/{postId}",
                                "/api/v1/members/{memberId}").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(new AccessTokenFilter(jwtProvider, sessions, unauthorized),
                        AnonymousAuthenticationFilter.class)
                .addFilterBefore(new AuthRequestFilter(forbidden), AccessTokenFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.auth.allowed-origin}") String origin) {
        URI uri = URI.create(origin);
        if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                || uri.getHost() == null || !origin.equals(uri.getScheme() + "://" + uri.getRawAuthority())
                || uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("AUTH_ALLOWED_ORIGIN must be an explicit HTTP(S) origin");
        }

        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(origin));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        cors.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION,
                AuthRequestFilter.CSRF_HEADER));
        cors.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", cors);
        return source;
    }

    private static void writeError(HttpServletResponse response, JsonMapper jsonMapper,
                                   int status, String message) throws IOException {
        response.setStatus(status);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), ApiResponse.failure(message));
    }
}
