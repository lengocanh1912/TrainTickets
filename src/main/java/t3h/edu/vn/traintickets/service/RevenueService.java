package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.MonthlyRevenueDto;
import t3h.edu.vn.traintickets.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RevenueService {

    @Autowired
    private OrderRepository orderRepository;

    public List<MonthlyRevenueDto> getMonthlyRevenue(int year) {
        List<Object[]> data = orderRepository.getMonthlyRevenue(year);
        List<MonthlyRevenueDto> result = new ArrayList<>();

        for (Object[] row : data) {
            int month = (Integer) row[0];
            float revenue = ((Double) row[1]).floatValue();
            result.add(new MonthlyRevenueDto(month, revenue));
        }

        return result;
    }

    public float getTotalRevenue(int year) {
        Float total = orderRepository.getTotalRevenueByYear(year);
        return total != null ? total : 0f;
    }
}
