package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t3h.edu.vn.traintickets.entities.Coach;

import java.util.List;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    List<Coach> findByTrainId(Long trainId);
}
