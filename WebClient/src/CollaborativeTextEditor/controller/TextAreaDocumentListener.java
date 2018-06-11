package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextAreaDocumentListener implements DocumentListener {

    private MyController controller;
    private MyView view;
    private MyModel model;

    TextAreaDocumentListener(MyController myController) {
        super();
        controller = myController;
        view = myController.view;
        model = myController.model;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        controller.change(false);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        controller.change(true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

}
