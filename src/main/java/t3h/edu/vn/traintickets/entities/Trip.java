package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainId", nullable = false)
    private Train train;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routeId", nullable = false)
    private Route route;

    @NotNull
    @Column(name = "departureAt", nullable = false)
    private LocalDateTime departureAt;

    @NotNull
    @Column(name = "arrivalAt", nullable = false)
    private LocalDateTime arrivalAt;

    @NotNull
    @PositiveOrZero
    @Column(name = "price", nullable = false)
    private Float price;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}

