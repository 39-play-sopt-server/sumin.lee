package org.sopt;

import java.time.LocalDateTime;

public class Post {
    private final long id;
    private String title;
    private String content;
    private Category category;
    private final LocalDateTime createdAt;

    public Post(long id, String title, String content, Category category) {
        validate(title, content, category);

        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    private void validate(String title, String content, Category category) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("본문은 비어 있을 수 없습니다.");
        }

        if (category == null) {
            throw new IllegalArgumentException("카테고리를 선택해야 합니다.");
        }
    }

    public void update(String title, String content, Category category) {
        validate(title, content, category);

        this.title = title;
        this.content = content;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}