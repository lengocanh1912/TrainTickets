package t3h.edu.vn.traintickets.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.*;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.entities.TicketLog;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.TicketService;
import t3h.edu.vn.traintickets.service.TrainService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/ticket")
public class AdminTicketController {
    @Autowired
    private TicketService ticketService;
    @Autowired
    private TrainService trainService;
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
        model.addAttribute("TicketStatus", TicketStatus.class);
        model.addAttribute("trains", trainService.findAll());

        return "admin/ticket/view";
    }

    @GetMapping("/search")
    public String searchTickets(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) TicketStatus status,
                                @RequestParam(required = false) Long trainId,
                                @RequestParam(required = false) LocalDate departureDate,
                                @RequestParam(required = false) String departure,
                                @RequestParam(required = false) String arrival,
                                Model model) {

        List<TicketAdminDto> tickets = ticketService.searchTickets(keyword,
                status,
                trainId,
                departureDate,
                departure,
                arrival );

        model.addAttribute("tickets", tickets);
        model.addAttribute("TicketStatus", TicketStatus.class);
        model.addAttribute("trains", trainService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("trainId", trainId);
        model.addAttribute("departureDate", departureDate);
        model.addAttribute("departure", departure);
        model.addAttribute("arrival", arrival);

        return "admin/ticket/search";
    }

    @GetMapping("/detail")
    public String viewTicketDetail(@RequestParam("id") Long id, Model model) {
        TicketDetailDto dto = ticketService.getTicketDetail(id);
        if (dto == null) {
            model.addAttribute("error", "Không tìm thấy vé!");
            return "admin/error";
        }

        List<TicketLog> logs = ticketService.getTicketLogs(id);

        model.addAttribute("ticket", dto);
        model.addAttribute("logs", logs);
        return "admin/ticket/detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(
            @RequestParam("ticket_id") Long ticketId,
            @RequestParam TicketStatus newStatus,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        ticketService.updateTicketStatus(ticketId, newStatus, principal.getName(), reason);
        redirectAttributes.addFlashAttribute("message",
                "Cập nhật trạng thái vé thành công!");
        return "redirect:/admin/ticket/detail?id=" + ticketId;
    }

    @GetMapping("/cancel")
    public String showCancelPage(@RequestParam("id") Long id, Model model) {
        model.addAttribute("ticket", ticketService.getTicketViewById(id));
        return "admin/ticket/cancel";
    }

    @PostMapping("/cancel")
    public String cancelTicket(@ModelAttribute TicketCancelDto dto, RedirectAttributes ra) {
        try {
            ticketService.cancelTicket(dto);
            ra.addFlashAttribute("message", "Hủy vé thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/ticket/cancel?id=" + dto.getTicketId();
    }

    @PostMapping("/refund")
    public String refundTicket(@ModelAttribute TicketRefundDto dto, RedirectAttributes ra) {
        try {
            ticketService.refundTicket(dto);
            ra.addFlashAttribute("message", "Hoàn tiền thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/ticket/cancel?id=" + dto.getTicketId();
    }
}

