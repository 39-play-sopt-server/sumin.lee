package org.sopt;

public record PostRequest(
        String title,
        String content,
        Category category
) {
}