package collaborativetexteditor.webclient.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.Socket;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.sql.SQLOutput;

import static java.lang.StrictMath.pow;


@RestController
public class AppController {

    Socket sock;
    PrintWriter out;
    //BufferedReader in;
    DataInputStream in;
    char c;
    byte[] buffer=new byte[1024];
    int x;
    AppController(){
        super();
        create();
        System.out.println("siema");
    }

    public void create() {
        try {
            sock = new Socket("127.0.0.1", 8080);
            out = new PrintWriter(sock.getOutputStream(), true);
            //in = sock.getInputStream();// new BufferedReader(new InputStreamReader(sock.getInputStream()),4);
            DataInputStream in = new DataInputStream(sock.getInputStream());
            System.out.println(in.available());
            System.out.println(in.read(buffer));
            System.out.println(in.available());
            System.out.println(buffer[0]);
            System.out.println(buffer[1]);
            System.out.println((buffer[0]+buffer[1]*pow(2,8)));


        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }





    /*@GetMapping(value = "/")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(200).build();
    }*/

    @GetMapping("/FrontEnd")
    public String helloWorld() {
        return "hello World";
    }



}
