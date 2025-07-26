package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.Seat;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.entities.Trip;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTripId(Long tripId);

    @Query("SELECT t.seat.id FROM Ticket t WHERE t.trip.id = :tripId AND t.status = 1")
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


    boolean existsBySeatAndTripAndStatusNot(Seat seat, Trip trip, byte b);
}
