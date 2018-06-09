package CollaborativeTextEditor;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.StrictMath.pow;

public class MyController {
    Socket sock;
    PrintWriter out;
    byte[] buffer=new byte[1024];
    int port;
    String mess;
    ServerSocket serverSocket;
    Socket clientSocket;
    StringBuffer sb = new StringBuffer();
    JFrame frame;
    JButton b1;

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



    MyController(){
        super();
        init();
        menu();
        System.out.println("siema");
    }
    public void init() {
        try {
            sock = new Socket("127.0.0.1", 8080);
            out = new PrintWriter(sock.getOutputStream(), true);
            DataInputStream in = new DataInputStream(sock.getInputStream());
            System.out.println(in.read(buffer));
            port= (int)(buffer[0] + (buffer[1] * pow(2, 8)));
            System.out.println(port);
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            DataInputStream infrom = new DataInputStream(clientSocket.getInputStream());
            Thread thread = new Thread("clientSocket"){
                public void run(){
                    byte[] buffer1=new byte[1024];
                    while(true) {
                        System.out.println("czeka");
                        try {
                            infrom.read(buffer1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String s = new String(buffer1);
                        if (s.equals(""))
                            break;
                        System.out.println("#"+ s);
                        sb.append(s);
                    }
                }
            };
            thread.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
