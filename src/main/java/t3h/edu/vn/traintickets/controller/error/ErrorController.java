package t3h.edu.vn.traintickets.controller.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/403")
    public String accessDenied() {
        return "error";
    }
}
