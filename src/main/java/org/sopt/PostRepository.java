package org.sopt;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {
    private final List<Post> posts = new ArrayList<>();

    public void save(Post post) {
        posts.add(post);
    }

    public List<Post> findAll() {
        return new ArrayList<>(posts);
    }

    public Post findById(long id) {
        for (Post post : posts) {
            if (post.getId() == id) {
                return post;
            }
        }

        throw new PostNotFoundException(id);
    }

    public void deleteById(long id) {
        Post post = findById(id);
        posts.remove(post);
    }
}