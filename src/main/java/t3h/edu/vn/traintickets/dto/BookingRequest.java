package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Long tripId;
    private List<Long> seatIds;
    private List<Float> prices;
    private List<Byte> ticketTypes; // 0: 1 chiều, 1: chiều đi, 2: chiều về

}

