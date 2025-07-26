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

    // getter, setter

    public @NotNull Long getId() {
        return id;
    }

    public void setId(@NotNull Long id) {
        this.id = id;
    }

    public @NotNull Long getDepartureId() {
        return departureId;
    }

    public void setDepartureId(@NotNull Long departureId) {
        this.departureId = departureId;
    }

    public @NotNull Long getArrivalId() {
        return arrivalId;
    }

    public void setArrivalId(@NotNull Long arrivalId) {
        this.arrivalId = arrivalId;
    }

    public @NotNull Float getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(@NotNull Float distanceKm) {
        this.distanceKm = distanceKm;
    }
}
