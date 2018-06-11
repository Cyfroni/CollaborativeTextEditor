package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.Vector;

public class DocBoxPopUpMenuListener implements PopupMenuListener {
    private MyController controller;
    private MyView view;
    private MyModel model;

    DocBoxPopUpMenuListener(MyController myController) {
        super();
        controller = myController;
        view = myController.view;
        model = myController.model;
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        System.out.println("updateOptionMenu");

        controller.mySendMessage("UP");
        String inputdata = controller.myReceiveMessage(controller.in, controller.buffer, 0);

        String[] SplitData = inputdata.split("\n");
        model.updateOptionMenu(SplitData);
        view.update();
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {

    }
}
