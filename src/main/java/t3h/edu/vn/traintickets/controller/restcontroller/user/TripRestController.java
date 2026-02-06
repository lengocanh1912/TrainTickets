package t3h.edu.vn.traintickets.controller.restcontroller.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.BookingRequest;
import t3h.edu.vn.traintickets.dto.TripDetailDto;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.repository.*;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.TripDtoService;

import java.math.BigDecimal;
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
    public ResponseEntity<?> bookTickets(@RequestBody BookingRequest request,
                                         Principal principal) {
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
                return ResponseEntity.badRequest().body(Map.of("error",
                        "User không tồn tại"));
            }
            System.out.println("✅ User found: " + user.getId());

            // 2. Kiểm tra Trip có tồn tại không
            Optional<Trip> tripOpt = tripRepository.findById(request.getTripId());
            if (!tripOpt.isPresent()) {
                System.err.println("❌ Trip not found: " + request.getTripId());
                return ResponseEntity.badRequest().body(Map.of("error",
                        "Chuyến đi không tồn tại"));
            }
            Trip trip = tripOpt.get();
            System.out.println("✅ Trip found: " + trip.getId());

            // 3. Tạo danh sách vé
            List<Ticket> savedTickets = new ArrayList<>();
            for (int i = 0; i < request.getSeatIds().size(); i++) {
                Long seatId = request.getSeatIds().get(i);
                Float price = request.getPrices().get(i);
                Byte ticketType = request.getTicketTypes().get(i);

                Optional<Seat> seatOpt = seatRepository.findById(seatId);
                if (!seatOpt.isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Ghế không tồn tại: " + seatId));
                }
                Seat seat = seatOpt.get();

                // 🔹 Kiểm tra ticket đã đặt ACTIVE (PENDING/PAID) chưa
                boolean isBooked = ticketRepository.existsBySeatAndTripAndStatusIn(
                        seat, trip, List.of(TicketStatus.PENDING, TicketStatus.PAID)
                );
                if (isBooked) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Ghế " + seat.getSeatCode() + " đã được đặt"));
                }

                // 🔹 Tái sử dụng ticket CANCELLED nếu có
                Optional<Ticket> oldTicketOpt = ticketRepository.findBySeatAndTripAndStatus(seat,
                        trip, TicketStatus.CANCELLED);
                Ticket ticket;
                if (oldTicketOpt.isPresent()) {
                    ticket = oldTicketOpt.get();
                    ticket.setStatus(TicketStatus.PENDING);
                    ticket.setPrice(price);
                    ticket.setTicketType(ticketType);
                    ticket.setUpdatedAt(LocalDateTime.now());

                    // ✅ FIX: Xóa các OrderTicket cũ của ticket trước khi gán vào đơn mới
                    orderTicketRepository.deleteByTicketId(ticket.getId());
                    System.out.println("🔹 Old OrderTicket deleted for ticket: " + ticket.getId());
                } else {
                    // Tạo ticket mới khi chưa có ticket nào
                    ticket = new Ticket();
                    ticket.setUser(user);
                    ticket.setTrip(trip);
                    ticket.setSeat(seat);
                    ticket.setPrice(price);
                    ticket.setStatus(TicketStatus.PENDING);
                    ticket.setTicketType(ticketType);
                    ticket.setCreatedAt(LocalDateTime.now());
                }

                Ticket savedTicket = ticketRepository.save(ticket);
                savedTickets.add(savedTicket);
            }


            // 4. Tạo đơn hàng
            Order order = new Order();
            order.setUser(user);
            BigDecimal total = savedTickets.stream()
                    .map(ticket -> BigDecimal.valueOf(ticket.getPrice())) // chuyển từng giá vé từ float → BigDecimal
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);
            order.setFinalAmount(total);
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setOrderCode(orderService.generateOrderCode());
            // 🔹 Set thời gian giữ ghế, ví dụ 8 phút
            order.setHoldUntil(LocalDateTime.now().plusMinutes(8));

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


