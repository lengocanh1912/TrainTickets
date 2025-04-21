package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "train")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @NotNull
    @Min(1)
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
