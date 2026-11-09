package org.sopt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void createPost(String title, String content, Category category) {
        Post post = new Post(title, content, category);
        repository.save(post);
    }

    public List<Post> getPosts() {
        return repository.findAll();
    }

    @Transactional
    public Post getPost(long id) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        post.increaseViewCount();
        return post;
    }

    @Transactional
    public void updatePost(
            long id,
            String title,
            String content,
            Category category
    ) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        post.update(title, content, category);
    }

    @Transactional
    public void deletePost(long id) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        repository.delete(post);
    }
}