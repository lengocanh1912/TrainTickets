package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t3h.edu.vn.traintickets.entities.ReviewImage;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

        List<ReviewImage> findByReviewId(Long reviewId);


    List<ReviewImage> findAllByReviewIdIn(List<Long> reviewIds);
}
