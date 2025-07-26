package t3h.edu.vn.traintickets.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.OrderGroupedTicketDto;
import t3h.edu.vn.traintickets.dto.OrderTicketDetailDto;
import t3h.edu.vn.traintickets.entities.Order;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user/order")
public class UserOrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

//    @GetMapping("/user/checkout")
//    public String checkoutPage(@RequestParam Long orderId, Model model) {
//        Order order = orderService.findById(orderId);
//        model.addAttribute("order", order);
//        return "user/checkout"; // HTML trang thanh toán
//    }

    @GetMapping("/myOrders")
    public String getOder(@RequestParam Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        return "user/myOrder";
    }


    @GetMapping("/view1")
    public String viewUserOrders(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 @ModelAttribute("currentUser") User currentUser,
                                 Model model) {

        if (currentUser == null) {
            return "redirect:/login"; // hoặc trang lỗi nếu chưa đăng nhập
        }

        Page<OrderTicketDetailDto> orderPage = orderService.getUserOrderTickets(currentUser.getId(), page, size);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("pageSize", size);

        return "/user/order_list";
    }

    @GetMapping("/view")
    public String viewUserOrders1(@ModelAttribute("currentUser") User currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<OrderGroupedTicketDto> orders = orderService.getGroupedOrderTickets(currentUser.getId());
        model.addAttribute("orders", orders);

        return "/user/grouped_order_list"; // Sẽ render đầy đủ ở Thymeleaf
    }



}
