package org.sopt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(
            @RequestBody PostCreateRequest request
    ) {
        postService.createPost(
                request.memberId(),
                request.title(),
                request.content(),
                request.category()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>success(
                        "게시글이 작성되었습니다.",
                        null
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostPageResponse>> readPosts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        PostPageResponse response = postService.getPosts(page, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "게시글 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> readPost(
            @PathVariable("postId") long postId
    ) {
        PostResponse response = postService.getPost(postId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "게시글 조회에 성공했습니다.",
                        response
                )
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable("postId") long postId,
            @RequestBody PostRequest request
    ) {
        postService.updatePost(
                postId,
                request.title(),
                request.content(),
                request.category()
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "게시글이 수정되었습니다.",
                        null
                )
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable("postId") long postId
    ) {
        postService.deletePost(postId);

        return ResponseEntity.ok(
                ApiResponse.<Void>success(
                        "게시글이 삭제되었습니다.",
                        null
                )
        );
    }
}