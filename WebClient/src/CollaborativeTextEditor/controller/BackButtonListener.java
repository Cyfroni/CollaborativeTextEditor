package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class BackButtonListener implements ActionListener {
    private MyController controller;
    private MyView view;
    private MyModel model;

    BackButtonListener(MyController myController) {
        super();
        controller = myController;
        view = myController.view;
        model = myController.model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        view.setStageView(MyView.StageView.MAIN_MENU);
    }
}
