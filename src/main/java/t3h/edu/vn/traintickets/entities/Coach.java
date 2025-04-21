package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "coach",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"trainId", "code"})}
)
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainId", nullable = false)
    private Train train;

    @NotNull
    @Size(max = 10)
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @NotNull
    @Size(max = 20)
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @NotNull
    @Min(1)
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
}


