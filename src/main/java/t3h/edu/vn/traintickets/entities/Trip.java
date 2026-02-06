package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import t3h.edu.vn.traintickets.enums.TripState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "trip")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainId", nullable = false)
    private Train train;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "routeId", nullable = false)
    private Route route;

    @NotNull
    @Column(name = "departureAt", nullable = false)
    private LocalDateTime departureAt;

    @NotNull
    @Column(name = "arrivalAt", nullable = false)
    private LocalDateTime arrivalAt;

    @NotNull
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TripState status;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull Train getTrain() {
        return train;
    }

    public void setTrain(@NotNull Train train) {
        this.train = train;
    }

    public @NotNull Route getRoute() {
        return route;
    }

    public void setRoute(@NotNull Route route) {
        this.route = route;
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


    public @NotNull BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@NotNull BigDecimal price) {
        this.price = price;
    }

    public @NotNull TripState getStatus() {
        return status;
    }

    public void setStatus(@NotNull TripState status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public @NotNull LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}