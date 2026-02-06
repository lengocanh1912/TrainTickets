package t3h.edu.vn.traintickets.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.enums.CancelType;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;

import java.time.LocalDateTime;

@Component
public class OrderAutoCancelJob {
    private final OrderRepository orderRepository;

    public OrderAutoCancelJob(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedRate = 60000) // Mỗi 1 phút kiểm tra
    @Transactional
    public void autoCancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        var expiredOrders = orderRepository.findExpiredUnpaidOrders(now);
        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelType(CancelType.AUTO_EXPIRED);
            order.setCancelNote("Hệ thống tự động hủy do quá hạn thanh toán");
            order.setUpdatedAt(now);
            orderRepository.save(order);
            // Gọi service giải phóng ghế ở đây
        }
    }
}
