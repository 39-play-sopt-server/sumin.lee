package org.sopt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Post extends BaseEntity {

    private static final int MAX_TITLE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private long viewCount = 0;

    protected Post() {
    }

    public Post(
            Member member,
            String title,
            String content,
            Category category
    ) {
        validate(title, content, category);

        if (member == null) {
            throw new IllegalArgumentException("작성자가 필요합니다.");
        }

        if (member.isDeleted()) {
            throw new IllegalArgumentException(
                    "탈퇴한 회원은 게시글을 작성할 수 없습니다."
            );
        }

        this.member = member;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    private void validate(
            String title,
            String content,
            Category category
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("제목은 255자 이하여야 합니다.");
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

    public Member getMember() {
        return member;
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

    public long getViewCount() {
        return viewCount;
    }
}