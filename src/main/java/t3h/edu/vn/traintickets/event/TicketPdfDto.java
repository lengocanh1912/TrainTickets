package t3h.edu.vn.traintickets.event;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


public record TicketPdfDto(
        Long ticketId,
        String ticketCode,

        String passengerName,
        String cccd,
        LocalDate birthday,
        String address,
        Byte  ticketType,

        String departureStation,
        String arrivalStation,

        String trainName,
        LocalDateTime departureAt,
        LocalDateTime arrivalAt,

        String coachCode,
        String seatCode,

        BigDecimal price
) {}

