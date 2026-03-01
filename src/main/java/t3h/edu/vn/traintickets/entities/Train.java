package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import t3h.edu.vn.traintickets.enums.TrainState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "train")
public class Train {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 20)
    @NotNull
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotNull
    @Column(name = "capacity", nullable = false)
    private int capacity;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "train",
            fetch = FetchType.LAZY ,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Coach> coaches = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private TrainState state ;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Size(max = 50) @NotNull String getName() {
        return name;
    }

    public void setName(@Size(max = 50) @NotNull String name) {
        this.name = name;
    }

    public @Size(max = 20) @NotNull String getCode() {
        return code;
    }

    public void setCode(@Size(max = 20) @NotNull String code) {
        this.code = code;
    }

    public @NotNull int getCapacity() {
        return capacity;
    }

    public void setCapacity(@NotNull int capacity) {
        this.capacity = capacity;
    }

    public @NotNull LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Coach> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<Coach> coaches) {
        this.coaches = coaches;
    }

    public TrainState getState() {
        return state;
    }

    public void setState(TrainState state) {
        this.state = state;
    }

    public void addCoach(Coach coach) {
        coach.setTrain(this);
        this.coaches.add(coach);
    }

    public void clearCoaches() {
        this.coaches.clear();
    }

}