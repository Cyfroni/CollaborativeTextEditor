package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import javax.swing.text.BadLocationException;
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
        if (e.getStateChange() == ItemEvent.DESELECTED || ((String) e.getItem()).equals("* None *")){
            return;
        }
        String docName = e.getItem().toString();
        controller.mySendMessage("G" + docName);
        String docText = controller.myReceiveMessage(controller.in, controller.buffer, 2);

        controller.info = new String[]{"0", "0", docText};
        controller.updateable = true;
//        model.removeAll();
        model.setText("");
        model.updateTextArea(controller.info);
        controller.updateable = false;
        view.setTitle(docName);
        view.setStageView(MyView.StageView.TEXTAREA);
    }
}
