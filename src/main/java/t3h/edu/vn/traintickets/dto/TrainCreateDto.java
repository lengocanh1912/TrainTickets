package t3h.edu.vn.traintickets.dto;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TrainCreateDto implements Serializable {

    private String name;
    private String code;
    private int coachCount;
    private List<CoachDto> coaches = new ArrayList<>();

    // Constructors
    public TrainCreateDto() {}

    public TrainCreateDto(String name, String code, List<CoachDto> coaches) {
        this.name = name;
        this.code = code;
        this.coaches = coaches;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<CoachDto> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<CoachDto> coaches) {
        this.coaches = coaches;
    }
}

