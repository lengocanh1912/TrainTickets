package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import t3h.edu.vn.traintickets.enums.CoachState;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoachDto {
    private Long id;
    private String type;
    private int capacity;
    private String code;
    private CoachState state;
    private int position;
    private List<SeatDto> seats;

}
