package t3h.edu.vn.traintickets.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.OrderGroupedTicketDto;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "order");
    }

    @GetMapping("/search")
    public String searchOrders(@RequestParam("keyword") String keyword, Model model) {
        List<OrderGroupedTicketDto> result = orderService.searchGroupedOrders(keyword);
        model.addAttribute("orders", result);
        return "admin/order/search"; // hoặc trang list.html nếu cùng template
    }

    @GetMapping("/total_revenue")
    public ResponseEntity<Double> getTotalRevenue() {
        double total = orderService.getTotalRevenue();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/list")
    @ResponseBody
    public Page<OrderGroupedTicketDto> list(@RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "5") Integer perpage) {
        return orderService.pagingOrders(page, perpage);
    }

    @GetMapping("/view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        Page<OrderGroupedTicketDto> paged = orderService.pagingOrders(page, perpage);
        model.addAttribute("page", paged);
        model.addAttribute("path", "/admin/order/view");
        model.addAttribute("OrderStatus", OrderStatus.class);
        return "admin/order/view";
    }

}
