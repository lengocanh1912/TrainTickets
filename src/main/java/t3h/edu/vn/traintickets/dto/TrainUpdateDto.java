package t3h.edu.vn.traintickets.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrainUpdateDto {
    private Long id;
    private String name;
    private String code;
    private List<CoachDto> coaches;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

