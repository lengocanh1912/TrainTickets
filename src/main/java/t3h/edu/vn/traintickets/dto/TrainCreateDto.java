package t3h.edu.vn.traintickets.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainCreateDto implements Serializable {

    private String name;
    private String code;
    private int coachCount;
    private List<CoachDto> coaches = new ArrayList<>();


}

