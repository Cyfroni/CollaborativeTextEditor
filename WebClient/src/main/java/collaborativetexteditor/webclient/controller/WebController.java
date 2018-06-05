package collaborativetexteditor.webclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
    @RequestMapping("/")
    public String index() {
        return "index.html";
    }
    @GetMapping("/open")
    public String open() {
        return "open.html";

    }
    @GetMapping("/create")
    public String create() {
        return "create.html";

    }
}
