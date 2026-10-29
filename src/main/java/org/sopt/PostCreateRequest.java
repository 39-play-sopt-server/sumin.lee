package org.sopt;

public record PostCreateRequest(
        Long memberId,
        String title,
        String content,
        Category category
) {
}