package t3h.edu.vn.traintickets.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.Seat;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByCoachId(Long coachId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids order by s.id")
    List<Seat> findAllByIdForUpdate(@Param("ids") List<Long> ids);

}
