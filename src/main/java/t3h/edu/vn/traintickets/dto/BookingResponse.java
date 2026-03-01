package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import t3h.edu.vn.traintickets.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long orderId;
    private String orderCode;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime holdUntil;

}