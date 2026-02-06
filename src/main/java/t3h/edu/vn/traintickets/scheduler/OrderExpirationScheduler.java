package t3h.edu.vn.traintickets.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.enums.CancelType;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;


@Component
public class OrderExpirationScheduler {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;
    private static final Logger log = LoggerFactory.getLogger(OrderExpirationScheduler.class);

    /**
     * Chạy mỗi phút để kiểm tra đơn hết hạn
     */
    @Scheduled(fixedRate = 60000) // 1 phút
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm các đơn PENDING hoặc RESERVED và đã hết hạn
        List<Order> expiredOrders = orderRepository
                .findByStatusAndExpiresAtBefore(OrderStatus.PENDING, now);

        for (Order order : expiredOrders) {
            try {
                // Cập nhật trạng thái
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelType(CancelType.AUTO_EXPIRED);
                order.setCancelNote("Hệ thống tự động hủy do quá thời gian thanh toán");
                order.setUpdatedAt(now);

                orderRepository.save(order);

                // Giải phóng ghế
                orderService.cancelOrderAndReleaseEverything(order.getId());

                log.info("Auto-cancelled expired order: {}", order.getOrderCode());

            } catch (Exception e) {
                log.error("Error cancelling order {}: {}",
                        order.getOrderCode(), e.getMessage());
            }
        }

        if (!expiredOrders.isEmpty()) {
            log.info("Cancelled {} expired orders", expiredOrders.size());
        }
    }
}
