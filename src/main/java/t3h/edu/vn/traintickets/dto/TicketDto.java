package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    private Long id;
    private String userFullname;
    private String trainName;
    private String coachCode;
    private String seatCode;
    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;
    private Float price;
    private Byte ticketType;
    private LocalDateTime createdAt;
    private TicketStatus status;

}
