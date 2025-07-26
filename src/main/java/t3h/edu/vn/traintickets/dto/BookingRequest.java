package t3h.edu.vn.traintickets.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    private Long tripId;
    private List<Long> seatIds;
    private List<Float> prices;
    private List<Byte> ticketTypes; // 0: 1 chiều, 1: chiều đi, 2: chiều về

//    getter & setter

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

    public List<Float> getPrices() {
        return prices;
    }

    public void setPrices(List<Float> prices) {
        this.prices = prices;
    }

    public List<Byte> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(List<Byte> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }
}

