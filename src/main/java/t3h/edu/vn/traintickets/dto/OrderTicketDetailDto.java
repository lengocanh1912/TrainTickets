package t3h.edu.vn.traintickets.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTicketDetailDto {
    private String orderCode;
    private String trainName;
    private String coachCode;
    private String seatCode;

    private String departureStation;
    private String arrivalStation;

    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;

    private Float  price;
    private Byte ticketType; // Người lớn, trẻ em, sinh viên...

    private Byte ticketStatus; // 0 = chưa thanh toán, 1 = đã thanh toán

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getCoachCode() {
        return coachCode;
    }

    public void setCoachCode(String coachCode) {
        this.coachCode = coachCode;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
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

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float  price) {
        this.price = price;
    }

    public Byte getTicketType() {
        return ticketType;
    }

    public void setTicketType(Byte ticketType) {
        this.ticketType = ticketType;
    }

    public Byte getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(Byte ticketStatus) {
        this.ticketStatus = ticketStatus;
    }
}

