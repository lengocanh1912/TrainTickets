package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import t3h.edu.vn.traintickets.enums.TripState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripUpdateDto {
    @NotNull
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
    @NotNull
    private TripState status;

    public String getDepartureAtFormatted() {
        return departureAt != null
                ? departureAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                : "";
    }

    public String getArrivalAtFormatted() {
        return arrivalAt != null
                ? arrivalAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                : "";
    }

}
