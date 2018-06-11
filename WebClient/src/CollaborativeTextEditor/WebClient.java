package CollaborativeTextEditor;

import CollaborativeTextEditor.controller.MyController;

import java.awt.*;

public class WebClient {

    public static void main(String[] args) {
//        System.out.println("abc");
        EventQueue.invokeLater(MyController::new);
    }
}
