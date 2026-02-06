package t3h.edu.vn.traintickets.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainUpdateDto {
    private Long id;
    private String name;
    private String code;
    private List<CoachDto> coaches;

}

