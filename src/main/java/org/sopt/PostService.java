package org.sopt;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository repository;
    private long nextId = 1L;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public void createPost(String title, String content, Category category) {
        Post post = new Post(nextId, title, content, category);
        repository.save(post);
        nextId++;
    }

    public List<Post> getPosts() {
        return repository.findAll();
    }

    public Post getPost(long id) {
        return repository.findById(id);
    }

    public void updatePost(
            long id,
            String title,
            String content,
            Category category
    ) {
        Post post = repository.findById(id);
        post.update(title, content, category);
    }

    public void deletePost(long id) {
        repository.deleteById(id);
    }
}