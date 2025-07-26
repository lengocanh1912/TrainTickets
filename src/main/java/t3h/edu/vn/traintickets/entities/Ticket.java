package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private t3h.edu.vn.traintickets.entities.User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tripId", nullable = false)
    private t3h.edu.vn.traintickets.entities.Trip trip;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seatId", nullable = false)
    private Seat seat;

    @NotNull
    @Column(name = "price", nullable = false)
    private Float price;

    @NotNull
    @Column(name = "status", nullable = false)
    private Byte status;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "ticketType", nullable = false)
    private Byte ticketType;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public @NotNull Seat getSeat() {
        return seat;
    }

    public void setSeat(@NotNull Seat seat) {
        this.seat = seat;
    }

    public @NotNull Float getPrice() {
        return price;
    }

    public void setPrice(@NotNull Float price) {
        this.price = price;
    }

    public @NotNull Byte getStatus() {
        return status;
    }

    public void setStatus(@NotNull Byte status) {
        this.status = status;
    }

    public @NotNull Byte getTicketType() {
        return ticketType;
    }

    public void setTicketType(@NotNull Byte ticketType) {
        this.ticketType = ticketType;
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
}