package CollaborativeTextEditor.view;

import CollaborativeTextEditor.model.MyModel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    MyModel model;
    JComboBox<String> docBox;
    JButton bNewDoc = new JButton("Nowy dokument");
    JButton bBack = new JButton("<-");
    JButton bOK = new JButton("OK");
    JTextField newdoc = new JTextField("");
    StageMenu stageMenu;

    public enum StageMenu {
        MENU, NEWDOC
    }

    public void setStage(StageMenu st) {
        if (stageMenu == st) {
            return;
        }
        stageMenu = st;
        getContentPane().removeAll();
        if (stageMenu == StageMenu.MENU) {
            add(docBox, BorderLayout.WEST);
            add(bNewDoc, BorderLayout.CENTER);
        } else {
            add(newdoc, BorderLayout.CENTER);
            add(bBack, BorderLayout.WEST);
            add(bOK, BorderLayout.EAST);
        }
        update();
    }

    MainFrame(MyModel myModel) {
        super();
        model = myModel;
        setSize(350, 64);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        bNewDoc.setVerticalTextPosition(AbstractButton.CENTER);
        bNewDoc.setHorizontalTextPosition(AbstractButton.LEADING);

        bBack.setVerticalTextPosition(AbstractButton.CENTER);
        bBack.setHorizontalTextPosition(AbstractButton.LEADING);

        bOK.setVerticalTextPosition(AbstractButton.CENTER);
        bOK.setHorizontalTextPosition(AbstractButton.LEADING);

        docBox = new JComboBox<>(model.getDocList());
        docBox.setSize(200, 64);

    }


    public void update() {
        revalidate();
        repaint();
        pack();
    }

    public String getNewDocName() {
        return newdoc.getText();
    }
}
