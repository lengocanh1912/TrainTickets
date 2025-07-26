package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ReviewDisplayDto {
    private Long reviewId; // 👈 Thêm trường này
    private String userFullName;
    private String trainName;
    private String departureStationName;
    private String arrivalStationName;
    private LocalDateTime departureTime;
    private Integer rating;
    private String comment;
    private List<String> imagePaths;
    private LocalDateTime createdAt;

    // Constructor dùng trong JPQL (imagePaths = null)
    public ReviewDisplayDto(
            Long reviewId,
            String userFullName,
            String trainName,
            String departureStationName,
            String arrivalStationName,
            LocalDateTime departureTime,
            Integer rating,
            String comment,
            LocalDateTime createdAt
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
        this.imagePaths = null; // sẽ gán sau
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getDepartureStationName() {
        return departureStationName;
    }

    public void setDepartureStationName(String departureStationName) {
        this.departureStationName = departureStationName;
    }

    public String getArrivalStationName() {
        return arrivalStationName;
    }

    public void setArrivalStationName(String arrivalStationName) {
        this.arrivalStationName = arrivalStationName;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

