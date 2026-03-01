package t3h.edu.vn.traintickets.scheduler;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import t3h.edu.vn.traintickets.service.OrderService;


@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderService orderService;

    // Chạy mỗi phút
    @Scheduled(cron = "0 * * * * *")
    public void autoCancelExpiredOrders() {
        orderService.autoCancelExpiredOrders();
    }
}
