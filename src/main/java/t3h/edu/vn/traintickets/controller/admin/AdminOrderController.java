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

//    @GetMapping("list")
//    @ResponseBody
//    public Object list(Model model,
//                       @RequestParam(defaultValue = "0  ") Integer page,
//                       @RequestParam(defaultValue = "5") Integer perpage) {
//        return orderService.paging(page, perpage);
//    }
//    @GetMapping("view")
//    public String view(Model model,
//                       @RequestParam(defaultValue = "0") Integer page,
//                       @RequestParam(defaultValue = "5") Integer perpage) {
//        model.addAttribute("page", orderService.paging(page, perpage));
//        model.addAttribute("path", "/admin/order/view");
//        return "admin/order/view";
//    }

//    @GetMapping("/view")
//    public String viewUserOrders1(@ModelAttribute("currentUser") User currentUser, Model model) {
//        if (currentUser == null) {
//            return "redirect:/login";
//        }
//
//        List<OrderGroupedTicketDto> orders = orderService.getGroupedOrderTickets(currentUser.getId());
//        model.addAttribute("orders", orders);
//
//        return "/admin/order/view"; // Sẽ render đầy đủ ở Thymeleaf
//    }
    @GetMapping("/total_revenue")
    public ResponseEntity<Double> getTotalRevenue() {
        double total = orderService.getTotalRevenue();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/admin/order/list")
    @ResponseBody
    public Page<OrderGroupedTicketDto> list(@RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "5") Integer perpage) {
        return orderService.pagingGroupedOrders(page, perpage);
    }
    @GetMapping("/view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        Page<OrderGroupedTicketDto> paged = orderService.pagingGroupedOrders(page, perpage);
        model.addAttribute("page", paged);
        model.addAttribute("path", "/admin/order/view");
        return "admin/order/view";
    }

}
