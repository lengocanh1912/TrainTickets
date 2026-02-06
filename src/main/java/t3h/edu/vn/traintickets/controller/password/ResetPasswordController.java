package t3h.edu.vn.traintickets.controller.password;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.repository.UserRepository;
import t3h.edu.vn.traintickets.service.UserService;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class ResetPasswordController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/reset-password")
    public String showResetPassword(
            @RequestParam String token,
            Model model
    ) {
        User user = userRepository.findByResetToken(token);

        if (user == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
            return "user/password/forgot-password";
        }

        model.addAttribute("token", token);
        model.addAttribute("expiryTime", user.getResetTokenExpiry().toEpochMilli());

        return "user/password/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model
    ) {
        try {

            userService.resetPassword(token, newPassword, confirmPassword);

        } catch (IllegalArgumentException e) {
            // Token sai / hết hạn / password không khớp
            model.addAttribute("error", e.getMessage());

            User user = userRepository.findByResetToken(token);
            if (user != null && user.getResetTokenExpiry().isAfter(Instant.now())) {
                model.addAttribute("token", token);
                model.addAttribute(
                        "expiryTime",
                        user.getResetTokenExpiry().toEpochMilli()
                );
                return "user/password/reset-password";
            }

            return "user/password/forgot-password";
        }

        model.addAttribute("message", "Đổi mật khẩu thành công, vui lòng đăng nhập");
        return "login";
    }
}

