package t3h.edu.vn.traintickets.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import t3h.edu.vn.traintickets.dto.MonthlyRevenueDto;
import t3h.edu.vn.traintickets.service.RevenueService;

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
    public String viewRevenuePage(@RequestParam(defaultValue = "2025") int year, Model model) {
        List<MonthlyRevenueDto> monthlyData = revenueService.getMonthlyRevenue(year);
        float totalRevenue = revenueService.getTotalRevenue(year);

        model.addAttribute("year", year);
        model.addAttribute("monthlyRevenue", monthlyData);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin/revenue/view";
    }
}
