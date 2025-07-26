package t3h.edu.vn.traintickets.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class OrderGroupedTicketDto  {
    private Long id;
    private String userFullname;
    private String orderCode;
    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private byte status;
    private Map<String, List<TicketDto>> groupedTickets;

    // Getters & Setters


    public String getUserFullname() {
        return userFullname;
    }

    public void setUserFullname(String userFullname) {
        this.userFullname = userFullname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
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

    public Map<String, List<TicketDto>> getGroupedTickets() {
        return groupedTickets;
    }

    public void setGroupedTickets(Map<String, List<TicketDto>> groupedTickets) {
        this.groupedTickets = groupedTickets;
    }
}

