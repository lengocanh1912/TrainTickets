package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripCreateDto {
    private Long id;
    @NotNull
    private Long trainId;
    @NotNull
    private Long routeId;
    @NotNull
    private LocalDateTime departureAt;
    @NotNull
    private LocalDateTime arrivalAt;
    @NotNull
    private String price;


}

