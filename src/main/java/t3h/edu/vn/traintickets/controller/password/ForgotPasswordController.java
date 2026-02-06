package t3h.edu.vn.traintickets.controller.password;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import t3h.edu.vn.traintickets.service.UserService;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final UserService userService;

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "user/password/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam String email,
            Model model
    ) {
        userService.sendResetPasswordLink(email);

        // Không lộ email tồn tại hay không
        model.addAttribute(
                "message",
                "Check email và làm theo các bước."
        );

        return "user/password/forgot-password";
    }
}

