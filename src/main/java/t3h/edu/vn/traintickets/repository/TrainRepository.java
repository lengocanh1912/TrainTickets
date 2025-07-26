package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.Train;

import java.util.List;
import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {

    Train findByName(String name);

//    @Query("SELECT t FROM Train t JOIN FETCH t.coaches c WHERE t.id = :id")
//    Train findWithCoachesById(@Param("id") Long id);

    @Query("SELECT t FROM Train t JOIN FETCH t.coaches c JOIN FETCH c.seats WHERE t.id = :id")
    Train findWithCoachesById(@Param("id") Long id);

    @Query("SELECT t FROM Train t WHERE " +
            "CAST(t.id AS string) LIKE %:keyword% OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Train> searchByKeyword(@Param("keyword") String keyword);

}
