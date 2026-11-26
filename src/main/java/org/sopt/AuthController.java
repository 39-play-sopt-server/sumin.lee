package org.sopt;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping(AuthController.PATH)
@SecurityScheme(name = "csrfProtection", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER,
        paramName = AuthRequestFilter.CSRF_HEADER, description = "Swagger에서 값 1을 입력합니다.")
@SecurityRequirement(name = "csrfProtection")
class AuthController {

    static final String PATH = "/api/v1/auth";
    static final String REFRESH_COOKIE = "__Host-sopt-refresh";

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        return tokenResponse(authService.login(request.username(), request.password()));
    }

    @PostMapping("/refresh")
    ResponseEntity<Void> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String cookie) {
        return tokenResponse(authService.refresh(cookie));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String cookie) {
        authService.logout(cookie);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie())
                .cacheControl(CacheControl.noStore())
                .build();
    }

    static String expiredRefreshCookie() {
        return refreshCookie("", Duration.ZERO);
    }

    private static ResponseEntity<Void> tokenResponse(AuthService.IssuedTokens tokens) {
        return ResponseEntity.noContent()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken(), tokens.refreshMaxAge()))
                .cacheControl(CacheControl.noStore())
                .build();
    }

    private static String refreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString();
    }

    record LoginRequest(String username, String password) {
    }
}
