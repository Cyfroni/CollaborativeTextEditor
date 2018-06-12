package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import javax.swing.text.BadLocationException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static java.lang.Thread.sleep;

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

        String docText;
        boolean cond = true;
        controller.updateable = true;
        model.setText("");
        do {
            docText = controller.myReceiveMessage(controller.in, controller.buffer, 0);
            if (docText.endsWith("\0")){
                cond = false;
                docText = docText.substring(0, docText.length() - 1);
            }
            model.append(docText);

        }while (cond);

        controller.updateable = false;
        view.setTitle(docName);
        view.setStageView(MyView.StageView.TEXTAREA);
    }
}
