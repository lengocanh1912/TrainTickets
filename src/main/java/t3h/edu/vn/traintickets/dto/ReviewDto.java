package t3h.edu.vn.traintickets.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ReviewDto {
    private Long orderId;
    private int rating;
    private String comment;
    private List<MultipartFile> images;

    // getters/setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
}

