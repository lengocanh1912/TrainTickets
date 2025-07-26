package t3h.edu.vn.traintickets.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import t3h.edu.vn.traintickets.dto.ReviewDisplayDto;
import t3h.edu.vn.traintickets.entities.Review;
import t3h.edu.vn.traintickets.service.ReviewService;

import java.util.List;

@Controller
@RequestMapping("/admin/review")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/view")
    public String viewReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllDisplayReviews());
        model.addAttribute("menu", "review"); // để active menu nếu dùng classappend
        return "admin/review/view"; // đường dẫn đến file Thymeleaf template
    }
    @GetMapping("/list")
    public String getReviewList(Model model) {
        List<ReviewDisplayDto> reviews = reviewService.getAllDisplayReviews(); // 👈 Gọi từ service, không gọi trực tiếp repo
        model.addAttribute("reviews", reviews);
        return "user/review_list"; // Tên template HTML
    }

    @GetMapping("/search")
    public String searchReviews(@RequestParam String keyword, Model model) {
        List<Review> reviews = reviewService.searchReviewsByUser(keyword);
        model.addAttribute("reviews", reviews);
        return "admin/review/list";
    }

}

