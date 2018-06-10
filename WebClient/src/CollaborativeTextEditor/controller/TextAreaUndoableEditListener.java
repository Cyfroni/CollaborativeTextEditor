package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoableEdit;

public class TextAreaUndoableEditListener implements UndoableEditListener {
    private MyController controller;
    private MyView view;
    private MyModel model;

    TextAreaUndoableEditListener(MyController myController) {
        super();
        controller = myController;
        view = myController.view;
        model = myController.model;
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {

        if (controller.updateable) {
            if (controller.info[2].equals("")) {
                model.setCaretPosition(Integer.parseInt(controller.info[0]));
            } else {
                model.setCaretPosition(Integer.parseInt(controller.info[1]));
            }
            return;
        }

        System.out.println("Undoable");
        String changeText = "";
        UndoableEdit edit = e.getEdit();
        AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
        int offset = event.getOffset();
        int lenght = event.getLength();
        try {
            changeText = event.getDocument().getText(offset, lenght);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        controller.currentCursor = model.getCaretPosition();
        try {
            controller.currentIndex[0] = model.getLineOfOffset(controller.currentCursor);
            controller.currentIndex[1] = model.getLineStartOffset(controller.currentIndex[0]);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        controller.updateable = true;
        e.getEdit().undo();
        controller.updateable = false;
        String msg;
        if (!controller.removalFlag) {
            msg = ("Z" + (controller.previousIndex[0] + 1) + '.' + (controller.previousCursor - controller.previousIndex[1]) + '.' + (controller.currentIndex[0] + 1) + '.' + (controller.currentCursor - controller.currentIndex[1]) + ':' + changeText);
        } else {
            msg = ("Z" + (controller.currentIndex[0] + 1) + '.' + (controller.currentCursor - controller.currentIndex[1]) + '.' + (controller.previousIndex[0] + 1) + '.' + (controller.previousCursor - controller.previousIndex[1]) + ':');
        }
        controller.mySendMessage(msg);

        System.out.println("kappa");


    }

}
