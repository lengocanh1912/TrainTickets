package t3h.edu.vn.traintickets.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.RateState;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class OrderGroupedTicketDto  {

    private Long id;
    private Long tripId;
    private String userFullname;
    private String orderCode;

    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    private OrderStatus status;
    private TicketStatus ticketStatus;
    private RateState rateStatus;

    private Map<String, List<TicketDto>> groupedTickets;

}

