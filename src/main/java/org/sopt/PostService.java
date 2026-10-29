package org.sopt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostService(
            PostRepository postRepository,
            MemberRepository memberRepository
    ) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void createPost(
            Long memberId,
            String title,
            String content,
            Category category
    ) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException(
                    "작성자 ID는 양수여야 합니다."
            );
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new MemberNotFoundException(memberId)
                );

        Post post = new Post(member, title, content, category);
        postRepository.save(post);
    }

    public PostPageResponse getPosts(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "페이지 번호는 0 이상이어야 합니다."
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "페이지 크기는 1 이상 100 이하여야 합니다."
            );
        }

        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> postPage = postRepository.findAll(pageable);

        return PostPageResponse.from(postPage);
    }

    @Transactional
    public PostResponse getPost(long id) {
        Post post = findPost(id);
        post.increaseViewCount();

        return PostResponse.from(post);
    }

    @Transactional
    public void updatePost(
            long id,
            String title,
            String content,
            Category category
    ) {
        Post post = findPost(id);
        post.update(title, content, category);
    }

    @Transactional
    public void deletePost(long id) {
        Post post = findPost(id);
        postRepository.delete(post);
    }

    private Post findPost(long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }
}