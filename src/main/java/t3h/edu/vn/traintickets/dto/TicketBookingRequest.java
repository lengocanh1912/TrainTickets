package t3h.edu.vn.traintickets.dto;

import java.util.List;

public class TicketBookingRequest {
    private Long tripId;
    private List<Long> seatIds;
    private List<String> ticketTypes; // Ví dụ: Người lớn, Trẻ em
}

