//package t3h.edu.vn.traintickets.controller.restcontroller.user;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import t3h.edu.vn.traintickets.entities.Order;
//import t3h.edu.vn.traintickets.enums.CancelType;
//import t3h.edu.vn.traintickets.enums.OrderStatus;
//import t3h.edu.vn.traintickets.repository.OrderRepository;
//import t3h.edu.vn.traintickets.service.OrderService;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/orders")
//public class OrderRestController {
//
//    @Autowired
//    private OrderService orderService;
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @PostMapping("/{orderId}/cancel-expired")
//    public ResponseEntity<?> cancelExpiredOrder(@PathVariable Long orderId) {
//        try {
//            Order order = orderService.findById(orderId);
//
//            if (order == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            // Kiểm tra xem đơn đã thanh toán chưa
//            if (order.getStatus() == OrderStatus.PAID) {
//                return ResponseEntity.badRequest()
//                        .body(Map.of("message", "Đơn hàng đã thanh toán"));
//            }
//
//            // Kiểm tra thời gian hết hạn
//            if (order.getHoldUntil() != null &&
//                    order.getHoldUntil().isBefore(LocalDateTime.now())) {
//                // Giải phóng ghế đã giữ
//                orderService.cancelOrderAndReleaseEverything(orderId);
//
//                return ResponseEntity.ok(Map.of(
//                        "success", true,
//                        "message", "Đơn hàng đã được hủy",
//                        "orderId", orderId
//                ));
//
//            }
//
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "Đơn hàng chưa hết hạn"));
//
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
//        }
//    }
//}