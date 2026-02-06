package t3h.edu.vn.traintickets.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import t3h.edu.vn.traintickets.repository.*;
import t3h.edu.vn.traintickets.service.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrainRepository trainRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private RevenueService revenueService;
    @Autowired
    private RouteRepository routeRepository;

    @GetMapping("/admin/home")
    public String showDashboard( Model model) {
        model.addAttribute("averageRating", reviewService.getAverageRating());
        model.addAttribute("reviewCount", reviewService.getTotalReviewCount());
        model.addAttribute("menu", "dashboard");
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("trainCount", trainRepository.count());
        model.addAttribute("ticketSold", ticketRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("routeCount", routeRepository.count());
//        model.addAttribute("availableSeats", seatService.countAvailableSeats());
//        model.addAttribute("discountCount", discountR.count());
        model.addAttribute("totalRevenue", revenueService.getTotalRevenueOrderPaid());

        return "/admin/home";
    }

    @GetMapping("/admin/home-test")
    public String adminHome(Model model){
        return  "admin/home-test";
    }

    @GetMapping("/user/home")
    public String userHome(Model model) {
        model.addAttribute("menu", "userHome");
        model.addAttribute("averageRating", reviewService.getAverageRating());
        model.addAttribute("reviewCount", reviewService.getTotalReviewCount());
        return "user/home";
    }


}
