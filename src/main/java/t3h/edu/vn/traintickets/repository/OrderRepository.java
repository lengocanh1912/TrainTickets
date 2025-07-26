package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.Order;


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


    //danh sách đơn hàng của người dùng hiện tại
//    @EntityGraph(attributePaths = {
//            "orderTickets",
//            "orderTickets.ticket",
//            "orderTickets.ticket.trip",
//            "orderTickets.ticket.seat",
//            "orderTickets.ticket.trip.departureStation",
//            "orderTickets.ticket.trip.arrivalStation"
//    })
//    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
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
    List<Order> findByUserIdWithTickets(@Param("userId") Long userId);




    // Tính tổng doanh thu từ các đơn hàng thành công
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = 1")
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


    @Query("""
    SELECT o FROM Order o
    JOIN FETCH o.user u
    LEFT JOIN FETCH o.orderTickets ot
    LEFT JOIN FETCH ot.ticket t
    LEFT JOIN FETCH t.trip tr
    LEFT JOIN FETCH tr.train
    LEFT JOIN FETCH t.seat s
    LEFT JOIN FETCH s.coach c
    LEFT JOIN FETCH tr.route r
    LEFT JOIN FETCH r.departure
    LEFT JOIN FETCH r.arrival
    """)
    List<Order> findAllWithTickets();

    boolean existsByOrderCode(String orderCode);

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

    @Query("SELECT MONTH(o.createdAt), SUM(o.finalAmount) " +
            "FROM Order o WHERE YEAR(o.createdAt) = :year AND o.status = 1 " +
            "GROUP BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE YEAR(o.createdAt) = :year AND o.status = 1")
    Float getTotalRevenueByYear(@Param("year") int year);
}
