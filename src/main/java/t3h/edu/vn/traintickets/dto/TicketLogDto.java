package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketLogDto {
    private String action;
    private TicketStatus oldStatus;
    private TicketStatus newStatus;
    private String updatedBy;
    private String reason;
    private LocalDateTime updatedAt;

}

