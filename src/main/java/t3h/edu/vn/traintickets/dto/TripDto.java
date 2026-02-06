package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import t3h.edu.vn.traintickets.utils.PriceUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripDto implements Serializable {
    private Long id;
    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private BigDecimal price;     // raw value: 300000
    private String displayPrice;  // formatted: "300.000 VND"
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;

    public TripDto(Long id, String trainName, String departureStation, String arrivalStation,
                   BigDecimal price, LocalDateTime departureAt, LocalDateTime arrivalAt) {
        this.id = id;
        this.trainName = trainName;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.price = price;
        this.displayPrice = PriceUtils.formatWithVND(price);
        this.departureAt = departureAt;
        this.arrivalAt = arrivalAt;
    }

    public String getDuration() {
        if (departureAt == null || arrivalAt == null) return "";
        Duration duration = Duration.between(departureAt, arrivalAt);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0 && minutes > 0)
            return String.format("%d giờ %d phút", hours, minutes);
        else if (hours > 0)
            return String.format("%d giờ", hours);
        else
            return String.format("%d phút", minutes);
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.displayPrice = PriceUtils.formatWithVND(price);
    }

}
