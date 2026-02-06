package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDetailDto {
    private Long Id;
    private String orderCode;
    private String userFullName;
    private String userEmail;

    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;

    private Float price;
    private String seatName;
    private String coachName;
    private String ticketType;
    private String ticketStatus;

    private String paymentMethod;
    private String refundPolicy;
    private List<TicketLogDto> logs;

}
