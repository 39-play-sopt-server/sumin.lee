package org.sopt;

import java.util.ArrayList;
import java.util.List;

public class PostController {
    private final List<Post> posts = new ArrayList<>();
    private final PostView view;

    public PostController(PostView view) {
        this.view = view;
    }

    public void run() {
        while (true) {
            int command = view.readCommand();

            switch (command) {
                case 1:
                    createPost();
                    break;
                case 2:
                    view.showPosts(posts);
                    break;
                case 3:
                    readPost();
                    break;
                case 4:
                    updatePost();
                    break;
                case 5:
                    deletePost();
                    break;
                case 6:
                    view.showMessage("프로그램을 종료합니다.");
                    return;
                default:
                    view.showMessage("잘못된 입력입니다.");
            }
        }
    }

    private void createPost() {
        String title = view.readTitle();
        String content = view.readContent();

        posts.add(new Post(title, content));
        view.showMessage("게시글이 작성되었습니다.");
    }

    private void readPost() {
        int index = findPostIndex("조회");
        if (index == -1) {
            return;
        }

        view.showPost(posts.get(index));
    }

    private void updatePost() {
        int index = findPostIndex("수정");
        if (index == -1) {
            return;
        }

        String title = view.readNewTitle();
        String content = view.readNewContent();

        posts.get(index).update(title, content);
        view.showMessage("게시글이 수정되었습니다.");
    }

    private void deletePost() {
        int index = findPostIndex("삭제");
        if (index == -1) {
            return;
        }

        posts.remove(index);
        view.showMessage("게시글이 삭제되었습니다.");
    }

    private int findPostIndex(String action) {
        if (posts.isEmpty()) {
            view.showMessage("게시글이 없습니다.");
            return -1;
        }

        int index = view.readPostNumber(action) - 1;

        if (index < 0 || index >= posts.size()) {
            view.showMessage("존재하지 않는 게시글입니다.");
            return -1;
        }

        return index;
    }
}