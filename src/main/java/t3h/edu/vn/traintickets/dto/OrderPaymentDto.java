package t3h.edu.vn.traintickets.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderPaymentDto {

    private Long orderId;
    private String orderCode;

    // Tổng tiền đơn
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime holdUntil;


    // Thông tin chuyến (lấy từ ticket đầu tiên)
    private String departureStation;
    private String arrivalStation;
    private String trainCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    //Thông tin liên hệ
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    // Danh sách vé
    private List<TicketPaymentDto> tickets;

    //Phương thức thanh toán
    private String paymentMethod;

}

