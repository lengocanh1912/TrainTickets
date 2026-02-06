package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.dto.TrainDto;
import t3h.edu.vn.traintickets.entities.Coach;
import t3h.edu.vn.traintickets.entities.Train;

import java.util.List;
import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {

    Train findByName(String name);

//    @Query("SELECT t FROM Train t JOIN FETCH t.coaches c WHERE t.id = :id")
//    Train findWithCoachesById(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT t
        FROM Train t
        JOIN FETCH t.coaches c
        JOIN FETCH c.seats
        WHERE t.id = :id
    """)
    Train findWithCoachesById(@Param("id") Long id);


    @Query("SELECT t FROM Train t WHERE " +
            "CAST(t.id AS string) LIKE %:keyword% OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Train> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Train t JOIN FETCH t.coaches WHERE t.id = :id")
    Train findTrainAndCoachesById(@Param("id") Long id);

    @Query("SELECT t FROM Train t WHERE t.id IN :ids")
    List<Train> findAllByIds(@Param("ids") List<Long> ids);

    @Query("""
        SELECT DISTINCT t
        FROM Train t
        LEFT JOIN FETCH t.coaches
        WHERE t.id IN :ids
    """)
    List<Train> findAllWithCoachesByIds(@Param("ids") List<Long> ids);

    Page<Train> findAll(Pageable pageable);


    @EntityGraph(attributePaths = {"coaches"})
    @Query("SELECT t FROM Train t")
    Page<Train> findAllWithCoaches(Pageable pageable);

}
