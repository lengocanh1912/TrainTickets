package t3h.edu.vn.traintickets.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.*;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.repository.*;
import t3h.edu.vn.traintickets.service.*;
import t3h.edu.vn.traintickets.service.booking.BookingService;
import t3h.edu.vn.traintickets.service.pdf_qr.TicketPdfService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user/trip")
public class UserTripController {

    @Autowired
    TripService tripService;
    @Autowired
    TripDtoService tripDtoService;


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
    @Autowired
    private TicketPdfService ticketPdfService;
    @Autowired
    private BookingService bookingService;

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

//    trả về trang thanh toán
    @GetMapping("/passengerInfo/{id}")
    public String viewPayment(@PathVariable Long id,
                              Model model,
                              Principal principal) {

        // 1. Lấy OrderPaymentDto từ service
        OrderPaymentDto dto = orderService.getOrderPaymentDtoById(id);

        if (dto == null) {
            throw new RuntimeException("Order not found with id = " + id);
        }

        model.addAttribute("order", dto);

        // 2. Lấy thông tin liên hệ từ Order hoặc User
        ContactInfo contactInfo = new ContactInfo();

        // Ưu tiên lấy từ Order trước (nếu đã lưu)
        if (dto.getContactName() != null && !dto.getContactName().isEmpty()) {
            contactInfo.setFullname(dto.getContactName());
            contactInfo.setPhoneNumber(dto.getContactPhone());
            contactInfo.setEmail(dto.getContactEmail());
        }

        // Nếu Order chưa có, lấy từ User đang đăng nhập
        else if (principal != null) {
            String username = principal.getName(); // Email hoặc username
            User user = userRepository.findByUsername(username);
            if (user != null) {
                contactInfo.setFullname(user.getFullname());
                contactInfo.setPhoneNumber(user.getPhoneNumber());
                contactInfo.setEmail(user.getEmail());
            }
        }

        model.addAttribute("contactInfo", contactInfo);

        // 3. Trả về view
        return "user/passengerInfo";
    }

    @GetMapping("/{id}/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long id)
            throws Exception {

        byte[] pdf = ticketPdfService.generateTicketPdf(id);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=ticket-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/rebook")
    public String rebook(
                        @RequestParam Long orderId,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {

        Long newOrderId = bookingService.createRebookOrder(orderId, principal.getName());

        if (newOrderId == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Một hoặc nhiều ghế đã được đặt lại, vui lòng chọn ghế khác");
            return "redirect:/user/order/view";
        }

        return "redirect:/user/trip/passengerInfo/" + newOrderId;
    }








}
