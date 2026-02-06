package t3h.edu.vn.traintickets.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.entities.OrderTicket;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.OrderTicketRepository;
import t3h.edu.vn.traintickets.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderHoldScheduler {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderTicketRepository orderTicketRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Scheduled(fixedRate = 60_000) // mỗi phút check 1 lần
    @Transactional
    public void expirePendingOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> expiredOrders = orderRepository.findByStatusAndHoldUntilBefore(
                OrderStatus.PENDING, now
        );

        for (Order order : expiredOrders) {
            // 1. Hủy tất cả vé trong order
            for (OrderTicket ot : order.getOrderTickets()) {
                Ticket ticket = ot.getTicket();
                ticket.setStatus(TicketStatus.CANCELLED);
                ticket.setUpdatedAt(now);
                ticketRepository.save(ticket);

                // ✅ Xóa mapping OrderTicket cũ
                orderTicketRepository.deleteByTicketId(ticket.getId());
            }

            // 2. Hủy Order
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            System.out.println("Order expired and cancelled: " + order.getId());
        }
    }
}

