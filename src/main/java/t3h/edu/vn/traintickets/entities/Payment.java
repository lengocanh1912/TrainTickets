package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @Size(max = 30)
    @NotNull
    @Column(name = "method", nullable = false, length = 30)
    private String method;

    @NotNull
    @Lob
    @Column(name = "status", nullable = false)
    private String status;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "paidAt")
    private Instant paidAt;

    @Size(max = 100)
    @Column(name = "transactionId", length = 100)
    private String transactionId;

}