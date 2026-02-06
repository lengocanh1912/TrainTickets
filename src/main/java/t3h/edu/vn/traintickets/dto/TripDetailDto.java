package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailDto {
    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private String departureDate;
//    private BigDecimal price;
    private List<CoachDto> coaches;

}
