package CollaborativeTextEditor.controller;

import CollaborativeTextEditor.model.MyModel;
import CollaborativeTextEditor.view.MyView;

import javax.swing.text.BadLocationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import static java.lang.StrictMath.pow;

public class MyController {
    MyView view;
    MyModel model;

    PrintWriter out;
    BufferedReader in;

    int currentCursor;
    int currentIndex[] = new int[2];

    int previousCursor;
    int previousIndex[] = new int[2];

    boolean removalFlag = false;

    boolean updateable = false;

    char[] buffer = new char[1024];

    String[] info = new String[]{"0", "0", ""};


    public boolean mySendMessage(String message) {
        if (message.length() == 0)
            return false;
        out.print(message);
        out.flush();
        System.out.println("SEND:" + message + "#");
        return true;
    }

    public String myReceiveMessage(BufferedReader reader, char[] b, int cut) {
//        char[] b = new char[1024];
        int amount = 0;
        try {
            amount = reader.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        amount -= cut;
        amount = amount < 0 ? 0 : amount;
        String msg = new String(b, 0, amount);
        System.out.println("RECEIVE:" + msg + "#");
        return msg;
    }

    public void change(boolean remove) {
        if (updateable) {
            return;
        }
        removalFlag = remove;
        previousCursor = model.getCaretPosition();

        try {
            previousIndex[0] = model.getLineOfOffset(previousCursor);
            previousIndex[1] = model.getLineStartOffset(previousIndex[0]);

        } catch (BadLocationException ex) {
            try {
                previousIndex[0] = model.getLineOfOffset(previousCursor-1);
                previousIndex[1] = model.getLineStartOffset(previousIndex[0]);
                System.out.println("Poczatekpliku");
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

        }
        System.out.println(previousCursor);
        System.out.println("previusCursor");
        System.out.println(previousIndex[0]);
        System.out.println("prevIndex[0]");
        System.out.println(previousIndex[1]);
        System.out.println("prevIndex[1]");


        System.out.println("insertion" + Integer.toString(previousIndex[0] + 1) + Integer.toString(previousCursor - previousIndex[1]));
    }


    public String[] toPlaceAt(String parse) {
        String splitedText[] = parse.split(":", parse.indexOf(':'));
        String[] splitedIndexes = splitedText[0].split("\\.");
        int[] splited = new int[4];
        System.out.println(Arrays.toString(splitedIndexes));
        for (int i = 0; i < 4; i++) {
            splited[i] = Integer.parseInt(splitedIndexes[i]);
        }
        for (int i = 0; i < 4; i += 2) {
            try {
                splited[i] = model.getLineStartOffset(splited[i] - 1) + splited[i + 1];
            } catch (BadLocationException e) {
                splited[i] = model.getDocument().getLength() + 1;
                e.printStackTrace();
            }
        }
        return new String[]{Integer.toString(splited[0]), Integer.toString(splited[2]), splitedText[1]};
    }


    public MyController() {
        super();
        init();
        model = new MyModel(50, 50);
        view = new MyView(model);
        view.addBackButtonListener(new BackButtonListener(this));
        view.addButtonOkListener(new ButtonOkListener(this));
        view.addDocBoxItemListener(new DocBoxItemListener(this));
        view.addDocBoxPopUpMenuListener(new DocBoxPopUpMenuListener(this));
        view.addNewDocListener(new NewDocListener(this));
        view.addTextAreaDocumentListenerListener(new TextAreaDocumentListener(this));
        view.addTextAreaFrameListener(new TextAreaFrameListener(this));
        view.addTextAreaUndoableEditListener(new TextAreaUndoableEditListener(this));
        view.setStageView(MyView.StageView.MAIN_MENU);
        System.out.println("siema");
    }

    public void init() {
        try {
            Socket sock = new Socket("127.0.0.1", 8181);
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            System.out.println(in.read(buffer));

            int port = (int) (buffer[0] + (buffer[1] * pow(2, 8)));
            System.out.println(port);
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();

            Thread thread = new Thread("clientSocket") {
                public void run() {
                    BufferedReader infrom = null;
                    try {
                        infrom = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    char[] thbuffer = new char[1024];
                    while (true) {
                        System.out.println("czeka");
                        String toUpdate = myReceiveMessage(infrom, thbuffer, 0);

                        System.out.println(toUpdate);
                        info = toPlaceAt(toUpdate);
                        updateable = true;
                        model.updateTextArea(info);
                        updateable = false;
                    }
                }
            };
            thread.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
