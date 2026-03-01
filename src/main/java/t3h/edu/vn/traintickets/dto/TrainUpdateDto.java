package t3h.edu.vn.traintickets.dto;

import lombok.*;
import t3h.edu.vn.traintickets.enums.TrainState;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainUpdateDto {
    private Long id;
    private String name;
    private String code;
    private TrainState state;
    private List<CoachDto> coaches;

}

