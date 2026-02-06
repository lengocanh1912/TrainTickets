package t3h.edu.vn.traintickets.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import org.springframework.web.multipart.MultipartFile;
import t3h.edu.vn.traintickets.dto.UserUpdateDto;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.service.UserService;

@Controller
@RequestMapping("/user/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    /**
     * TAB: Thông tin cá nhân
     * URL: /trainticket/user/account
     * URL: /trainticket/user/account/profile
     */
    @GetMapping({"", "/profile"})
    public String profile(Model model, Principal principal) throws Exception {
        User user = userService.findByUserName(principal.getName());
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "profile");
        model.addAttribute("contentTemplate", "user/account/account-profile");

        return "user/account/account";
    }
    /**
     * TAB: Đơn hàng
     * URL: /trainticket/user/account/orders
     */
    @GetMapping("/orders")
    public String orders(Model model, Principal principal) throws Exception {

        User user = userService.findByUserName(principal.getName());

        model.addAttribute("user", user);
        model.addAttribute("activeTab", "orders");
        model.addAttribute("contentTemplate", "user/account/account-orders");

        return "user/account/account";
    }

    /**
     * TAB: Thanh toán
     * URL: /trainticket/user/account/payment
     */
    @GetMapping("/payment")
    public String payment(Model model, Principal principal) throws Exception {

        User user = userService.findByUserName(principal.getName());

        model.addAttribute("user", user);
        model.addAttribute("activeTab", "payment");
        model.addAttribute("contentTemplate", "user/account/account-payment");

        return "user/account/account";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            Principal principal
    ) throws Exception {

        User user = userService.findByUserName(principal.getName());
        userService.updateAvatar(user, file);

        return "redirect:/user/account";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
                            UserUpdateDto dto,
                            Principal principal
                                ) throws Exception {

        User currentUser = userService.findByUserName(principal.getName());

        dto.setId(currentUser.getId());

        userService.updateProfile(dto);

        return "redirect:/user/account";
    }

    @GetMapping("/password")
    public String changePasswordPage(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("activeTab", "password");
        model.addAttribute("contentTemplate", "user/account/account-password");

        return "user/account/account";
    }

    @PostMapping("/password/update")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal
    ) {

        if (principal == null) {
            return "redirect:/login";
        }

        userService.changePassword(
                principal.getName(),
                currentPassword,
                newPassword,
                confirmPassword
        );

        return "redirect:/user/account?passwordChanged";
    }

    @GetMapping("/email")
    public String changEmail(Model model, Principal principal) throws Exception {
        User user = userService.findByUserName(principal.getName());
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "email");
        model.addAttribute("contentTemplate", "user/account/account-email");

        return "user/account/account";
    }

    @PostMapping("/email/verify/request")
    @ResponseBody
    public String requestVerifyCurrentEmail(
            @RequestParam String currentEmail,
            Principal principal
    ) {
        userService.sendOtpToCurrentEmail(principal.getName(), currentEmail);
        return "OTP đã được gửi về email hiện tại";
    }

    @PostMapping("/email/verify/confirm")
    @ResponseBody
    public String confirmChangeEmail(
            @RequestParam String otp,
            Principal principal
    ) {
        userService.confirmEmailOTP(principal.getName(), otp);
        return "Đổi email thành công";
    }

}
