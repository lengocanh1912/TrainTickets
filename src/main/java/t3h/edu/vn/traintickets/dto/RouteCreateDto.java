package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RouteCreateDto {
    private Long id;
    @NotNull
    private Long departureId;
    @NotNull
    private Long arrivalId;

    @NotNull
    private Float distanceKm;


}



