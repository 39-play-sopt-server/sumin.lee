package org.sopt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(
            @RequestBody PostRequest request
    ) {
        service.createPost(
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
    public ResponseEntity<ApiResponse<List<Post>>> readPosts() {
        List<Post> posts = service.getPosts();

        return ResponseEntity.ok(
                ApiResponse.success("게시글 목록 조회에 성공했습니다.", posts)
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<Post>> readPost(
            @PathVariable("postId") long postId
    ) {
        Post post = service.getPost(postId);

        return ResponseEntity.ok(
                ApiResponse.success("게시글 조회에 성공했습니다.", post)
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable("postId") long postId,
            @RequestBody PostRequest request
    ) {
        service.updatePost(
                postId,
                request.title(),
                request.content(),
                request.category()
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>success("게시글이 수정되었습니다.", null)
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable("postId") long postId
    ) {
        service.deletePost(postId);

        return ResponseEntity.ok(
                ApiResponse.<Void>success("게시글이 삭제되었습니다.", null)
        );
    }
}