package org.sopt;

import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public String createPost(@RequestBody PostRequest request) {
        service.createPost(
                request.title(),
                request.content(),
                request.category()
        );

        return "게시글이 작성되었습니다.";
    }

    @GetMapping
    public List<Post> readPosts() {
        return service.getPosts();
    }

    @GetMapping("/{postId}")
    public Post readPost(@PathVariable("postId") long postId) {
        return service.getPost(postId);
    }

    @PutMapping("/{postId}")
    public String updatePost(
            @PathVariable("postId") long postId,
            @RequestBody PostRequest request
    ) {
        service.updatePost(
                postId,
                request.title(),
                request.content(),
                request.category()
        );

        return "게시글이 수정되었습니다.";
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable("postId") long postId) {
        service.deletePost(postId);
    }
}