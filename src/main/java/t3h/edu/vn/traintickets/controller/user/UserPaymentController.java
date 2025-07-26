package t3h.edu.vn.traintickets.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.VNPayService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/user/payment")
public class UserPaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderService orderService;

    @PostMapping
    public String createPayment(@RequestParam("orderId") Long orderId,
                                HttpServletRequest request) {
        Order order = orderService.findById(orderId);
        String paymentUrl = vnPayService.createVNPayPayment(order, request);

        return "redirect:" + paymentUrl;
    }

    @GetMapping("/return")
    public String handleVNPayReturn(HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.processVNPayResponse(request);

        // Lấy orderId từ VNPay (txnRef)
        String orderIdStr = request.getParameter("vnp_TxnRef");
        if (paymentStatus == 1 && orderIdStr != null) {
            try {
                Long orderId = Long.parseLong(orderIdStr);
                orderService.updateOrderStatusToPaid(orderId); // Cập nhật DB
            } catch (Exception e) {
                // Có thể log hoặc set lỗi cho model nếu cần
                e.printStackTrace();
            }
        }

        model.addAttribute("paymentStatus", paymentStatus == 1 ? "Thành công" : "Thất bại");
        return "user/payment_result";
    }


//    @PostMapping("/create")
//    public ResponseEntity<?> createPayment(
//            @RequestParam Long amount,
//            @RequestParam String orderInfo,
//            @RequestParam String returnUrl) {
//
//        try {
//            String paymentUrl = vnPayService.createVNPayUrl(amount, orderInfo, returnUrl);
//            return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", e.getMessage()));
//        }
//    }
}

