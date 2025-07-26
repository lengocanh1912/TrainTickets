package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import t3h.edu.vn.traintickets.dto.ReviewDisplayDto;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.ReviewImageRepository;
import t3h.edu.vn.traintickets.repository.ReviewRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ReviewImageRepository reviewImageRepository;

    public List<Review> searchReviewsByUser(String keyword) {
        return reviewRepository.searchByUserFullname(keyword);
    }

    public List<Review> getAllReview() {
        List<Review> review = null ;
        try{
            review = reviewRepository .findAll();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return review ;
    }

    @Transactional
    public void saveReview(Long orderId, int rating, String content, List<MultipartFile> images, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Lấy trip đầu tiên từ orderTickets
        if (order.getOrderTickets() == null || order.getOrderTickets().isEmpty()) {
            throw new RuntimeException("Không tìm thấy vé trong đơn hàng để lấy chuyến đi");
        }

        OrderTicket firstTicket = order.getOrderTickets().get(0);
        Trip trip = firstTicket.getTicket().getTrip(); // ✅ Bắt buộc để set vào Review

        Review review = new Review();
        review.setUser(order.getUser());
        review.setOrder(order);
        review.setTrip(trip);
        review.setRating(rating);
        review.setComment(content);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        review = reviewRepository.save(review);

        // Lưu ảnh nếu có
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                    Path path = Paths.get("uploads/review-images/" + filename);
                    try {
                        Files.createDirectories(path.getParent());
                        Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                        ReviewImage reviewImage = new ReviewImage();
                        reviewImage.setReview(review);
                        reviewImage.setFilePath("/uploads/review-images/" + filename);
                        reviewImageRepository.save(reviewImage);
                    } catch (IOException e) {
                        throw new RuntimeException("Lỗi khi lưu ảnh", e);
                    }
                }
            }
        }
    }

    public List<ReviewDisplayDto> getAllDisplayReviews() {
        List<ReviewDisplayDto> dtos = reviewRepository.findAllWithoutImages();

        // Lấy tất cả ảnh
        List<ReviewImage> allImages = reviewImageRepository.findAll();

        // Gom ảnh theo reviewId
        Map<Long, List<String>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getReview().getId(),
                        Collectors.mapping(ReviewImage::getFilePath, Collectors.toList())
                ));

        // Gán ảnh vào từng DTO
        for (ReviewDisplayDto dto : dtos) {
            List<String> images = imageMap.getOrDefault(dto.getReviewId(), null);
            dto.setImagePaths(images);
        }

        return dtos;
    }


    public Double getAverageRating() {
        Double avg = reviewRepository.findAverageRating(); // trên thang 5
        if (avg == null) return 0.0;
        double converted = avg * 2; // chuyển sang hệ 10
        return Math.round(converted * 10.0) / 10.0; // làm tròn 1 số thập phân
    }



    public Long getTotalReviewCount() {
        return reviewRepository.countTotalReviews();
    }


}
