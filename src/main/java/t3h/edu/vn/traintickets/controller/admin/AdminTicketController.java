package t3h.edu.vn.traintickets.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.TicketDto;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.service.TicketService;

import java.util.List;

@Controller
@RequestMapping("/admin/ticket")
public class AdminTicketController {
    @Autowired
    private TicketService ticketService;
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "ticket");
    }
    @GetMapping("list")
    @ResponseBody
    public Object list(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        return ticketService.paging(page, perpage);
    }
    @GetMapping("view1")
    public String view1(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        model.addAttribute("page", ticketService.paging(page, perpage));
        model.addAttribute("path", "/admin/ticket/view");
        return "admin/ticket/view";
    }
    @GetMapping("view")
    public String view(Model model,
                       @RequestParam(defaultValue = "0") Integer page,
                       @RequestParam(defaultValue = "5") Integer perpage) {
        Page<TicketDto> ticketDtos = ticketService.paging(page, perpage);
        model.addAttribute("page", ticketDtos);
        model.addAttribute("path", "/admin/ticket/view");
        return "admin/ticket/view";
    }
    @GetMapping("/search")
    public String searchTickets(@RequestParam("keyword") String keyword, Model model) {
        List<Ticket> tickets = ticketService.searchTickets(keyword);
        model.addAttribute("tickets", tickets);
        return "admin/ticket/search"; // hoặc "admin/ticket/list"
    }

}

