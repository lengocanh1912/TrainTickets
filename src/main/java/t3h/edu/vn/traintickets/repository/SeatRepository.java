package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t3h.edu.vn.traintickets.entities.Seat;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByCoachId(Long coachId);
}
