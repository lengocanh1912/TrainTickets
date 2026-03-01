package t3h.edu.vn.traintickets.controller.verify;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.booking.VNPayService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayIPNController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @PostMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleIPN(HttpServletRequest request) {

        System.out.println("\n====== VNPay IPN CALLED ======");
        request.getParameterMap().forEach((k, v) -> {
            System.out.println(k + " = " + Arrays.toString(v));
        });
        System.out.println("==============================\n");

        int result = vnPayService.processVNPayResponse(request);

        Map<String, String> res = new HashMap<>();
        if (result == 1) {
            res.put("RspCode", "00");
            res.put("Message", "Confirm Success");
        } else {
            res.put("RspCode", "99");
            res.put("Message", "Confirm Fail");
        }
        return ResponseEntity.ok(res);
    }

}


