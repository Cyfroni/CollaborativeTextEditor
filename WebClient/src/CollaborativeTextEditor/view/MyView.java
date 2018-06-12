package CollaborativeTextEditor.view;

import CollaborativeTextEditor.controller.*;
import CollaborativeTextEditor.model.MyModel;

public class MyView {
    MyModel model;

    public MainFrame mainFrame;
    public TextAreaFrame textAreaFrame;

    StageView stageView;

    public String getNewDocName() {
        return mainFrame.getNewDocName();
    }

    public void setTitle(String docText) {
        if (stageView == StageView.MAIN_MENU || stageView == StageView.MAIN_NEWDOC) {
            mainFrame.setTitle(docText);
        } else {
            textAreaFrame.setTitle(docText);
        }
        update();
    }

    public void end() {
        mainFrame.dispose();
        textAreaFrame.dispose();
    }

    public enum StageView {
        MAIN_MENU, MAIN_NEWDOC, TEXTAREA
    }


    public MyView(MyModel myModel) {
        model = myModel;

        mainFrame = new MainFrame(model);
        textAreaFrame = new TextAreaFrame(model);
        stageView = StageView.MAIN_MENU;

    }


    public void update() {
        if (stageView == StageView.MAIN_MENU || stageView == StageView.MAIN_NEWDOC) {
            mainFrame.update();
        } else {
            textAreaFrame.update();
        }
    }

    public void setStageView(StageView st) {
        stageView = st;
        if (stageView == StageView.MAIN_MENU) {
            mainFrame.setVisible(true);
            textAreaFrame.setVisible(false);
            mainFrame.setStage(MainFrame.StageMenu.MENU);
        } else if (stageView == StageView.MAIN_NEWDOC) {
            mainFrame.setVisible(true);
            textAreaFrame.setVisible(false);
            mainFrame.setStage(MainFrame.StageMenu.NEWDOC);
        } else {
            mainFrame.setVisible(false);
            textAreaFrame.setVisible(true);
        }
    }


    public void addBackButtonListener(BackButtonListener backButtonListener) {
        mainFrame.bBack.addActionListener(backButtonListener);
    }

    public void addButtonOkListener(ButtonOkListener buttonOkListener) {
        mainFrame.bOK.addActionListener(buttonOkListener);
    }

    public void addDocBoxItemListener(DocBoxItemListener docBoxItemListener) {
        mainFrame.docBox.addItemListener(docBoxItemListener);
    }

    public void addDocBoxPopUpMenuListener(DocBoxPopUpMenuListener docBoxPopUpMenuListener) {
        mainFrame.docBox.addPopupMenuListener(docBoxPopUpMenuListener);
    }

    public void addNewDocListener(NewDocListener newDocListener) {
        mainFrame.bNewDoc.addActionListener(newDocListener);
    }

    public void addTextAreaDocumentListenerListener(TextAreaDocumentListener textAreaDocumentListener) {
        textAreaFrame.textArea.getDocument().addDocumentListener(textAreaDocumentListener);
    }

    public void addTextAreaFrameListener(TextAreaFrameListener textAreaFrameListener) {
        textAreaFrame.addWindowListener(textAreaFrameListener);
    }

    public void addTextAreaUndoableEditListener(TextAreaUndoableEditListener textAreaUndoableEditListener) {
        textAreaFrame.textArea.getDocument().addUndoableEditListener(textAreaUndoableEditListener);
    }
}
