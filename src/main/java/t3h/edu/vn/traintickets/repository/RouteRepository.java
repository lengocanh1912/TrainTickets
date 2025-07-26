package t3h.edu.vn.traintickets.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.entities.Route;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface RouteRepository extends JpaRepository<Route, Long> {

    // đảm bảo load luôn departure và arrival
    @EntityGraph(attributePaths = {"departure", "arrival"})
    Page<Route> findAll(Pageable pageable);

    // Dùng cho form create/update Trip
    @EntityGraph(attributePaths = {"departure", "arrival"})
    List<Route> findAll(); // hoặc đổi tên thành findAllBy() cũng được

    @Query("SELECT r FROM Route r " +
            "JOIN FETCH r.departure d " +
            "JOIN FETCH r.arrival a " +
            "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Route> searchByKeyword(@Param("keyword") String keyword);



}
