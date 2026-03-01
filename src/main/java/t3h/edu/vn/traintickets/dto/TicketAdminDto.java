package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketAdminDto {
    private Long id;
    private String userFullname;
    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;
    private String seatCode;
    private Byte ticketType;
    private BigDecimal price;
    private TicketStatus status;
    private LocalDateTime createdAt;

}
