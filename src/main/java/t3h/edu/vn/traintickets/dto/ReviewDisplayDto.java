package t3h.edu.vn.traintickets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import t3h.edu.vn.traintickets.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ReviewDisplayDto {

    private Long reviewId;
    private String userFullName;
    private String trainName;
    private String departureStationName;
    private String arrivalStationName;
    private LocalDateTime departureTime;
    private Integer rating;
    private String comment;
    private List<String> imagePaths;
    private LocalDateTime createdAt;
    private ReviewStatus status;

    // admin reply
    private String adminReply;
    private LocalDateTime replyCreatedAt;

    // ✅ CONSTRUCTOR KHỚP JPQL (10 PARAM)
    public ReviewDisplayDto(
            Long reviewId,
            String userFullName,
            String trainName,
            String departureStationName,
            String arrivalStationName,
            LocalDateTime departureTime,
            Integer rating,
            String comment,
            LocalDateTime createdAt,
            ReviewStatus status
    ) {
        this.reviewId = reviewId;
        this.userFullName = userFullName;
        this.trainName = trainName;
        this.departureStationName = departureStationName;
        this.arrivalStationName = arrivalStationName;
        this.departureTime = departureTime;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.status = status;
        this.imagePaths = null; // gán sau
    }
}
