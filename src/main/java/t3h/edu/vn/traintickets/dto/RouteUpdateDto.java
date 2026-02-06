package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RouteUpdateDto {
    @NotNull
    private Long id;

    @NotNull
    private Long departureId;

    @NotNull
    private Long arrivalId;

    @NotNull
    private Float distanceKm;

}
