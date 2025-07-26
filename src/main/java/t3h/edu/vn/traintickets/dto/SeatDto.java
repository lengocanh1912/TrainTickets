package t3h.edu.vn.traintickets.dto;

import t3h.edu.vn.traintickets.entities.Seat;

public class SeatDto {
    private Long id;
    private String seatCode;
    private boolean booked;
    private float price;

    public SeatDto() {
    }

    public SeatDto(Long seatId, String seatCode, boolean booked, float price) {
        this.id = seatId;
        this.seatCode = seatCode;
        this.booked = booked;
        this.price = price;
    }

    // Getters và setters
    public Long getSeatId() { return id; }
    public void setSeatId(Long seatId) { this.id = seatId; }

    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }

    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }
}



