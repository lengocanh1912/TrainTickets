package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "userId", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "discountId", referencedColumnName = "id")
    private Discount discount;

    @NotNull
    @Column(name = "totalAmount", nullable = false)
    private Float totalAmount;

    @NotNull
    @Column(name = "finalAmount", nullable = false)
    private Float finalAmount;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private java.util.Date createdAt;

    @Column(name = "updatedAt")
    private java.util.Date updatedAt;

}
