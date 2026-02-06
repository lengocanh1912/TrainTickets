package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import t3h.edu.vn.traintickets.enums.CancelType;
import t3h.edu.vn.traintickets.enums.OrderStatus;

import java.math.BigDecimal;
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
    private BigDecimal totalAmount;

    @NotNull
    @Column(name = "finalAmount", nullable = false)
    private BigDecimal finalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returnTripId")
    private t3h.edu.vn.traintickets.entities.Trip returnTrip;

    @Enumerated(EnumType.STRING) // hoặc EnumType.ORDINAL nếu bạn dùng số
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime expiresAt; // thời gian hết hạn giữ vé

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CancelType cancelType; // loại hủy

    @Column(length = 255)
    private String cancelNote; // ghi chú thêm

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderTicket> orderTickets;

    @Column(name = "transaction_code", unique = true)
    private String transactionCode;

    @Column(name = "hold_until")
    private LocalDateTime holdUntil; // thời điểm hết hạn giữ ghế cho cả Order

    //contact
    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    }