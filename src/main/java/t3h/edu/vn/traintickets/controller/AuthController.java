package t3h.edu.vn.traintickets.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
import t3h.edu.vn.traintickets.security.UserDetailsImpl;
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
    public String getLoginPage(HttpSession session, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AnonymousAuthenticationToken) {
            return "redirect:/user/home";
        }
        Boolean loginRequired =
                (Boolean) session.getAttribute("LOGIN_REQUIRED");

        if (Boolean.TRUE.equals(loginRequired)) {
            model.addAttribute("loginRequired", true);
            session.removeAttribute("LOGIN_REQUIRED"); // ❗ xoá ngay
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

            UserDetailsImpl userDetails = new UserDetailsImpl(user);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getPassword(),
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            ra.addFlashAttribute("message",
                    "Đăng ký và đăng nhập thành công!");
            return "redirect:/user/home";
        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/register";
        }
    }




}
