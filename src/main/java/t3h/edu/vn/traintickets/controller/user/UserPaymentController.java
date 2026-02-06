package t3h.edu.vn.traintickets.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.OrderPaymentDto;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.TicketRepository;
import t3h.edu.vn.traintickets.service.MailService;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.TicketService;
import t3h.edu.vn.traintickets.service.VNPayService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/user/payment")
public class UserPaymentController {

    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private MailService mailService;

    @PostMapping
    public String createPayment(@RequestParam("orderId") Long orderId,
                                HttpServletRequest request) {
        System.out.println("✅ >>> Payment request received. orderId = " + orderId);
        Order order = orderService.findById(orderId);
        String paymentUrl = vnPayService.createVNPayPayment(order, request);

        return "redirect:" + paymentUrl;
    }

    @PostMapping("/payment")
    public String createPayment(@ModelAttribute OrderPaymentDto orderDto,
                                HttpServletRequest request) {

        System.out.println(">>> Received OrderId: " + orderDto.getOrderId());
        System.out.println(">>> ContactName: " + orderDto.getContactName());
        System.out.println(">>> Ticket[0] Name: " + orderDto.getTickets().get(0).getPassengerName());

        // Cập nhật lại order với dữ liệu từ form
        orderService.updateOrderContactInfo(orderDto);
        ticketService.updateTicketPassengerInfo(orderDto.getTickets());

        Order order = orderService.findById(orderDto.getOrderId());
        String paymentUrl = vnPayService.createVNPayPayment(order, request);

        return "redirect:" + paymentUrl;
    }

    @GetMapping("/return")
    public String handleVNPayReturn(HttpServletRequest request, Model model) {
        try {
            // 🔹 B1: Xử lý phản hồi từ VNPay
            int paymentStatus = vnPayService.processVNPayResponse(request);

            // 🔹 B2: Lấy mã giao dịch (vnp_TxnRef)
            String txnRef = request.getParameter("vnp_TxnRef");

            if (txnRef != null) {
                // ví dụ txnRef = "123_1730212345678"
                String[] parts = txnRef.split("_");
                Long orderId = Long.parseLong(parts[0]);

//                // 🔹 B3: Gọi service cập nhật trạng thái
                boolean success = (paymentStatus == 1);
                orderService.updateOrderAndTicketsStatus(orderId, success);

                // 🔹 Lấy lại order sau cập nhật
                Order order = orderRepository.findById(orderId).orElse(null);
                model.addAttribute("order", order);


            }

            // 🔹 B4: Trả về trang kết quả
            model.addAttribute("paymentStatus", paymentStatus == 1 ? "Thành công" : "Thất bại");
            return "user/payment_result";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("paymentStatus", "Lỗi hệ thống khi xử lý thanh toán");
            return "user/payment_result";
        }

    }




}

