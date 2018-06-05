package collaborativetexteditor.webclient.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.sql.SQLOutput;

import static java.lang.StrictMath.pow;


@RestController
public class AppController {

    Socket sock;
    PrintWriter out;
    byte[] buffer=new byte[1024];
    int port;
    String mess;
    ServerSocket serverSocket;
    Socket clientSocket;
    StringBuffer sb = new StringBuffer();


    AppController(){
        super();
        init();
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





    /*@GetMapping(value = "/")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(200).build();
    }*/






}
