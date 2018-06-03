package collaborativetexteditor.webclient.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AppController {


    @GetMapping(value = "/")
    public ResponseEntity<String> test() {
        return ResponseEntity.status(200).build();
    }


    @GetMapping(value = "/hello")
    public String helloWorld() {
        return "hello World";
    }

}
