package t3h.edu.vn.traintickets.controller.restcontroller.user;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.VNPayService;

import java.util.Map;

@RestController
@RequestMapping("/user/qr-payment")
public class QRPaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayService vnPayService;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateQrUrl(@RequestParam Long orderId, HttpServletRequest request) {
        System.out.println("==> QR generate endpoint hit with orderId: " + orderId);

        Order order = orderService.findById(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
        }

        String paymentUrl = vnPayService.createVNPayPayment(order, request);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

}

