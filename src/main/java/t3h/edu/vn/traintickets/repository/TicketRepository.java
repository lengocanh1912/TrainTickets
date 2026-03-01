package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.dto.TicketCancelFormDto;
import t3h.edu.vn.traintickets.event.TicketPdfDto;
import t3h.edu.vn.traintickets.entities.Seat;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.entities.Trip;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTripId(Long tripId);

    @Query("SELECT t.seat.id FROM Ticket t WHERE t.trip.id = :tripId AND t.status = 'PAID'")
    List<Long> findBookedSeatIdsByTrip(@Param("tripId") Long tripId);

    boolean existsBySeatIdAndTripId(Long seatId, Long tripId);

    @EntityGraph(attributePaths = {
            "user", "seat", "seat.coach", "trip", "trip.train", "trip.route", "trip.route.departure", "trip.route.arrival"
    })
    @Query("SELECT t FROM Ticket t")
    Page<Ticket> findAllWithDependencies(Pageable pageable);

    @EntityGraph(attributePaths = {
            "user",
            "seat", "seat.coach",
            "trip", "trip.train",
            "trip.route", "trip.route.departure", "trip.route.arrival"
    })
    @Query("SELECT t FROM Ticket t " +
            "JOIN t.user u " +
            "JOIN t.trip tr " +
            "JOIN tr.route r " +
            "JOIN r.departure d " +
            "JOIN r.arrival a " +
            "WHERE LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(CONCAT(d.name, ' đến ', a.name)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> searchByUserOrTripName(@Param("keyword") String keyword);

    Optional<Ticket> findBySeatAndTripAndStatus(Seat seat,
                                                Trip trip,
                                                TicketStatus status);

    // Kiểm tra ticket có cùng seat + trip và status thuộc list
    boolean existsBySeatAndTripAndStatusIn(Seat seat,
                                           Trip trip,
                                           List<TicketStatus> statusList);

    @Query("""
    SELECT t FROM Ticket t
    JOIN FETCH t.user u
    JOIN FETCH t.trip tr
    JOIN FETCH tr.route r
    JOIN FETCH r.departure dep
    JOIN FETCH r.arrival arr
    JOIN FETCH t.seat s
    WHERE (:keyword IS NULL OR LOWER(TRIM(u.fullname)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%')))
      AND (:status IS NULL OR t.status = :status)
      AND (:trainId IS NULL OR tr.train.id = :trainId)
      AND (:departureDate IS NULL OR FUNCTION('DATE', tr.departureAt) = :departureDate)
      AND (
            (:departure IS NULL AND :arrival IS NULL)
            OR (:departure IS NOT NULL AND :arrival IS NULL AND LOWER(dep.name) LIKE LOWER(CONCAT('%', :departure, '%')))
            OR (:departure IS NULL AND :arrival IS NOT NULL AND LOWER(arr.name) LIKE LOWER(CONCAT('%', :arrival, '%')))
            OR (:departure IS NOT NULL AND :arrival IS NOT NULL 
                AND LOWER(dep.name) LIKE LOWER(CONCAT('%', :departure, '%')) 
                AND LOWER(arr.name) LIKE LOWER(CONCAT('%', :arrival, '%')))
          )
    ORDER BY t.createdAt DESC
    """)
    List<Ticket> searchTickets(
            @Param("keyword") String keyword,
            @Param("status") TicketStatus status,
            @Param("trainId") Long trainId,
            @Param("departureDate") LocalDate departureDate,
            @Param("departure") String departure,
            @Param("arrival") String arrival
    );

    @Query("SELECT t FROM Ticket t " +
            "LEFT JOIN FETCH t.user " +
            "LEFT JOIN FETCH t.trip tr " +
            "LEFT JOIN FETCH tr.route " +
            "LEFT JOIN FETCH t.seat " +
            "WHERE t.id = :id")
    Ticket findDetailById(@Param("id") Long id);

    @Query("""
    SELECT new t3h.edu.vn.traintickets.dto.TicketCancelFormDto(
        t.id,
        CONCAT('T', t.id),
        t.price,
        t.status,
        s.seatCode,
        u.fullname,
        CONCAT(r.departure.name, ' → ', r.arrival.name)
    )
    FROM Ticket t
    JOIN t.seat s
    JOIN t.user u
    JOIN t.trip tr
    JOIN tr.route r
    WHERE t.id = :id
""")
    Optional<TicketCancelFormDto> findCancelViewById(@Param("id") Long id);

    @Query("""
    SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
    FROM Ticket t
    WHERE t.seat.id = :seatId
      AND t.trip.id = :tripId
      AND t.status NOT IN (:cancelled, :refunded)
""")
    boolean existsValidTicket(@Param("seatId") Long seatId,
                              @Param("tripId") Long tripId,
                              @Param("cancelled") TicketStatus cancelled,
                              @Param("refunded") TicketStatus refunded);


    @Query("""
    select new t3h.edu.vn.traintickets.event.TicketPdfDto(
        t.id,
        t.ticketCode,
        
        t.passengerName,
        t.cccd,
        t.birthday,
        t.address,
        t.ticketType,

        dep.name,
        arr.name,

        trn.name,
        trip.departureAt,
        trip.arrivalAt,
        
        coach.code,
        seat.seatCode,

        t.price
    )
    from Ticket t
    join t.trip trip
    join trip.route r
    join r.departure dep
    join r.arrival arr
    join trip.train trn
    join t.seat seat
    join seat.coach coach
    where t.id = :ticketId
""")
    Optional<TicketPdfDto> findTicketPdfDto(@Param("ticketId") Long ticketId);


    @Query("""
        select t.id
        from OrderTicket ot
        join ot.ticket t
        join ot.order o
        where o.id = :orderId
    """)
    List<Long> findTicketIdsByOrderId(@Param("orderId") Long orderId);

    boolean existsByTicketCode(String code);

    @Query("""
    select t from Ticket t
    where t.trip = :trip
    and t.seat.id in :seatIds
    and t.status in :statuses
""")
    List<Ticket> findByTripAndSeatIdInAndStatusIn(
            @Param("trip") Trip trip,
            @Param("seatIds") List<Long> seatIds,
            @Param("statuses") List<TicketStatus> statuses
    );
}
