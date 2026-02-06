package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import t3h.edu.vn.traintickets.enums.TrainState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainDto {
    private Long id;
    private String name;
    private String code;
    private int capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TrainState state;
    private List<CoachDto> coaches;

}

