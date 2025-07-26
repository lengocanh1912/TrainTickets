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

//    @NotNull
//    private LocalTime duration;

    @NotNull
    private Float distanceKm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

//    public @NotNull LocalTime getDuration() {
//        return duration;
//    }
//
//    public void setDuration(@NotNull LocalTime duration) {
//        this.duration = duration;
//    }

    public @NotNull Float getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(@NotNull Float distanceKm) {
        this.distanceKm = distanceKm;
    }
}



