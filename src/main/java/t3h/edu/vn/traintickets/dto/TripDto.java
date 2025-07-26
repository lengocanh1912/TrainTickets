package t3h.edu.vn.traintickets.dto;

import ch.qos.logback.core.testUtil.DummyEncoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@ToString
public class TripDto implements Serializable {
    private Long id;
    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private Float price;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;

    public TripDto(
            Long id,
            String trainName,
            String departureStation,
            String arrivalStation,
            Float price,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt) {
        this.id = id;
        this.trainName = trainName;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.price = price;
        this.departureAt = departureAt;
        this.arrivalAt = arrivalAt;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getDepartureStation() { return departureStation; }
    public void setDepartureStation(String departureStation) { this.departureStation = departureStation; }

    public String getArrivalStation() { return arrivalStation; }
    public void setArrivalStation(String arrivalStation) { this.arrivalStation = arrivalStation; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public LocalDateTime getDepartureAt() {
        return departureAt;
    }

    public void setDepartureAt(LocalDateTime departureAt) {
        this.departureAt = departureAt;
    }

    public LocalDateTime getArrivalAt() {
        return arrivalAt;
    }

    public void setArrivalAt(LocalDateTime arrivalAt) {
        this.arrivalAt = arrivalAt;
    }


//    getDuration
    public String getDuration() {
        if (departureAt == null || arrivalAt == null || arrivalAt.isBefore(departureAt)) {
            return "Không hợp lệ";
        }

        Duration duration = Duration.between(departureAt, arrivalAt);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        return String.format("%d giờ %d phút", hours, minutes);
    }

}
