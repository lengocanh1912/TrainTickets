package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketPaymentDto {
    private Long ticketId;

    // Toa – Ghế – Loại ghế
    private String coachName;
    private String seatNumber;
    private String seatType;

    private BigDecimal price;

    // 0: người lớn, 1: trẻ em, 2: sinh viên, 3: người già
    private Byte ticketType;
    private String ticketTypeLabel;

    // Form thông tin khách hàng
    private String passengerName;
    private String birthDate;
    private String cccd;
    private String phone;
    private String address;

}

