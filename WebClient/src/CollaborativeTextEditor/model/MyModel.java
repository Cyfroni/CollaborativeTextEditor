package CollaborativeTextEditor.model;


import javax.swing.*;
import java.util.Arrays;
import java.util.Vector;

public class MyModel extends JTextArea {
    String defaultElem = "* None *";
    DefaultComboBoxModel<String> combo = new DefaultComboBoxModel<>();

    public MyModel(int rows, int columns) {
        super(rows, columns);
        combo.addElement(defaultElem);
    }

    public void updateOptionMenu(String[] splitData) {
        System.out.println(Arrays.toString(splitData));
        combo.removeAllElements();
        combo.addElement(defaultElem);
        for (String item : splitData){
            combo.addElement(item);

        }
    }

    public void updateTextArea(String[] info) {
        if (info[2].equals("")) {
            replaceRange("", Integer.parseInt(info[0]), Integer.parseInt(info[1]));
        } else {
            insert(info[2], Integer.parseInt(info[0]));
        }
    }

    public DefaultComboBoxModel<String> getDocList() {
        return combo;
    }
}
