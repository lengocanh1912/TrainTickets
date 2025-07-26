package t3h.edu.vn.traintickets.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TripDetailDto {
    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private String departureDate;
    private List<CoachDto> coaches;

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public List<CoachDto> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<CoachDto> coaches) {
        this.coaches = coaches;
    }
}
