package t3h.edu.vn.traintickets.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orderId", referencedColumnName = "id", nullable = false)
    private Order order;

    @Column(name = "method", nullable = false, length = 30)
    private String method;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "paidAt")
    private java.util.Date paidAt;
}

