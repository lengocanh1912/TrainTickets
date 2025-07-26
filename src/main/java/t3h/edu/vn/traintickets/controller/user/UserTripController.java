package t3h.edu.vn.traintickets.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.*;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.repository.*;
import t3h.edu.vn.traintickets.service.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/trip")
public class UserTripController {

    @Autowired
    TripService tripService;
    @Autowired
    TripDtoService tripDtoService;
    @Autowired
    private CoachService coachService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private UserService userService;
    @Autowired
    private TrainService trainService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderTicketRepository orderTicketRepository;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("menu", "trip");
    }

    @GetMapping("/search")
    public String searchTripsGet(@ModelAttribute SearchTripForm form, Model model) {
        int totalTicket = form.getAdultQuantity() + form.getChildQuantity()
                + form.getStudentQuantity() + form.getSeniorQuantity();

        System.out.println("Người lớn: " + form.getAdultQuantity());
        System.out.println("Trẻ em: " + form.getChildQuantity());

        // Gọi chuyến đi
        List<TripDto> departureTrips = tripDtoService.findTripsByStationNames(
                form.getDepartureName(),
                form.getArrivalName(),
                form.getDepartureDate(),
                totalTicket // tổng số vé cần tìm
        );

        model.addAttribute("tripType", form.getTripType());
        model.addAttribute("departureTrips", departureTrips);
        model.addAttribute("ticketQuantity", totalTicket);
        model.addAttribute("searchTripForm", form); // để dùng lại trong modal
        model.addAttribute("totalTicket", totalTicket); // để dùng lại trong modal


        if (departureTrips.isEmpty()) {
            model.addAttribute("image", "/img/sold-out.png");
            model.addAttribute("message", "Không có vé cho ngày bạn đã chọn.");
            model.addAttribute("message1", "Vui lòng chọn một ngày khác hoặc kiểm tra lịch trình.");
        }

        // Nếu là khứ hồi thì tìm chuyến về
        if ("roundtrip".equalsIgnoreCase(form.getTripType()) && form.getReturnDate() != null) {
            List<TripDto> returnTrips = tripDtoService.findTripsByStationNames(
                    form.getArrivalName(),
                    form.getDepartureName(),
                    form.getReturnDate(),
                    totalTicket
            );

            model.addAttribute("returnTrips", returnTrips);

            if (returnTrips.isEmpty()) {
                model.addAttribute("imageReturn", "/img/sold-out.png");
                model.addAttribute("messageReturn", "Không có vé chiều về.");
            }
        }

        return "user/trip_listSearch";
    }


    @PostMapping("/search")
    public String searchTripsPost(@ModelAttribute SearchTripForm form, Model model) {
        // Tìm chuyến đi (bắt buộc)
        List<TripDto> departureTrips = tripDtoService.findTripsByStationNames(
                form.getDepartureName(),
                form.getArrivalName(),
                form.getDepartureDate(),
                form.getTicketQuantity()
        );

        model.addAttribute("tripType", form.getTripType());
        model.addAttribute("departureTrips", departureTrips);
        model.addAttribute("ticketQuantity", form.getTicketQuantity());

        if (departureTrips.isEmpty()) {
            model.addAttribute("image", "/img/sold-out.png");
            model.addAttribute("message", "Không có vé cho ngày bạn đã chọn.");
            model.addAttribute("message1", "Vui lòng chọn một ngày khác hoặc kiểm tra lịch trình.");
        }

        // Nếu là khứ hồi thì tìm chuyến về
        if ("roundtrip".equalsIgnoreCase(form.getTripType()) && form.getReturnDate() != null) {
            List<TripDto> returnTrips = tripDtoService.findTripsByStationNames(
                    form.getArrivalName(), // đảo chiều ga
                    form.getDepartureName(),
                    form.getReturnDate(),
                    form.getTicketQuantity()
            );

            model.addAttribute("returnTrips", returnTrips);

            if (returnTrips.isEmpty()) {
                model.addAttribute("imageReturn", "/img/sold-out.png");
                model.addAttribute("messageReturn", "Không có vé chiều về.");
            }
        }
        return "user/trip_listSearch"; // Giao diện kết quả
    }

//    @PostMapping("/booking")
//    public String bookTickets(@RequestBody BookingRequest request, Principal principal, RedirectAttributes redirectAttributes) {
//        // 1. Lấy user đang đăng nhập
//        String username = principal.getName();
//        User user = userRepository.findByUsername(username);
//
//        // 2. Tạo vé và lưu vào DB
//        List<Ticket> savedTickets = new ArrayList<>();
//        for (int i = 0; i < request.getSeatIds().size(); i++) {
//            Long seatId = request.getSeatIds().get(i);
//            Float price = request.getPrices().get(i);
//            Byte ticketType = request.getTicketTypes().get(i);
//
//            Ticket ticket = new Ticket();
//            ticket.setUser(user);
//            ticket.setTrip(tripRepository.findById(request.getTripId()).orElseThrow());
//            ticket.setSeat(seatRepository.findById(seatId).orElseThrow());
//            ticket.setPrice(price);
//            ticket.setStatus((byte) 0); // chưa thanh toán
//            ticket.setTicketType(ticketType);
//            ticket.setCreatedAt(LocalDateTime.now());
//
//            savedTickets.add(ticketRepository.save(ticket));
//        }
//
//        // 3. Tạo đơn hàng
//        Order order = new Order();
//        order.setUser(user);
//        float total = savedTickets.stream().map(Ticket::getPrice).reduce(0f, Float::sum);
//        order.setTotalAmount(total);
//        order.setFinalAmount(total); // chưa áp mã giảm giá
//        order.setStatus((byte) 0); // chờ thanh toán
//        order.setCreatedAt(LocalDateTime.now());
//
//        Order savedOrder = orderRepository.save(order);
//
//        // 4. Gán vé vào đơn hàng (order_ticket)
//        for (Ticket ticket : savedTickets) {
//            OrderTicket ot = new OrderTicket();
//            ot.setOrder(savedOrder);
//            ot.setTicket(ticket);
//            orderTicketRepository.save(ot);
//        }
//
//        // 5. Chuyển hướng đến trang thanh toán
//        redirectAttributes.addFlashAttribute("message", "Vui lòng thanh toán để hoàn tất.");
//        return "redirect:/user/checkout/" + savedOrder.getId();
//    }


    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("menu", "trip");
        model.addAttribute("trips", tripDtoService.getAllTripDtos());
        return "/user/trip_list";
    }

    @GetMapping("/detail")
    public String tripDetail(@RequestParam("id") Long id, Model model) {
        List<TripDto> tripDtoDetail = tripDtoService.findByIdDto(id);
        model.addAttribute("trips", tripDtoDetail);
        return "/user/trip_detail";
    }

//    @GetMapping("/trip_booking/{id}")
//    public String viewPayment(@PathVariable Long id, Model model) {
//        Order order = orderService.findById(id);
//        if (order == null) {
//            System.out.println("ko thấy đơn hàng");
//        }
//
//        OrderPaymentDto dto = orderService.getOrderPaymentDto(order);
//        model.addAttribute("order", dto);
//
//        return "user/payment";
//    }
    @GetMapping("/trip_booking/{id}")
    public String viewPayment(@PathVariable Long id, Model model) {
        OrderPaymentDto dto = orderService.getOrderPaymentDtoById(id);
        model.addAttribute("order", dto);
        return "user/payment";
    }








}
