package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.DailyRevenueDto;
import t3h.edu.vn.traintickets.dto.MonthlyRevenueDto;
import t3h.edu.vn.traintickets.dto.RevenueSummaryDto;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RevenueService {

    @Autowired
    private OrderRepository orderRepository;

    // ===== DOANH THU =====

    public List<MonthlyRevenueDto> getMonthlyRevenue(int year, OrderStatus status) {
        List<Object[]> data =
                orderRepository.getMonthlyRevenue(year, status);

        List<MonthlyRevenueDto> result = new ArrayList<>();
        for (Object[] row : data) {
            int month = ((Number) row[0]).intValue();
            BigDecimal revenue = (BigDecimal) row[1];
            result.add(new MonthlyRevenueDto(month, revenue));
        }
        return result;
    }

    public List<DailyRevenueDto> getDailyRevenue(int year, int month, OrderStatus status) {
        List<Object[]> data =
                orderRepository.getDailyRevenue(year, month, status);

        List<DailyRevenueDto> result = new ArrayList<>();
        for (Object[] row : data) {
            int day = ((Number) row[0]).intValue();
            BigDecimal revenue = (BigDecimal) row[1];
            result.add(new DailyRevenueDto(day, revenue));
        }
        return result;
    }


    public BigDecimal getTotalRevenue(int year) {
        return orderRepository
                .getTotalRevenueByYear(year, OrderStatus.PAID);
    }

    public BigDecimal getTotalRevenueOrderPaid() {
        return orderRepository.getTotalRevenueOrderPaid(OrderStatus.PAID);
    }


    public BigDecimal getAverageMonthlyRevenue(int year) {
        return getTotalRevenue(year)
                .divide(BigDecimal.valueOf(12), RoundingMode.HALF_UP);
    }

    public MonthlyRevenueDto getBestMonth(int year) {
        return getMonthlyRevenue(year,OrderStatus.PAID)
                .stream()
                .max(Comparator.comparing(MonthlyRevenueDto::getRevenue))
                .orElse(null);
    }

    public MonthlyRevenueDto getWorstMonth(int year) {
        return getMonthlyRevenue(year,OrderStatus.PAID)
                .stream()
                .min(Comparator.comparing(MonthlyRevenueDto::getRevenue))
                .orElse(null);
    }

    // ===== ĐƠN HỦY =====

    public BigDecimal getCancelledRevenue(int year) {
        return orderRepository
                .getCancelledRevenueByYear(year, OrderStatus.CANCELLED);
    }

    public RevenueSummaryDto getSummary(int year, Integer month) {

        RevenueSummaryDto dto = new RevenueSummaryDto();

        if (month == null) {
            // ===== THEO NĂM =====
            BigDecimal total = getTotalRevenue(year);
            BigDecimal cancelled = getCancelledRevenue(year);

            dto.setTotalRevenue(total);
            dto.setCancelledRevenue(cancelled);
            dto.setAvgRevenue(getAverageMonthlyRevenue(year));

            MonthlyRevenueDto best = getBestMonth(year);
            MonthlyRevenueDto worst = getWorstMonth(year);

            dto.setBestMonth(best != null ? best.getMonth() : null);
            dto.setWorstMonth(worst != null ? worst.getMonth() : null);

        } else {
            // ===== THEO THÁNG =====
            List<DailyRevenueDto> daily =
                    getDailyRevenue(year, month, OrderStatus.PAID);

            BigDecimal total = daily.stream()
                    .map(DailyRevenueDto::getRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal cancelled =
                    orderRepository.getCancelledRevenueByMonth(
                            year, month, OrderStatus.CANCELLED
                    );

            dto.setTotalRevenue(total);
            dto.setCancelledRevenue(cancelled);

            // theo tháng thì mấy cái này không có ý nghĩa
            dto.setAvgRevenue(BigDecimal.ZERO);
            dto.setBestMonth(null);
            dto.setWorstMonth(null);
        }

        return dto;
    }

}

