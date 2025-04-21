package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "seat",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"coachId", "seatCode"})}
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachId", nullable = false)
    private Coach coach;

    @NotNull
    @Size(max = 10)
    @Column(name = "seatCode", nullable = false, length = 10)
    private String seatCode;
}

