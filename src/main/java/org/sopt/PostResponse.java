package org.sopt;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        Category category,
        String authorName,
        long viewCount,
        LocalDateTime createdAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getMember().getName(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}