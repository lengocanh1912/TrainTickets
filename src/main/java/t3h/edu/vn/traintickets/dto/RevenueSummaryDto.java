package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueSummaryDto {
    private BigDecimal totalRevenue;
    private BigDecimal cancelledRevenue;
    private BigDecimal avgRevenue;
    private Integer bestMonth;
    private Integer worstMonth;

}
