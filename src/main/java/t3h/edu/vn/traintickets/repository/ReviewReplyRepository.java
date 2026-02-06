package t3h.edu.vn.traintickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t3h.edu.vn.traintickets.entities.ReviewReply;

import java.util.Optional;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    Optional<ReviewReply> findByReviewId(Long reviewId);
}
