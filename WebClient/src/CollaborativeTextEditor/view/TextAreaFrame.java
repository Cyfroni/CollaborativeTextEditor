package CollaborativeTextEditor.view;

import CollaborativeTextEditor.model.MyModel;

import javax.swing.*;

public class TextAreaFrame extends JFrame {
    MyModel textArea;


    public void update() {
        revalidate();
        repaint();
        pack();
    }

    public TextAreaFrame(MyModel jTextArea) {
        textArea = jTextArea;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        add(textArea);
    }
}
