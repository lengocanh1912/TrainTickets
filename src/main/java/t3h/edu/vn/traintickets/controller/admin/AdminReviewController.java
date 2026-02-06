package t3h.edu.vn.traintickets.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.ReviewDisplayDto;
import t3h.edu.vn.traintickets.entities.ReviewReply;
import t3h.edu.vn.traintickets.enums.ReviewStatus;
import t3h.edu.vn.traintickets.repository.ReviewReplyRepository;
import t3h.edu.vn.traintickets.repository.ReviewRepository;
import t3h.edu.vn.traintickets.service.ReviewReplyService;
import t3h.edu.vn.traintickets.service.ReviewService;

import java.util.List;

@Controller
@RequestMapping("/admin/review")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewReplyRepository reviewReplyRepository;
    @Autowired
    private ReviewReplyService reviewReplyService;


    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "review");
    }

    //    @GetMapping("/view")
//    public String viewReviews(Model model) {
//        model.addAttribute("reviews", reviewService.getAllDisplayReviews());
//        model.addAttribute("menu", "review"); // để active menu nếu dùng classappend
//        return "admin/review/view"; // đường dẫn đến file Thymeleaf template
//    }
    @GetMapping("/list")
    @ResponseBody
    public Page<ReviewDisplayDto> list(@RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "4") Integer perpage) {
        return reviewService.getAllDisplayReviews(page, perpage);
    }

    @GetMapping("/view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "4") Integer perpage) {
        Page<ReviewDisplayDto> paged = reviewService.getAllDisplayReviews(page, perpage);
        model.addAttribute("page", paged);
        model.addAttribute("path", "/admin/review/view");
        model.addAttribute("ReviewStatus", ReviewStatus.class);
        return "admin/review/view";
    }

    @GetMapping("/search")
    public String searchReviews(@RequestParam String keyword, Model model) {
        List<ReviewDisplayDto> reviews = reviewService.searchReviewsByUser(keyword);
        model.addAttribute("reviews", reviews);
        return "admin/review/search";
    }

    @GetMapping("/reply/{id}")
    public String showReplyForm(@PathVariable Long id, Model model) {
        ReviewDisplayDto reviewDto = reviewService.findByReviewId(id);

        ReviewReply reply = reviewReplyRepository.findByReviewId(id).orElse(null);

        model.addAttribute("review", reviewDto);
        model.addAttribute("reply", reply != null ? reply : new ReviewReply());
        model.addAttribute("ReviewStatus",ReviewStatus.class);

        return "admin/review/reply-form";
    }

    @PostMapping("/reply/save")
    public String saveReply(@RequestParam Long reviewId,
                            @RequestParam String content,
                            RedirectAttributes redirectAttributes) {
        reviewReplyService.saveOrUpdateReply(reviewId, content);
        redirectAttributes.addFlashAttribute("message", "Đã gửi phản hồi!");
        return "redirect:/admin/review/view";
    }


}

