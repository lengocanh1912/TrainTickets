package t3h.edu.vn.traintickets.controller.restcontroller.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.BookingRequest;
import t3h.edu.vn.traintickets.dto.TripDetailDto;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.repository.*;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.TripDtoService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/trips")
public class TripRestController {
    @Autowired
    private TripDtoService tripDtoService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderTicketRepository orderTicketRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}/detail")
    public TripDetailDto getTripDetail(@PathVariable Long id) {
        System.out.println("Trip ID nhận được: " + id);
        return tripDtoService.getTripDetail(id);
    }

    @PostMapping("/booking")
    @PreAuthorize("isAuthenticated()") // hoặc hasAnyRole('USER', 'ADMIN')
    public ResponseEntity<?> bookTickets(@RequestBody BookingRequest request, Principal principal) {
        try {
            System.out.println("🔍 Booking request received: " + request);
            System.out.println("🔍 Trip ID: " + request.getTripId());
            System.out.println("🔍 Seat IDs: " + request.getSeatIds());
            System.out.println("🔍 Prices: " + request.getPrices());
            System.out.println("🔍 Ticket Types: " + request.getTicketTypes());

            // 1. Lấy user đang đăng nhập
            String username = principal.getName();
            System.out.println("🔍 Username: " + username);

            User user = userRepository.findByUsername(username);
            if (user == null) {
                System.err.println("❌ User not found: " + username);
                return ResponseEntity.badRequest().body(Map.of("error", "User không tồn tại"));
            }
            System.out.println("✅ User found: " + user.getId());

            // 2. Kiểm tra Trip có tồn tại không
            Optional<Trip> tripOpt = tripRepository.findById(request.getTripId());
            if (!tripOpt.isPresent()) {
                System.err.println("❌ Trip not found: " + request.getTripId());
                return ResponseEntity.badRequest().body(Map.of("error", "Chuyến đi không tồn tại"));
            }
            Trip trip = tripOpt.get();
            System.out.println("✅ Trip found: " + trip.getId());

            // 3. Tạo danh sách vé
            List<Ticket> savedTickets = new ArrayList<>();
            for (int i = 0; i < request.getSeatIds().size(); i++) {
                Long seatId = request.getSeatIds().get(i);
                Float price = request.getPrices().get(i);
                Byte ticketType = request.getTicketTypes().get(i);

                System.out.println("🔍 Processing seat " + i + ": ID=" + seatId + ", Price=" + price + ", Type=" + ticketType);

                // Kiểm tra Seat có tồn tại không
                Optional<Seat> seatOpt = seatRepository.findById(seatId);
                if (!seatOpt.isPresent()) {
                    System.err.println("❌ Seat not found: " + seatId);
                    return ResponseEntity.badRequest().body(Map.of("error", "Ghế không tồn tại: " + seatId));
                }
                Seat seat = seatOpt.get();
                System.out.println("✅ Seat found: " + seat.getId() + " - " + seat.getSeatCode());

                // Kiểm tra ghế đã được đặt chưa
                boolean isBooked = ticketRepository.existsBySeatAndTripAndStatusNot(seat, trip, (byte) 2); // 2 = cancelled
                if (isBooked) {
                    System.err.println("❌ Seat already booked: " + seat.getSeatCode());
                    return ResponseEntity.badRequest().body(Map.of("error", "Ghế " + seat.getSeatCode() + " đã được đặt"));
                }

                Ticket ticket = new Ticket();
                ticket.setUser(user);
                ticket.setTrip(trip);
                ticket.setSeat(seat);
                ticket.setPrice(price);
                ticket.setStatus((byte) 0); // chưa thanh toán
                ticket.setTicketType(ticketType);
                ticket.setCreatedAt(LocalDateTime.now());

                Ticket savedTicket = ticketRepository.save(ticket);
                System.out.println("✅ Ticket saved: " + savedTicket.getId());
                savedTickets.add(savedTicket);
            }

            // 4. Tạo đơn hàng
            Order order = new Order();
            order.setUser(user);
            float total = savedTickets.stream()
                    .map(Ticket::getPrice)
                    .reduce(0f, Float::sum);
            order.setTotalAmount(total);
            order.setFinalAmount(total);
            order.setStatus((byte) 0);
            order.setCreatedAt(LocalDateTime.now());
            order.setOrderCode(orderService.generateOrderCode());

            Order savedOrder = orderRepository.save(order);
            System.out.println("✅ Order saved: " + savedOrder.getId());

            // 5. Gán vé vào đơn hàng
            for (Ticket ticket : savedTickets) {
                OrderTicket ot = new OrderTicket();
                ot.setOrder(savedOrder);
                ot.setTicket(ticket);
                orderTicketRepository.save(ot);
            }

            System.out.println("✅ Booking completed successfully!");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đặt vé thành công. Vui lòng thanh toán.");
            response.put("redirectUrl", "/trainticket/user/trip/trip_booking/" + savedOrder.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Booking error: " + e.getMessage());
            e.printStackTrace(); // In full stack trace

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi đặt vé: " + e.getMessage()));
        }
    }

}


