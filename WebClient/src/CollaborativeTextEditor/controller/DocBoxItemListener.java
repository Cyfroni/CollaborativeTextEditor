package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class DocBoxItemListener implements ItemListener {
    private MyController controller;
    private MyView view;
    private MyModel model;

    DocBoxItemListener(MyController myController) {
        super();
        controller = myController;
        view = myController.view;
        model = myController.model;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        String docName = e.getItem().toString();
        controller.mySendMessage("G" + docName);
        String docText = controller.myReceiveMessage(controller.in, controller.buffer, 2);

        String[] info = {"0", "0", docText};
        controller.updateable = true;
        model.removeAll();
        model.updateTextArea(info);
        controller.updateable = false;
        view.setTitle(docText);
        view.setStageView(MyView.StageView.TEXTAREA);
    }
}
