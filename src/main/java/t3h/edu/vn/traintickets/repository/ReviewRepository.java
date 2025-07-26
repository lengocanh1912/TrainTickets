package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t3h.edu.vn.traintickets.dto.ReviewDisplayDto;
import t3h.edu.vn.traintickets.entities.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r " +
            "JOIN r.order o " +
            "JOIN o.user u " +
            "WHERE LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Review> searchByUserFullname(@Param("keyword") String keyword);

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
        r.createdAt
    )
    FROM Review r
    JOIN r.trip t
""")
    List<ReviewDisplayDto> findAllWithoutImages();

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double findAverageRating();

    @Query("SELECT COUNT(r) FROM Review r")
    Long countTotalReviews();

}
