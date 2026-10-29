package org.sopt;

import org.springframework.data.domain.Page;

import java.util.List;

public record PostPageResponse(
        List<PostResponse> posts,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static PostPageResponse from(Page<Post> postPage) {
        List<PostResponse> posts = postPage.getContent()
                .stream()
                .map(PostResponse::from)
                .toList();

        return new PostPageResponse(
                posts,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.hasNext()
        );
    }
}