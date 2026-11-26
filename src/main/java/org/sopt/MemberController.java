package org.sopt;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    public MemberController(MemberService memberService, AuthService authService) {
        this.memberService = memberService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createMember(
            @RequestBody SignupRequest request
    ) {
        long memberId = authService.register(request.name(), request.username(), request.password());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "회원이 생성되었습니다.",
                        memberId
                ));
    }

    @DeleteMapping("/{memberId}")
    @SecurityRequirement(name = SecurityConfig.BEARER_SCHEME)
    public ResponseEntity<ApiResponse<Void>> withdrawMember(
            @PathVariable("memberId") long memberId,
            @AuthenticationPrincipal Long authenticatedMemberId
    ) {
        if (memberId != authenticatedMemberId) {
            throw new AccessDeniedException("본인 계정만 탈퇴할 수 있습니다.");
        }
        memberService.withdrawMember(memberId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, AuthController.expiredRefreshCookie())
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.success("회원 탈퇴가 완료되었습니다.", null));
    }

    record SignupRequest(String name, String username, String password) {
    }
}
