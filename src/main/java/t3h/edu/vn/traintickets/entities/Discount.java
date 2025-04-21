package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "discount")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 30)
    @NotNull
    @Column(name = "code", nullable = false, length = 30, unique = true)
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

    @NotNull
    @Column(name = "expiredAt", nullable = false)
    private java.util.Date expiredAt;

    @NotNull
    @Column(name = "createdAt", nullable = false)
    private java.util.Date createdAt;

}
