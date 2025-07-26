package t3h.edu.vn.traintickets.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import t3h.edu.vn.traintickets.dto.TicketBookingRequest;
@Controller
public class UserTicketController {

//    @PostMapping("/book-ticket")
//    public String bookTicket(@ModelAttribute TicketBookingRequest request, Model model) {
//        // B1: Lấy thông tin người dùng hiện tại (nếu có login)
//        // B2: Kiểm tra các chỗ ngồi còn trống
//        // B3: Lưu vào DB: ticket -> order -> order_ticket
//        // B4: Đưa thông tin sang trang payment
//
//        // Ví dụ:
//        model.addAttribute("orderSummary", order); // truyền sang trang thanh toán
//        return "payment_page"; // ví dụ là payment_page.html
//    }

}
