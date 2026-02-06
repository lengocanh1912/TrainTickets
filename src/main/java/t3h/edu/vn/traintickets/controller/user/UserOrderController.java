package t3h.edu.vn.traintickets.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import t3h.edu.vn.traintickets.dto.OrderGroupedTicketDto;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.enums.CancelType;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.UserService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/user/order")
public class UserOrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderRepository orderRepository;


    @GetMapping("/myOrders")
    public String getOder(@RequestParam Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        return "user/myOrder";
    }


    @GetMapping("/list")
    @ResponseBody
    public Page<OrderGroupedTicketDto> list(@RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "2") Integer perpage) {
        return orderService.pagingGroupedOrders(page, perpage);
    }

    @GetMapping("/view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "2") Integer perpage) {
        Page<OrderGroupedTicketDto> paged = orderService.pagingGroupedOrders(page, perpage);
        model.addAttribute("page", paged);
        model.addAttribute("path", "/user/order/view");
        model.addAttribute("OrderStatus", OrderStatus.class);
        model.addAttribute("TicketStatus", TicketStatus.class);
        return "/user/grouped_order_list";
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(id);
        if (order != null && order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelType(CancelType.USER_CANCELLED);
            order.setCancelNote("Người dùng tự hủy đơn");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("message", "Đã hủy đơn thành công!");
        }
        return "redirect:/user/orders";
    }

}
