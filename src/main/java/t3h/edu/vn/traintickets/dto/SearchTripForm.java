package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchTripForm {
    private String tripType; // "oneway" hoặc "roundtrip"
    private String departureName;
    private String arrivalName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    private int adultQuantity;    // Người lớn (11 - 59 tuổi)
    private int childQuantity;    // Trẻ em (6 - 10 tuổi)
    private int seniorQuantity;   // Người cao tuổi (60+)
    private int studentQuantity;  // Sinh viên (có thẻ SV)

    // Tính tổng vé từ từng loại vé
    public int getTicketQuantity() {
        return adultQuantity + childQuantity + seniorQuantity + studentQuantity;
    }

}