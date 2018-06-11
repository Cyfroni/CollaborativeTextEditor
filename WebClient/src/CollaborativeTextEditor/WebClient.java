package CollaborativeTextEditor;

import CollaborativeTextEditor.controller.MyController;

import java.awt.*;

public class WebClient {

    public static void main(String[] args) {
        EventQueue.invokeLater(MyController::new);
    }
}
