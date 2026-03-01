package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCancelFormDto {

    private Long id;
    private String orderCode;
    private BigDecimal price;
    private TicketStatus status;
    private String seatName;
    private String userFullName;
    private String tripRoute;

//    // ✅ CONSTRUCTOR CHUẨN CHO JPQL
//    public TicketCancelFormDto(
//            Long id,
//            String orderCode,
//            BigDecimal price,
//            TicketStatus status,
//            String seatName,
//            String userFullName,
//            String tripRoute
//    ) {
//        this.id = id;
//        this.orderCode = orderCode;
//        this.price = price;
//        this.status = status;
//        this.seatName = seatName;
//        this.userFullName = userFullName;
//        this.tripRoute = tripRoute;
//    }

}
