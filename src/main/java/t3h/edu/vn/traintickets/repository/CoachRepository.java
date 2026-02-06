package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.Coach;

import java.util.List;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    List<Coach> findByTrainId(Long trainId);

    @Query("SELECT DISTINCT c FROM Coach c JOIN FETCH c.seats WHERE c.train.id = :id")
    List<Coach> findCoachesWithSeatsByTrainId(@Param("id") Long id);


}
