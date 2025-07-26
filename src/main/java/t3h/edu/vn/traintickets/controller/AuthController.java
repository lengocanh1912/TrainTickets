package t3h.edu.vn.traintickets.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.config.UserDetailServiceImpl;
import t3h.edu.vn.traintickets.dto.UserCreateDto;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.repository.UserRepository;
import t3h.edu.vn.traintickets.service.UserService;
import org.springframework.ui.Model;

@Controller
@RequestMapping
public class AuthController extends GlobalModelAttribute {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserDetailServiceImpl userDetailsService;
    @GetMapping("/login")
    public String getLoginPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return "redirect:user/home";
        }
        return "/login";
    }
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserCreateDto());
        return "/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") UserCreateDto userCreateDto,
                                  RedirectAttributes ra,
                                  HttpServletRequest request) {
        try {
            userService.register(userCreateDto);

            User user = userRepository.findByUsername(userCreateDto.getUsername());
            UserDetails userDetails = new UserDetailServiceImpl.UserDetailImpl(user);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, userDetails.getPassword(), userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ Gán SecurityContext vào session để không bị mất sau redirect
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            ra.addFlashAttribute("message", "Đăng ký và đăng nhập thành công!");
            return "redirect:/user/home";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/register";
        }
    }

//    @PostMapping("/forgot-password")
//    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes ra) {
//        try {
//            userService.sendResetPasswordLink(email);
//            ra.addFlashAttribute("message", "Đã gửi liên kết đặt lại mật khẩu đến email.");
//        } catch (Exception e) {
//            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
//        }
//        return "redirect:/forgot-password";
//    }



}
