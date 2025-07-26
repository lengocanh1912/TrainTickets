package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoachDetailDto {
    private String code;
    private String type;
    private int availableSeats;
    private List<SeatDetailDto> seats;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public List<SeatDetailDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDetailDto> seats) {
        this.seats = seats;
    }
}
