package t3h.edu.vn.traintickets.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import t3h.edu.vn.traintickets.dto.ReviewDisplayDto;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.entities.Review;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.ReviewStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.ReviewRepository;
import t3h.edu.vn.traintickets.service.ReviewService;
import t3h.edu.vn.traintickets.service.UserService;

import java.security.Principal;
import java.util.List;

@RequestMapping("/user/review")
@Controller
public class UserReviewController {
    @Autowired
    ReviewService reviewService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "review");

    }

//    @GetMapping("/list")
//    public String getReviewList(Model model) {
//        List<ReviewDisplayDto> reviews = reviewService.getAllDisplayReviews(); // 👈 Gọi từ service, không gọi trực tiếp repo
//        model.addAttribute("reviews", reviews);
//        return "user/review_list"; // Tên template HTML
//    }

    @GetMapping("/list")
    @ResponseBody
    public Page<ReviewDisplayDto> list(@RequestParam(defaultValue = "0") Integer page,
                                       @RequestParam(defaultValue = "2") Integer perpage) {
        return reviewService.getAllDisplayReviews(page, perpage);
    }

    @GetMapping("/view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "2") Integer perpage) {
        Page<ReviewDisplayDto> paged = reviewService.getAllDisplayReviews(page, perpage);
        model.addAttribute("page", paged);
        model.addAttribute("path", "/user/review/view");
        model.addAttribute("ReviewStatus", ReviewStatus.class);
        return "user/review_list";
    }


    @GetMapping("/rate")
    public String showReviewForm(@RequestParam("id") Long orderId,
                                 Model model,
                                 Principal principal,
                                 HttpServletRequest request) {
        // Lấy order và kiểm tra quyền truy cập
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Optional: Kiểm tra order này có thuộc về user hiện tại không
//        String username = principal.getName();
//        if (!order.getUser().getUsername().equals(username)) {
//            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng này");
//        }

        // Optional: chỉ cho phép đánh giá khi đơn đã hoàn thành
        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Chỉ có thể đánh giá các chuyến đi đã hoàn tất");
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("order", order); // nếu bạn cần hiển thị thêm thông tin chuyến

        return "user/review_rate"; // Thymeleaf file để render form đánh giá
    }

    @PostMapping("/submit")
    public String submitReview(
            @RequestParam Long orderId,
            @RequestParam int rating,
            @RequestParam String content,
            @RequestParam("images") List<MultipartFile> images,
            Principal principal
    ) {
        reviewService.saveReview(orderId, rating, content, images, principal.getName());
        return "redirect:/user/home";
    }

}
