package t3h.edu.vn.traintickets.service.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.dto.BookingRequest;
import t3h.edu.vn.traintickets.dto.BookingResponse;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.enums.TripState;
import t3h.edu.vn.traintickets.repository.*;
import t3h.edu.vn.traintickets.service.OrderService;
import t3h.edu.vn.traintickets.service.TicketService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final TicketService ticketService;
    private final OrderService orderService;
    private final SeatRepository seatRepository;
    private  final  TicketRepository ticketRepository;
    private final  OrderRepository orderRepository;
    private final OrderTicketRepository orderTicketRepository;

    @Transactional
    public BookingResponse createBooking(Long tripId,
                                         BookingRequest request,
                                         String username) {

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            throw new RuntimeException("Ticket list cannot be empty");
        }

        List<Ticket> tickets = ticketService
                .createTickets(user, trip, request.getTickets());

        Order order = orderService.createOrder(user, tickets);

        return BookingResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .holdUntil(order.getHoldUntil())
                .build();
    }

    @Transactional
    public Long createRebookOrder(Long oldOrderId, String username) {

        // 1️⃣ Validate order + user
        Order oldOrder = validateOrderForRebook(oldOrderId, username);

        Trip trip = oldOrder.getOrderTickets()
                .get(0)
                .getTicket()
                .getTrip();

        validateTripForRebook(trip);

        // 2️⃣ Clone tickets sau khi check seat
        List<Ticket> newTickets = new ArrayList<>();

        for (OrderTicket ot : oldOrder.getOrderTickets()) {

            Ticket oldTicket = ot.getTicket();

            checkSeatAvailability(trip, oldTicket.getSeat());

            Ticket newTicket = cloneTicket(oldTicket);

            newTickets.add(ticketRepository.save(newTicket));
        }

        // 3️⃣ Reuse createOrder
        Order newOrder = orderService.createOrder(
                oldOrder.getUser(),
                newTickets
        );

        return newOrder.getId();
    }

    private Order validateOrderForRebook(Long orderId, String username) {

        Order order = orderRepository.findByIdWithTickets(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }

        // Không phải chủ đơn
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền thực hiện thao tác này");
        }

        // Không cho rebook nếu đang PENDING hoặc PAID
        if (!(order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.FAILED)) {

            throw new RuntimeException("Đơn hàng không thể đặt lại");
        }

        if (order.getOrderTickets() == null || order.getOrderTickets().isEmpty()) {
            throw new RuntimeException("Đơn hàng không có vé");
        }

        return order;
    }

    private void validateTripForRebook(Trip trip) {

        if (trip == null) {
            throw new RuntimeException("Chuyến đi không tồn tại");
        }

        if (trip.getStatus() != TripState.ACTIVE) {
            throw new RuntimeException("Chuyến đi hiện không mở bán");
        }

        if (trip.getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Chuyến đi đã khởi hành hoặc đã kết thúc, vui lòng đặt chuyến khác"
            );
        }
    }

    private void checkSeatAvailability(Trip trip, Seat seat) {

        boolean taken = ticketRepository.existsBySeatAndTripAndStatusIn(
                seat,
                trip,
                List.of(TicketStatus.PENDING, TicketStatus.PAID)
        );

        if (taken) {
            throw new RuntimeException(
                    "Ghế " + seat.getSeatCode() + " đã được đặt"
            );
        }
    }

    private Ticket cloneTicket(Ticket oldTicket) {

        Ticket ticket = new Ticket();
        ticket.setUser(oldTicket.getUser());
        ticket.setTrip(oldTicket.getTrip());
        ticket.setSeat(oldTicket.getSeat());
        ticket.setTicketType(oldTicket.getTicketType());
        ticket.setPrice(oldTicket.getPrice()); // giữ giá cũ
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setCreatedAt(LocalDateTime.now());

        return ticket;
    }

}