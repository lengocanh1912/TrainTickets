package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.entities.OrderTicket;

import java.util.List;

public interface OrderTicketRepository extends JpaRepository<OrderTicket, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderTicket ot WHERE ot.ticket.id = :ticketId")
    void deleteByTicketId(@Param("ticketId") Long ticketId);


    List<OrderTicket> findByOrderId(Long orderId);


}
