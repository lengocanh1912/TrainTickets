package t3h.edu.vn.traintickets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.dto.ReviewDisplayDto;
import t3h.edu.vn.traintickets.entities.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT new t3h.edu.vn.traintickets.dto.ReviewDisplayDto(" +
            "r.id, u.fullname, t.train.name, rt.departure.name, rt.arrival.name, " +
            "t.departureAt, r.rating, r.comment, r.createdAt, r.status) " +
            "FROM Review r " +
            "JOIN r.order o " +
            "JOIN o.user u " +
            "JOIN r.trip t " +
            "JOIN t.route rt " +
            "WHERE LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ReviewDisplayDto> searchByUserFullname(@Param("keyword") String keyword);


    @Query("""
    SELECT new t3h.edu.vn.traintickets.dto.ReviewDisplayDto(
        r.id,
        r.user.fullname,
        t.train.name,
        t.route.departure.name,
        t.route.arrival.name,
        t.departureAt,
        r.rating,
        r.comment,
        r.createdAt,
        r.status
    )
    FROM Review r
    JOIN r.trip t
""")
    Page<ReviewDisplayDto> findAllWithoutImages(Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double findAverageRating();

    @Query("SELECT COUNT(r) FROM Review r")
    Long countTotalReviews();

    @EntityGraph(attributePaths = {
            "order.user",
            "trip.train",
            "trip.route.departure",
            "trip.route.arrival",
            "reviewImages"
    })
    Optional<Review> findWithOrderAndUserById(Long id);

    boolean existsByOrderId(Long id);
}
