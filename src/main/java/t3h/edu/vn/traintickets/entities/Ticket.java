package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    private BigDecimal price;

    @Enumerated(EnumType.STRING) // hoặc EnumType.ORDINAL nếu bạn dùng số
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "ticketType", nullable = false)
    private Byte ticketType;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "passengerName", length = 100)
    private String passengerName;

    @Column(name = "cccd", length = 20)
    private String cccd;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "ticket_code", unique = true, length = 50)
    private String ticketCode;

    @Column(name = "used")
    private Boolean used;

}