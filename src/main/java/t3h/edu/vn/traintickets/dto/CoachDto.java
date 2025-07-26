package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import t3h.edu.vn.traintickets.enums.CoachState;

import java.util.List;

@Data
public class CoachDto {
    private long id;
    private String type;
    private int capacity;
    private String code;
    private List<SeatDto> seats;
    private CoachState state;
    private int position;


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public CoachState getState() {
        return state;
    }

    public void setState(CoachState state) {
        this.state = state;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public @NotNull int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<SeatDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDto> seats) {
        this.seats = seats;
    }
}
