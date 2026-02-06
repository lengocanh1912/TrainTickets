package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.dto.TripDto;
import t3h.edu.vn.traintickets.entities.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Trip findByTrainId(long trainId);

    @Query("SELECT tr FROM Trip tr " +
            "JOIN tr.route r " +
            "JOIN r.departure d " +
            "JOIN r.arrival a " +
            "WHERE LOWER(CONCAT(d.name, ' đến ', a.name)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Trip> searchByRouteName(@Param("keyword") String keyword);




    @Query("""
SELECT COUNT(t) > 0 FROM Trip t
WHERE t.train.id = :trainId
AND NOT (
    t.arrivalAt <= :departureAt OR t.departureAt >= :arrivalAt
)
""")
    boolean existsByTrainAndTimeOverlap(Long trainId, LocalDateTime departureAt, LocalDateTime arrivalAt);

    // tìm chuyến đang hoạt động
    @Query("""
    SELECT new t3h.edu.vn.traintickets.dto.TripDto(
        t.id,
        t.train.name,
        r.departure.name,
        r.arrival.name,
        t.price,
        t.departureAt,
        t.arrivalAt
    )
    FROM Trip t
    JOIN t.route r
    JOIN r.departure d
    JOIN r.arrival a
    WHERE d.name = :departureName
      AND a.name = :arrivalName
      AND DATE(t.departureAt) = :departureAt
      AND t.status = t3h.edu.vn.traintickets.enums.TripState.ACTIVE
      AND (
              SELECT COUNT(s) FROM Seat s
              WHERE s.coach.train = t.train
              AND s.id NOT IN (
                  SELECT ti.seat.id FROM Ticket ti WHERE ti.trip = t
              )
          ) >= :ticketQuantity
    """)
    List<TripDto> findTripsByStationNames(
            @Param("departureName") String departureName,
            @Param("arrivalName") String arrivalName,
            @Param("departureAt") LocalDate departureAt,
            @Param("ticketQuantity") int ticketQuantity
    );


    //tripDto-list
    @Query("SELECT new t3h.edu.vn.traintickets.dto.TripDto(" +
            "t.id, tr.name, s1.name, s2.name, t.price, t.departureAt, t.arrivalAt) " +
            "FROM Trip t " +
            "JOIN t.train tr " +
            "JOIN t.route r " +
            "JOIN r.departure s1 " +
            "JOIN r.arrival s2"
            )
    List<TripDto> getAllTripDtos();

    @Query("SELECT new t3h.edu.vn.traintickets.dto.TripDto(" +
            "t.id, tr.name, s1.name, s2.name, t.price, t.departureAt, t.arrivalAt) " +
            "FROM Trip t " +
            "JOIN t.train tr " +
            "JOIN t.route r " +
            "JOIN r.departure s1 " +
            "JOIN r.arrival s2 " +
            "WHERE t.id = :id"
            )
    List<TripDto> findByIdDto(long id);

    @Query("""
    SELECT t FROM Trip t
    JOIN FETCH t.train tr
    JOIN FETCH t.route r
    JOIN FETCH r.departure
    JOIN FETCH r.arrival
    """)
    Page<Trip> findAllWithDetails(Pageable pageable);

    @Query("""
    SELECT COUNT(t) > 0 FROM Trip t
    WHERE t.train.id = :trainId
      AND t.id <> :currentTripId
      AND (
          (:departureAt BETWEEN t.departureAt AND t.arrivalAt)
          OR (:arrivalAt BETWEEN t.departureAt AND t.arrivalAt)
          OR (t.departureAt BETWEEN :departureAt AND :arrivalAt)
      )
""")
    boolean existsByTrainAndTimeOverlapExceptCurrent(
            Long trainId,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt,
            Long currentTripId
    );




}
