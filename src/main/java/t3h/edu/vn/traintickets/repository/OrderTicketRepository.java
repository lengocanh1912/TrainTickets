package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t3h.edu.vn.traintickets.entities.OrderTicket;

public interface OrderTicketRepository extends JpaRepository<OrderTicket, Long> {
}
