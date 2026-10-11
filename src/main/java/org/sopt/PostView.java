package org.sopt;

import java.util.List;
import java.util.Scanner;

public class PostView {
    private final Scanner scanner = new Scanner(System.in);

    public int readCommand() {
        System.out.println("\n=== 게시판 ===");
        System.out.println("1. 게시글 작성");
        System.out.println("2. 게시글 목록 조회");
        System.out.println("3. 게시글 단건 조회");
        System.out.println("4. 게시글 수정");
        System.out.println("5. 게시글 삭제");
        System.out.println("6. 종료");

        return Integer.parseInt(readText("선택: "));
    }

    public String readTitle() {
        return readText("제목: ");
    }

    public String readContent() {
        return readText("내용: ");
    }

    public String readNewTitle() {
        return readText("새로운 제목: ");
    }

    public String readNewContent() {
        return readText("새로운 내용: ");
    }

    public long readPostId(String action) {
        return Long.parseLong(readText(action + "할 게시글 ID: "));
    }

    public Category readCategory() {
        System.out.println("1. 일반 / 2. 질문 / 3. 정보");
        int number = Integer.parseInt(readText("카테고리 선택: "));

        switch (number) {
            case 1:
                return Category.GENERAL;
            case 2:
                return Category.QUESTION;
            case 3:
                return Category.INFORMATION;
            default:
                throw new IllegalArgumentException("잘못된 카테고리입니다.");
        }
    }

    public void showPosts(List<Post> posts) {
        System.out.println("\n=== 게시글 목록 ===");

        if (posts.isEmpty()) {
            showMessage("게시글이 없습니다.");
            return;
        }

        for (Post post : posts) {
            System.out.println(
                    post.getId() + ". [" + post.getCategory() + "] "
                            + post.getTitle()
            );
        }
    }

    public void showPost(Post post) {
        System.out.println("\n=== 게시글 ===");
        System.out.println("ID: " + post.getId());
        System.out.println("제목: " + post.getTitle());
        System.out.println("내용: " + post.getContent());
        System.out.println("카테고리: " + post.getCategory());
        System.out.println("작성 시간: " + post.getCreatedAt());
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    private String readText(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }
}