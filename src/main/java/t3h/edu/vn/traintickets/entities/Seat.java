package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coachId", nullable = false)
    private Coach coach;

    @Size(max = 10)
    @NotNull
    @Column(name = "seatCode", nullable = false, length = 10)
    private String seatCode;

//    @Size(max = 20)
//    @ColumnDefault("'standard'")
//    @Column(name = "type", length = 20)
//    private String type;

    @Column(name = "type", nullable = false, length = 20)
    private String type;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull Coach getCoach() {
        return coach;
    }

    public void setCoach(@NotNull Coach coach) {
        this.coach = coach;
    }

    public @Size(max = 10) @NotNull String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(@Size(max = 10) @NotNull String seatCode) {
        this.seatCode = seatCode;
    }

    public @Size(max = 20) String getType() {
        return type;
    }

    public void setType(@Size(max = 20) String type) {
        this.type = type;
    }
}