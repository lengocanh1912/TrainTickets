package t3h.edu.vn.traintickets.dto;


import lombok.*;


import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
    private Long id;
    private String seatCode;
    private boolean booked;
    private BigDecimal price;

    public SeatDto(Long id, String seatCode) {
        this.id = id;
        this.seatCode = seatCode;
    }
}



