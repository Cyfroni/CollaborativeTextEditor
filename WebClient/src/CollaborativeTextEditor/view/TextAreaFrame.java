package CollaborativeTextEditor.view;

import CollaborativeTextEditor.model.MyModel;

import javax.swing.*;
import java.awt.*;

public class TextAreaFrame extends JFrame {
    MyModel textArea;


    public void update() {
        revalidate();
        repaint();
        pack();
    }

    public TextAreaFrame(MyModel jTextArea) {
        textArea = jTextArea;
        Dimension dimension=new Dimension(500,500);
        setMinimumSize(dimension);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        add(textArea);
    }
}
