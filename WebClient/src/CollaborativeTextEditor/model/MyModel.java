package CollaborativeTextEditor.model;


import javax.swing.*;
import java.util.Vector;

public class MyModel extends JTextArea {
    Vector<String> docList = new Vector<>(); //TODO: change to HasheSet

    public MyModel(int rows, int columns) {
        super(rows, columns);
    }

    public void updateOptionMenu(String[] splitData) {
        for (String option : splitData) {
            if (!docList.contains(option))
                docList.add(option);
        }
    }

    public void updateTextArea(String[] info) {
        if (info[2].equals("")) {
            replaceRange("", Integer.parseInt(info[0]), Integer.parseInt(info[1]));
        } else {
            insert(info[2], Integer.parseInt(info[0]));
        }
    }

    public Vector<String> getDocList() {
        return docList;
    }
}
