package org.sopt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createMember(
            @RequestBody MemberRequest request
    ) {
        Long memberId = memberService.createMember(request.name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "회원이 생성되었습니다.",
                        memberId
                ));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> withdrawMember(
            @PathVariable("memberId") long memberId
    ) {
        memberService.withdrawMember(memberId);

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "회원 탈퇴가 완료되었습니다.",
                        null
                )
        );
    }
}