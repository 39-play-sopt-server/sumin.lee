package org.sopt;

public class Main {
    public static void main(String[] args) {
        PostRepository repository = new PostRepository();
        PostService service = new PostService(repository);
        PostView view = new PostView();
        PostController controller = new PostController(view, service);

        controller.run();
    }
}