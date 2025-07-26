package t3h.edu.vn.traintickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
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
    private Float price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull Long getTrainId() {
        return trainId;
    }

    public void setTrainId(@NotNull Long trainId) {
        this.trainId = trainId;
    }

    public @NotNull Long getRouteId() {
        return routeId;
    }

    public void setRouteId(@NotNull Long routeId) {
        this.routeId = routeId;
    }

    public @NotNull LocalDateTime getDepartureAt() {
        return departureAt;
    }

    public void setDepartureAt(@NotNull LocalDateTime departureAt) {
        this.departureAt = departureAt;
    }

    public @NotNull LocalDateTime getArrivalAt() {
        return arrivalAt;
    }

    public void setArrivalAt(@NotNull LocalDateTime arrivalAt) {
        this.arrivalAt = arrivalAt;
    }

    public @NotNull Float getPrice() {
        return price;
    }

    public void setPrice(@NotNull Float price) {
        this.price = price;
    }
}

