package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "discount")
public class Discount {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 30)
    @NotNull
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Size(max = 100)
    @Column(name = "description", length = 100)
    private String description;

    @Size(max = 20)
    @NotNull
    @Column(name = "discountType", nullable = false, length = 20)
    private String discountType;

    @NotNull
    @Column(name = "value", nullable = false)
    private Float value;

    @Column(name = "maxDiscount")
    private Float maxDiscount;

    @Column(name = "minOrderAmount")
    private Float minOrderAmount;

    @Column(name = "quantityAvailable")
    private Integer quantityAvailable;

    @ColumnDefault("1")
    @Column(name = "isActive")
    private Boolean isActive;

    @NotNull
    @Column(name = "expiredAt", nullable = false)
    private Instant expiredAt;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

}