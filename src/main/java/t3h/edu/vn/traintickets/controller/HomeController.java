package t3h.edu.vn.traintickets.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
//import ch.qos.logback.core.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.config.UserDetailServiceImpl;
import t3h.edu.vn.traintickets.dto.UserCreateDto;
import t3h.edu.vn.traintickets.dto.UserPasswordDto;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.TicketRepository;
import t3h.edu.vn.traintickets.repository.TrainRepository;
import t3h.edu.vn.traintickets.repository.UserRepository;
import t3h.edu.vn.traintickets.service.*;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainService trainService;
    @Autowired
    private TrainRepository trainRepository;
//
    @Autowired
    private TicketService ticketService;
//
//    @Autowired
//    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReviewService reviewService;
//    @Autowired
//    private RouteService routeService;
//
//    @Autowired
//    private SeatService seatService;
//
//    @Autowired
//    private DiscountService discountService;

    @GetMapping("/admin/home")
    public String showDashboard( Model model) {
        model.addAttribute("averageRating", reviewService.getAverageRating());
        model.addAttribute("reviewCount", reviewService.getTotalReviewCount());
        model.addAttribute("menu", "dashboard");
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("trainCount", trainRepository.count());
        model.addAttribute("ticketSold", ticketRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
//        model.addAttribute("routeCount", routeService.count());
//        model.addAttribute("availableSeats", seatService.countAvailableSeats());
//        model.addAttribute("discountCount", discountService.count());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());

        return "/admin/home"; // trỏ tới file admin_home.jsp hoặc admin_home.html
    }

    @GetMapping("/user/home")
    public String userHome(Model model) {
        model.addAttribute("menu", "userHome");
        model.addAttribute("averageRating", reviewService.getAverageRating());
        model.addAttribute("reviewCount", reviewService.getTotalReviewCount());
        return "user/home";
    }

    @Autowired
    private UserService userService;

    @GetMapping("/admin/password")
    public String password(HttpSession session, Model model) {
        UserPasswordDto user = (UserPasswordDto) session.getAttribute("user");
        model.addAttribute("user", user);
        return "admin/password";
    }

    @PostMapping("/admin/password")
    public String passwordSave(@RequestParam("newPassword") String newPassword,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        userService.updatePassword(user.getId(), newPassword); // custom method đổi mật khẩu

        redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công");
        return "redirect:/admin/user/view";
    }



}
