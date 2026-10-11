package org.sopt;

public class PostController {
    private final PostView view;
    private final PostService service;

    public PostController(PostView view, PostService service) {
        this.view = view;
        this.service = service;
    }

    public void run() {
        while (true) {
            try {
                int command = view.readCommand();

                switch (command) {
                    case 1:
                        createPost();
                        break;
                    case 2:
                        view.showPosts(service.getPosts());
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
                        view.showMessage("잘못된 메뉴 번호입니다.");
                }
            } catch (NumberFormatException e) {
                view.showMessage("숫자를 입력해주세요.");
            } catch (PostNotFoundException e) {
                view.showMessage(e.getMessage());
            } catch (IllegalArgumentException e) {
                view.showMessage(e.getMessage());
            }
        }
    }

    private void createPost() {
        String title = view.readTitle();
        String content = view.readContent();
        Category category = view.readCategory();

        service.createPost(title, content, category);
        view.showMessage("게시글이 작성되었습니다.");
    }

    private void readPost() {
        long id = view.readPostId("조회");

        Post post = service.getPost(id);
        view.showPost(post);
    }

    private void updatePost() {
        long id = view.readPostId("수정");

        service.getPost(id);

        String title = view.readNewTitle();
        String content = view.readNewContent();
        Category category = view.readCategory();

        service.updatePost(id, title, content, category);
        view.showMessage("게시글이 수정되었습니다.");
    }

    private void deletePost() {
        long id = view.readPostId("삭제");

        service.deletePost(id);
        view.showMessage("게시글이 삭제되었습니다.");
    }
}