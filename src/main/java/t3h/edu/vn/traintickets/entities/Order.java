package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "orderCode", unique = true, nullable = false, length = 50)
    private String orderCode;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private t3h.edu.vn.traintickets.entities.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discountId")
    private Discount discount;

    @NotNull
    @Column(name = "totalAmount", nullable = false)
    private Float totalAmount;

    @NotNull
    @Column(name = "finalAmount", nullable = false)
    private Float finalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returnTripId")
    private t3h.edu.vn.traintickets.entities.Trip returnTrip;

    @NotNull
    @Column(name = "status", nullable = false)
    private Byte status;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderTicket> orderTickets;

    //getter & setter
    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public List<OrderTicket> getOrderTickets() {
        return orderTickets;
    }

    public void setOrderTickets(List<OrderTicket> orderTickets) {
        this.orderTickets = orderTickets;
    }

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

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public @NotNull Float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(@NotNull Float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public @NotNull Float getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(@NotNull Float finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Trip getReturnTrip() {
        return returnTrip;
    }

    public void setReturnTrip(Trip returnTrip) {
        this.returnTrip = returnTrip;
    }

    public @NotNull Byte getStatus() {
        return status;
    }

    public void setStatus(@NotNull Byte status) {
        this.status = status;
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