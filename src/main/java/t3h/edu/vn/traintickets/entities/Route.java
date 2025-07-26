package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "route")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departureId", nullable = false)
    private t3h.edu.vn.traintickets.entities.Station departure;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrivalId", nullable = false)
    private t3h.edu.vn.traintickets.entities.Station arrival;

//    @NotNull
//    @Column(name = "duration", nullable = false)
//    private LocalTime duration;

    @NotNull
    @Column(name = "distanceKm", nullable = false)
    private Float distanceKm;

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

    public Station getDeparture() {
        return departure;
    }

    public void setDeparture(Station departure) {
        this.departure = departure;
    }

    public Station getArrival() {
        return arrival;
    }

    public void setArrival(Station arrival) {
        this.arrival = arrival;
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