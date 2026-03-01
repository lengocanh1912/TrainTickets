package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.enums.OrderStatus;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.user u " +
            "JOIN FETCH o.orderTickets ot " +
            "JOIN FETCH ot.ticket t " +
            "JOIN FETCH t.seat s " +
            "JOIN FETCH s.coach c " +
            "JOIN FETCH t.trip tr " +
            "JOIN FETCH tr.route r " +
            "JOIN FETCH r.departure dep " +
            "JOIN FETCH r.arrival arr " +
            "JOIN FETCH tr.train train " +
            "WHERE LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Order> searchByKeyword(@Param("keyword") String keyword);




    // Tính tổng doanh thu từ các đơn hàng thành công
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = 'PAID' ")
    Double getTotalRevenue();

    @EntityGraph(attributePaths = {
            "orderTickets",
            "orderTickets.ticket",
            "orderTickets.ticket.trip",
            "orderTickets.ticket.trip.route",
            "orderTickets.ticket.trip.route.departure",
            "orderTickets.ticket.trip.route.arrival",
            "orderTickets.ticket.trip.train",
            "orderTickets.ticket.seat",
            "orderTickets.ticket.seat.coach"
    })
    Optional<Order> findWithTicketsById(Long id);

    @Query(value = """
    SELECT DISTINCT o FROM Order o
    JOIN FETCH o.user
    LEFT JOIN FETCH o.orderTickets ot
    LEFT JOIN FETCH ot.ticket t
    LEFT JOIN FETCH t.trip tr
    LEFT JOIN FETCH tr.train
    LEFT JOIN FETCH tr.route r
    LEFT JOIN FETCH r.departure
    LEFT JOIN FETCH r.arrival
    LEFT JOIN FETCH t.seat s
    LEFT JOIN FETCH s.coach
    """,
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllWithTicketsPage(Pageable pageable);


    @EntityGraph(attributePaths = {
            "orderTickets",
            "orderTickets.ticket",
            "orderTickets.ticket.trip",
            "orderTickets.ticket.trip.route",
            "orderTickets.ticket.trip.route.departure",
            "orderTickets.ticket.trip.route.arrival",
            "orderTickets.ticket.seat",
            "orderTickets.ticket.seat.coach"
    })
    @Query("""
            SELECT o FROM Order o
            WHERE o.user.username = :username
        """)
    Page<Order> findByUsername(
            @Param("username") String username,
            Pageable pageable
    );

    @Query("""
        SELECT 
            MONTH(o.createdAt),
            SUM(o.finalAmount)
        FROM Order o
        WHERE YEAR(o.createdAt) = :year
          AND o.status = :status
        GROUP BY MONTH(o.createdAt)
        ORDER BY MONTH(o.createdAt)
    """)
    List<Object[]> getMonthlyRevenue(
            @Param("year") int year,
            @Param("status") OrderStatus status
    );


    @Query("""
        SELECT COALESCE(SUM(o.finalAmount), 0)
        FROM Order o
        WHERE YEAR(o.createdAt) = :year
          AND o.status = :status
    """)
    BigDecimal getTotalRevenueByYear(
            @Param("year") int year,
            @Param("status") OrderStatus status
    );

    @Query("""
        SELECT o FROM Order o 
        JOIN o.orderTickets ot 
        WHERE ot.ticket.id = :ticketId
    """)
    Optional<Order> findByTicketId(@Param("ticketId") Long ticketId);

    Order findByTransactionCode(String transactionCode);

    List<Order> findByStatusAndHoldUntilBefore(
            OrderStatus status,
            LocalDateTime time);


    @Query("""
        SELECT 
            DAY(o.createdAt),
            SUM(o.finalAmount)
        FROM Order o
        WHERE YEAR(o.createdAt) = :year
          AND MONTH(o.createdAt) = :month
          AND o.status = :status
        GROUP BY DAY(o.createdAt)
        ORDER BY DAY(o.createdAt)
    """)
    List<Object[]> getDailyRevenue(
            @Param("year") int year,
            @Param("month") int month,
            @Param("status") OrderStatus status
    );

    @Query("""
    SELECT COALESCE(SUM(o.finalAmount), 0)
    FROM Order o
    WHERE YEAR(o.createdAt) = :year
      AND o.status = :status
""")
    BigDecimal getCancelledRevenueByYear(
            @Param("year") int year,
            @Param("status") OrderStatus status
    );

    @Query("""
    SELECT COALESCE(SUM(o.finalAmount), 0)
    FROM Order o
    WHERE YEAR(o.createdAt) = :year
      AND MONTH(o.createdAt) = :month
      AND o.status = :status
""")
    BigDecimal getCancelledRevenueByMonth(
            @Param("year") int year,
            @Param("month") int month,
            @Param("status") OrderStatus status
    );

    @Query("""
        SELECT COALESCE(SUM(o.finalAmount), 0)
        FROM Order o
        WHERE o.status = :status
    """)
    BigDecimal getTotalRevenueOrderPaid(@Param("status") OrderStatus status);

    @Query("""
    SELECT DISTINCT o FROM Order o
    LEFT JOIN FETCH o.orderTickets ot
    LEFT JOIN FETCH ot.ticket t
    LEFT JOIN FETCH t.seat s
    LEFT JOIN FETCH t.trip tr
    WHERE o.id = :id
""")
    Optional<Order> findByIdWithTickets(@Param("id") Long id);}
