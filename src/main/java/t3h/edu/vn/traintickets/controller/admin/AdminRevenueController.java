package t3h.edu.vn.traintickets.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.DailyRevenueDto;
import t3h.edu.vn.traintickets.dto.MonthlyRevenueDto;
import t3h.edu.vn.traintickets.dto.RevenueSummaryDto;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.service.RevenueService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/revenue")
public class AdminRevenueController {

    @Autowired
    private RevenueService revenueService;


    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "revenue");
    }

    @GetMapping("/view")
    public String viewRevenuePage(
            @RequestParam(defaultValue = "2025") int year,
            Model model
    ) {
        model.addAttribute("year", year);
        model.addAttribute("monthlyRevenue", revenueService.getMonthlyRevenue(year,OrderStatus.PAID));
        model.addAttribute("totalRevenue", revenueService.getTotalRevenue(year));
        model.addAttribute("avgRevenue", revenueService.getAverageMonthlyRevenue(year));
        model.addAttribute("bestMonth", revenueService.getBestMonth(year));
        model.addAttribute("worstMonth", revenueService.getWorstMonth(year));
        model.addAttribute("cancelledRevenue", revenueService.getCancelledRevenue(year));

        return "admin/revenue/view";
    }


    @GetMapping("/daily")
    @ResponseBody
    public List<DailyRevenueDto> getDailyRevenue(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam OrderStatus status,
            Model model
    ) {
        model.addAttribute("year", year);
        model.addAttribute("monthlyRevenue", revenueService.getMonthlyRevenue(year,OrderStatus.PAID));
        model.addAttribute("totalRevenue", revenueService.getTotalRevenue(year));
        model.addAttribute("avgRevenue", revenueService.getAverageMonthlyRevenue(year));
        model.addAttribute("bestMonth", revenueService.getBestMonth(year));
        model.addAttribute("worstMonth", revenueService.getWorstMonth(year));
        model.addAttribute("cancelledRevenue", revenueService.getCancelledRevenue(year));

        return revenueService.getDailyRevenue(year, month, status);
    }

    @GetMapping("/monthly")
    @ResponseBody
    public List<MonthlyRevenueDto> getMonthlyRevenue(
            @RequestParam int year,
            @RequestParam OrderStatus status,
            Model model
    ) {
        model.addAttribute("year", year);
        model.addAttribute("monthlyRevenue", revenueService.getMonthlyRevenue(year,OrderStatus.PAID));
        model.addAttribute("totalRevenue", revenueService.getTotalRevenue(year));
        model.addAttribute("avgRevenue", revenueService.getAverageMonthlyRevenue(year));
        model.addAttribute("bestMonth", revenueService.getBestMonth(year));
        model.addAttribute("worstMonth", revenueService.getWorstMonth(year));
        model.addAttribute("cancelledRevenue", revenueService.getCancelledRevenue(year));

        return revenueService.getMonthlyRevenue(year, status);
    }

    @GetMapping("/summary")
    @ResponseBody
    public RevenueSummaryDto getSummary(
            @RequestParam int year,
            @RequestParam(required = false) Integer month
    ) {
        return revenueService.getSummary(year, month);
    }

}
