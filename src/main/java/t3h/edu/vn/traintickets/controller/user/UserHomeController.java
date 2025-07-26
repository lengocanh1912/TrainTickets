package t3h.edu.vn.traintickets.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import t3h.edu.vn.traintickets.service.UserService;

@Controller
@RequestMapping( "/user")
public class UserHomeController {

    @Autowired
    UserService userService;

    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("menu", "support");
        return "user/support";
    }
    @GetMapping("/test")
    public String test(Model model) {
        return"/user/test";
    }

    @GetMapping("/test1")
    public String test1(Model model) {
        return"/user/test1";
    }

    @GetMapping("/test2")
    public String test2(Model model) {
        return"/user/test2";
    }
}
