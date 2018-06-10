package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TextAreaFrameListener extends WindowAdapter {
    private MyController controller;
    private MyView view;
    private MyModel model;

    TextAreaFrameListener(MyController myController) {
        super();
        controller = myController;
        view = myController.view;
        model = myController.model;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        controller.mySendMessage("UG");
        view.setStageView(MyView.StageView.MAIN_MENU);
    }
}
