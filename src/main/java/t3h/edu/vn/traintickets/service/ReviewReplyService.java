package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.entities.Review;
import t3h.edu.vn.traintickets.entities.ReviewReply;
import t3h.edu.vn.traintickets.enums.ReviewStatus;
import t3h.edu.vn.traintickets.repository.ReviewReplyRepository;
import t3h.edu.vn.traintickets.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewReplyService {

    @Autowired
    private ReviewReplyRepository reviewReplyRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public void saveOrUpdateReply(Long reviewId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung phản hồi không được để trống");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá với ID: " + reviewId));

        Optional<ReviewReply> existing = reviewReplyRepository.findByReviewId(reviewId);

        ReviewReply reply = existing.orElseGet(() -> {
            ReviewReply r = new ReviewReply();
            r.setReview(review);
            LocalDateTime now = LocalDateTime.now();
            r.setCreatedAt(now);
            r.setUpdatedAt(now);
            return r;
        });

        reply.setContent(content);
        reply.setUpdatedAt(LocalDateTime.now());

        review.setStatus(ReviewStatus.REPLIED);

        reviewReplyRepository.save(reply);
        reviewRepository.save(review);
    }
}
