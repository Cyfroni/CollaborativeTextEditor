package CollaborativeTextEditor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoableEdit;
import javax.xml.soap.Text;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


import static java.lang.StrictMath.pow;

public class MyController {
    Socket sock;
    PrintWriter out;
    BufferedReader in;

    int currentCursor;
    int currentIndex[]= new int[2];

    int previousCursor;
    int previousIndex[]= new int[2];

    boolean removalFlag = false;

    boolean updateable = false;

    int insertChange = 0;

    char[] buffer=new char[1024];
    char[] buf=new char[1024];
    int port;
    ServerSocket serverSocket;
    Socket clientSocket;
    StringBuffer sb = new StringBuffer();
    JTextArea textArea = new JTextArea(50,50);
    Vector<String> docList=new Vector<>();
    JFrame Mainframe = new JFrame("FrameDemo");
    JComboBox<String> docBox = new JComboBox<>(docList);
    JButton bNewDoc = new JButton("Nowy dokument");
    JButton bBack = new JButton("<-");
    JButton bOK = new JButton("OK");
    JTextField newdoc=new JTextField("");
    JFrame TextAreaframe = new JFrame();
    String[] info = new String[]{"0","0",""};

    public void menu(){
        System.out.println("menu");
        Mainframe.getContentPane().removeAll();

        Mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Mainframe.setVisible(true);
        Mainframe.add(docBox, BorderLayout.WEST);
        Mainframe.add(bNewDoc, BorderLayout.CENTER);

        Mainframe.revalidate();
        Mainframe.repaint();
        Mainframe.pack();
    }

    public void sendMessage(){

        String name=newdoc.getText();
        if (name.length() == 0)
            return;

        out.print("N" +name);
        out.flush();

        //menu();
    }
    public void newDocument(){
        System.out.println("newDocument");
        Mainframe.getContentPane().removeAll();

        Mainframe.setVisible(true);

        Mainframe.add(newdoc, BorderLayout.CENTER);
        Mainframe.add(bBack, BorderLayout.WEST);
        Mainframe.add(bOK, BorderLayout.EAST);
        Mainframe.repaint();
        Mainframe.pack();

    }


    public void updateOptionMenu()  {
        System.out.println("updateOptionMenu");
        out.print("UP");
        out.flush();

        try {
            int read = in.read(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputdata = new String(buf);
        String[] SplitData = inputdata.split("\n");


        for (String option : SplitData) {
            if (!docList.contains(option))
                docList.add(option);


        }
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
                splited[i] = textArea.getDocument().getLength()+1;
                e.printStackTrace();
            }
        }
        return new String[] {Integer.toString(splited[0]), Integer.toString(splited[2]), splitedText[1]};
    }

    public void textArea(String docName){
        Mainframe.setVisible(false);
        TextAreaframe.setVisible(true);

        TextAreaframe.setTitle(docName);
        TextAreaframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        TextAreaframe.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                out.print("UG");
                out.flush();
                TextAreaframe.setVisible(false);
                Mainframe.setVisible(true);
                textArea.removeAll();

            }
        });


        //TextAreaframe.setVisible(true);


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
                    if(info[2].equals(""))
                    {
                        textArea.setCaretPosition(Integer.parseInt(info[0]));
                    } else {
                        textArea.setCaretPosition(Integer.parseInt(info[1]));
                    }
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
                //textArea.setCaretPosition(currentCursor);
                updateable = false;
                if(!removalFlag){
                    String msgInsert = ("Z"+(previousIndex[0]+1)+'.'+(previousCursor-previousIndex[1])+'.'+(currentIndex[0]+1)+'.'+(currentCursor-currentIndex[1])+':'+changeText);
                    out.print(msgInsert);
//                    System.out.println(msgInsert);
                } else{
                    String msgDelete = ("Z"+(currentIndex[0]+1)+'.'+(currentCursor-currentIndex[1])+'.'+(previousIndex[0]+1)+'.'+(previousCursor-previousIndex[1])+':');
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
        TextAreaframe.getContentPane().add(textArea, BorderLayout.CENTER);
        TextAreaframe.pack();
    }


    MyController(){
        super();
        init();
        menu();
        System.out.println("siema");
    }
    public void init() {
        Mainframe.setSize(350, 64);
        bNewDoc.setVerticalTextPosition(AbstractButton.CENTER);
        bNewDoc.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        //bNewDoc.setActionCommand("disable");
        bNewDoc.addActionListener(d -> newDocument());

        bBack.setVerticalTextPosition(AbstractButton.CENTER);
        bBack.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        //bBack.setActionCommand("disable");
        bBack.addActionListener(e ->menu());

        bOK.setVerticalTextPosition(AbstractButton.CENTER);
        bOK.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        // bOK.setActionCommand("disable");
        bOK.addActionListener(f ->sendMessage());

        //docBox.addActionListener(dB ->updateOptionMenu());
        docBox.setSize(200,64);
        docBox.addItemListener(e -> {

            textArea(e.getItem().toString());
        });
        docBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                updateOptionMenu();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });




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
                            info = toPlaceAt(toUpdate);
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
