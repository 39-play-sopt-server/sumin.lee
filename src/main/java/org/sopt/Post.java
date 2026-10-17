package org.sopt;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime createdAt;
    private long viewCount;

    protected Post() {
    }

    public Post(String title, String content, Category category) {
        validate(title, content, category);

        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.viewCount = 0;
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

    public void increaseViewCount() {
        this.viewCount++;
    }

    public Long getId() {
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

    public long getViewCount() {
        return viewCount;
    }
}