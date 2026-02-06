package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t3h.edu.vn.traintickets.entities.TicketLog;
import java.util.List;

public interface TicketLogRepository extends JpaRepository<TicketLog, Long> {
    List<TicketLog> findAllByTicketIdOrderByUpdatedAtDesc(Long ticketId);
}
