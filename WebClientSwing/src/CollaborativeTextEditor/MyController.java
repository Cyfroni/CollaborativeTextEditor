package CollaborativeTextEditor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;


import static java.lang.StrictMath.pow;

public class MyController {
    Socket sock;
    PrintWriter out;
    BufferedReader in;
    JTextArea textArea;

    int currentCursor;
    int currentIndex[]= new int[2];

    int previousCursor;
    int previousIndex[]= new int[2];

    boolean removalFlag = false;

    boolean updateable = false;


    char[] buffer=new char[1024];
    int port;
    String mess;
    ServerSocket serverSocket;
    Socket clientSocket;
    StringBuffer sb = new StringBuffer();
    JFrame frame;
    JButton b1;
    //JButton b;
    //JButton b1;

    public void menu(){
        String[] docList = { "" };
        frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        JComboBox<String> docBox = new JComboBox<>(docList);
        docBox.addItem("test");
        //docBox.addActionListener(this);

        b1 = new JButton("Nowy dokument");
        b1.setVerticalTextPosition(AbstractButton.CENTER);
        b1.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        b1.setActionCommand("disable");
        b1.addActionListener(e -> newDocument());
        frame.getContentPane().add(docBox, BorderLayout.WEST);
        frame.getContentPane().add(b1, BorderLayout.EAST);

        frame.pack();
    }

    public void newDocument(){

        System.out.println("Nowy dokument");
    }
    public String[] toPlaceAt(String parse){
        String splitedText[] = parse.split(":", parse.indexOf(':'));
        String[] splitedIndexes = splitedText[0].split("\\.");
        int[] splited = new int[4];
        for (int i=0;i<4;i++){
            splited[i] = Integer.parseInt(splitedIndexes[i]);
        }
        for (int i=0;i<4;i+=2){
            try {
                splited[i] = textArea.getLineStartOffset(splited[i]-1) + splited[i+1];
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return new String[] {Integer.toString(splited[0]), Integer.toString(splited[2]), splitedText[1]};
    }

    public void textArea(String docName){
        frame = new JFrame(docName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        textArea = new JTextArea(50,50);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(updateable){

                    return;
                }
                removalFlag = false;
                previousCursor = textArea.getCaretPosition();
                try {
                    previousIndex[0] = textArea.getLineOfOffset(previousCursor);
                    previousIndex[1] = textArea.getLineStartOffset(previousIndex[0]);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                System.out.println("insertion"+Integer.toString(previousIndex[0]+1)+Integer.toString(previousCursor-previousIndex[1]));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if(updateable){

                    return;
                }
                removalFlag = true;
                System.out.println("removal");
                previousCursor = textArea.getCaretPosition();
                try {
                    previousIndex[0] = textArea.getLineOfOffset(previousCursor);
                    previousIndex[1] = textArea.getLineStartOffset(previousIndex[0]);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                if(updateable){

                    return;
                }
                System.out.println("Undoable");
                String changeText="";
                UndoableEdit edit = e.getEdit();
                AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
                int offset = event.getOffset();
                int lenght = event.getLength();
                try {
                    changeText = event.getDocument().getText(offset,lenght);
//                    System.out.println(Arrays.toString(event.getDocument().getRootElements());

                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }

                currentCursor = textArea.getCaretPosition();
                try {
                    currentIndex[0] = textArea.getLineOfOffset(currentCursor);
                    currentIndex[1] = textArea.getLineStartOffset(currentIndex[0]);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                updateable = true;
                e.getEdit().undo();
                textArea.setCaretPosition(currentCursor);
                updateable = false;
                if(!removalFlag){
                    String msgInsert = ("Z"+(previousIndex[0]+1)+'.'+(previousCursor-previousIndex[1])+'.'+(currentIndex[0]+1)+'.'+(currentCursor-currentIndex[1])+':'+changeText);
                    out.print(msgInsert);
//                    System.out.println(msgInsert);
                } else{
                    String msgDelete = ("Z"+(currentIndex[0]+1)+'.'+(currentCursor-currentIndex[1])+'.'+(previousIndex[0]+1)+'.'+(previousCursor-previousIndex[1])+':'+changeText);
                    out.print(msgDelete);
//                    System.out.println(msgDelete);
                }
                out.flush();


                //System.out.println(e.getEdit().toString());

                //System.out.println(msg);
                System.out.println("kappa");
                //System.out.println(currentIndex[0]+1);
//                System.out.println(currentCursor - currentIndex[1]);


            }
        });

        out.print('G'+docName);
        out.flush();

        try {
            int byteNum = in.read(buffer);
            StringBuilder sb = new StringBuilder(new String(buffer,0,byteNum));
            sb.deleteCharAt(byteNum-1); /* adjusting message by deleting additional chars */
            sb.deleteCharAt(byteNum-2);
            String docText = sb.toString();
            updateable = true;
            textArea.append(docText);
            updateable = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        System.out.println(currentIndex[0]);
        System.out.println(currentIndex[1]);
        System.out.println(currentCursor);
        */
        frame.getContentPane().add(textArea, BorderLayout.CENTER);
        frame.pack();
    }


    MyController(){
        super();
        init();
        //menu();
        textArea("test.txt");
        System.out.println("siema");
    }
    public void init() {
        try {
            sock = new Socket("127.0.0.1", 8181);
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            System.out.println(in.read(buffer));

            port= (int)(buffer[0] + (buffer[1] * pow(2, 8)));
            System.out.println(port);
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();

            Thread thread = new Thread("clientSocket"){
                public void run(){
                    BufferedReader infrom = null;
                    try {
                        infrom = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    char[] thbuffer=new char[1024];
                    while(true) {
                        System.out.println("czeka");
                        try {
                            int byteNum = infrom.read(thbuffer);
                            StringBuilder sb = new StringBuilder(new String(thbuffer,0,byteNum));
                            String toUpdate = sb.toString();
                            System.out.println(toUpdate);
                            String[] info = toPlaceAt(toUpdate);
                            updateable = true;
                            if (info[2].equals("")){
                                textArea.replaceRange("",Integer.parseInt(info[0]),Integer.parseInt(info[1]));
                            }
                            else{
                                textArea.insert(info[2],Integer.parseInt(info[0]));

                            }
                            updateable = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
