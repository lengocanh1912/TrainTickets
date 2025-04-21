package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_ticket")
public class OrderTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orderId", referencedColumnName = "id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "ticketId", referencedColumnName = "id", nullable = false)
    private Ticket ticket;

    // No need for explicit UNIQUE constraint in Java, the database will enforce it
}

