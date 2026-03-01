package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    private List<TicketRequest> tickets;

    @Data
    public static class TicketRequest {
        private Long seatId;
        private Byte ticketType;
    }
}

