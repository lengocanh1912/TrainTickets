package t3h.edu.vn.traintickets.entities;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "route",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"departureId", "arrivalId"})}
)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departureId", nullable = false)
    private Station departure;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrivalId", nullable = false)
    private Station arrival;

    @NotNull
    @Column(name = "duration", nullable = false)
    private Duration duration;

    @NotNull
    @Column(name = "distanceKm", nullable = false)
    private Float distanceKm;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}

