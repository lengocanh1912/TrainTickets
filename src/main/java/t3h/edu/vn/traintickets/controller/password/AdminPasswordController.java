package t3h.edu.vn.traintickets.controller.password;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.UserPasswordDto;
import t3h.edu.vn.traintickets.service.UserService;

import java.security.Principal;

@Controller
public class AdminPasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin/updatePassword")
    public String password(HttpSession session, Model model) {
        model.addAttribute("user", new UserPasswordDto()); // Dùng DTO cho form
        return "admin/updatePassword";
    }

    @PostMapping("/admin/updatePassword")
    public String updatePassword(
            @Valid @ModelAttribute("userpassword") UserPasswordDto dto,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "admin/update-password";
        }

        try {
            userService.changePasswordAdmin(principal.getName(), dto);
            redirect.addFlashAttribute("message", "Đổi mật khẩu thành công");
            return "redirect:/admin/user/view";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/updatePassword";
        }
    }
}
