package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
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
    private Float price;

    @NotNull
    @Column(name = "status", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean status;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "updatedAt")
    private Instant updatedAt;

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

    public @NotNull Float getPrice() {
        return price;
    }

    public void setPrice(@NotNull Float price) {
        this.price = price;
    }

    public @NotNull boolean getStatus() {
        return status;
    }

    public void setStatus(@NotNull boolean status) {
        this.status = status;
    }

    public @NotNull Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}