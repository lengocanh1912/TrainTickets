package t3h.edu.vn.traintickets.service;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.dto.*;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.enums.CancelType;
import t3h.edu.vn.traintickets.enums.OrderStatus;
import t3h.edu.vn.traintickets.enums.SeatStatus;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.event.OrderPaidEvent;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.OrderTicketRepository;
import t3h.edu.vn.traintickets.repository.SeatRepository;
import t3h.edu.vn.traintickets.repository.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service

public class OrderService {

    @Autowired
    private MailService mailService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private OrderTicketRepository orderTicketRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Transactional(readOnly = true)
    public List<OrderGroupedTicketDto> searchGroupedOrders(String keyword) {
        List<Order> orders = orderRepository.searchByKeyword(keyword);
        List<OrderGroupedTicketDto> content = new ArrayList<>();

        for (Order order : orders) {
            List<OrderTicket> orderTickets = order.getOrderTickets();
            if (orderTickets == null || orderTickets.isEmpty()) continue;

            OrderGroupedTicketDto dto = new OrderGroupedTicketDto();
            dto.setId(order.getId());
            dto.setOrderCode(order.getOrderCode());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setStatus(order.getStatus());
            dto.setUserFullname(order.getUser().getFullname());

            // Lấy vé đầu
            Ticket firstTicket = orderTickets.stream()
                    .map(OrderTicket::getTicket)
                    .findFirst()
                    .orElse(null);
            if (firstTicket == null) continue;

            Trip trip = firstTicket.getTrip();
            dto.setDepartureAt(trip.getDepartureAt());
            dto.setArrivalAt(trip.getArrivalAt());
            dto.setDepartureStation(trip.getRoute().getDeparture().getName());
            dto.setArrivalStation(trip.getRoute().getArrival().getName());

            // Gom vé theo loại
            Map<String, List<TicketDto>> grouped = new LinkedHashMap<>();
            for (OrderTicket ot : orderTickets) {
                Ticket t = ot.getTicket();
                String label = switch (t.getTicketType()) {
                    case 0 -> "Người lớn";
                    case 1 -> "Trẻ em";
                    case 2 -> "Sinh viên";
                    case 3 -> "Người cao tuổi";
                    default -> "Không rõ";
                };
                grouped.computeIfAbsent(label, k -> new ArrayList<>());

                TicketDto td = new TicketDto();
                td.setCoachCode(t.getSeat().getCoach().getCode());
                td.setSeatCode(t.getSeat().getSeatCode());
                td.setPrice(t.getPrice());
                td.setTicketType(t.getTicketType());
                td.setTrainName(t.getTrip().getTrain().getName());
                td.setUserFullname(order.getUser().getFullname());

                grouped.get(label).add(td);
            }

            dto.setGroupedTickets(grouped);
            content.add(dto);
        }

        return content;
    }

    //phân trang
    public Page paging(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Order> orders  = orderRepository.findAll(pageable);
        return orders;
    }

    //doanh thu
    public double getTotalRevenue() {
        Double revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    @Transactional
    public Page<OrderGroupedTicketDto> pagingGroupedOrders(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<Order> ordersPage = orderRepository.findAllWithTicketsPage(pageable); // thêm query

        List<OrderGroupedTicketDto> content = new ArrayList<>();

        for (Order order : ordersPage.getContent()) {
            List<OrderTicket> orderTickets = order.getOrderTickets();
            if (orderTickets == null || orderTickets.isEmpty()) continue;

            OrderGroupedTicketDto dto = new OrderGroupedTicketDto();
            dto.setId(order.getId());
            dto.setOrderCode(order.getOrderCode());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setStatus(order.getStatus());
            dto.setUserFullname(order.getUser().getFullname());

            // Lấy vé đầu
            Ticket firstTicket = orderTickets.stream()
                    .map(OrderTicket::getTicket)
                    .findFirst()
                    .orElse(null);

            if (firstTicket == null) continue;

            Trip trip = firstTicket.getTrip();
            dto.setDepartureAt(trip.getDepartureAt());
            dto.setArrivalAt(trip.getArrivalAt());
            dto.setDepartureStation(trip.getRoute().getDeparture().getName());
            dto.setArrivalStation(trip.getRoute().getArrival().getName());

            // Gom vé theo loại
            Map<String, List<TicketDto>> grouped = new LinkedHashMap<>();
            for (OrderTicket ot : orderTickets) {
                Ticket t = ot.getTicket();
                String label = switch (t.getTicketType()) {
                    case 0 -> "Người lớn";
                    case 1 -> "Trẻ em";
                    case 2 -> "Sinh viên";
                    case 3 -> "Người cao tuổi";
                    default -> "Không rõ";
                };

                grouped.computeIfAbsent(label, k -> new ArrayList<>());

                TicketDto td = new TicketDto();
                td.setCoachCode(t.getSeat().getCoach().getCode());
                td.setSeatCode(t.getSeat().getSeatCode());
                td.setPrice(t.getPrice());
                td.setTicketType(t.getTicketType());
                td.setTrainName(t.getTrip().getTrain().getName());
                td.setUserFullname(order.getUser().getFullname());

                grouped.get(label).add(td);
            }

            dto.setGroupedTickets(grouped);
            content.add(dto);
        }

        return new PageImpl<>(content, pageable, ordersPage.getTotalElements());
    }

    public String generateOrderCode() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "OD" + timePart + randomPart;
    }

    @Transactional
    public Order findById(Long id) {
        Order order = null;
        try {
            order = orderRepository.findWithTicketsById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    @Transactional
    public OrderPaymentDto getOrderPaymentDtoById(Long id) {
        Order order = orderRepository.findWithTicketsById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return getOrderPaymentDto(order);
    }

    @Transactional
    public OrderPaymentDto getOrderPaymentDto(Order order) {

        OrderPaymentDto dto = new OrderPaymentDto();

        dto.setOrderId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setHoldUntil(order.getHoldUntil());

        // Lấy ticket đầu tiên để lấy thông tin trip
        Ticket firstTicket = order.getOrderTickets().get(0).getTicket();
        Trip trip = firstTicket.getTrip();

        dto.setDepartureStation(trip.getRoute().getDeparture().getName());
        dto.setArrivalStation(trip.getRoute().getArrival().getName());
        dto.setTrainCode(trip.getTrain().getCode());
        dto.setDepartureTime(trip.getDepartureAt());
        dto.setArrivalTime(trip.getArrivalAt());

        List<TicketPaymentDto> ticketDtos = new ArrayList<>();

        for (OrderTicket ot : order.getOrderTickets()) {

            Ticket t = ot.getTicket();
            TicketPaymentDto td = new TicketPaymentDto();

            td.setTicketId(t.getId());

            td.setCoachName(t.getSeat().getCoach().getCode());
            td.setSeatNumber(t.getSeat().getSeatCode());
            td.setSeatType(t.getSeat().getType());
            td.setPrice(t.getPrice());

            Byte type = t.getTicketType();
            td.setTicketType(type);
            td.setTicketTypeLabel(convertTicketType(type));

            // nếu bạn muốn prefill thông tin đã có:
            td.setPassengerName(t.getPassengerName());
            td.setBirthDate(null);
            td.setCccd(t.getCccd());
            td.setPhone(t.getPhone());
            td.setAddress(t.getAddress());

            ticketDtos.add(td);
        }

        dto.setTickets(ticketDtos);

        return dto;
    }

    private String convertTicketType(Byte type) {
        return switch (type) {
            case 0 -> "Người lớn";
            case 1 -> "Trẻ em";
            case 2 -> "Sinh viên";
            case 3 -> "Người cao tuổi";
            default -> "Không xác định";
        };
    }

    @Transactional
    public void updateOrderAndTicketsStatus(Long orderId, boolean success) throws Exception {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 🔒 Guard double callback
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.setHoldUntil(null);

        if (success) {
            order.setStatus(OrderStatus.PAID);

            for (OrderTicket ot : order.getOrderTickets()) {
                Ticket ticket = ot.getTicket();

                ticket.setStatus(TicketStatus.PAID);
                ticket.setUsed(false);

                if (ticket.getTicketCode() == null) {
                    ticket.setTicketCode(generateUniqueTicketCode());
                }

                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }
            // ✅ PUBLISH EVENT (CHỈ KHI PAID)
            eventPublisher.publishEvent(
                    new OrderPaidEvent(order.getId(), order.getContactEmail())
            );
        } else {
            order.setStatus(OrderStatus.FAILED);

            for (OrderTicket ot : order.getOrderTickets()) {
                Ticket ticket = ot.getTicket();
                ticket.setStatus(TicketStatus.CANCELLED);
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }
        }

        orderRepository.save(order);

    }



    @Transactional
    public void cancelOrderAndReleaseEverything(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderTicket> orderTickets = orderTicketRepository.findByOrderId(orderId);

        for (OrderTicket ot : orderTickets) {

            Ticket ticket = ot.getTicket();
            Seat seat = ticket.getSeat();  // LẤY GHẾ Ở ĐÂY

            // 1. Giải phóng ghế
            if (seat != null) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
                order.setHoldUntil(null);
            }

            // 2. Hủy vé
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setUpdatedAt(LocalDateTime.now());
            ticket.setPassengerName(null);
            ticket.setCccd(null);
            ticket.setAddress(null);
            ticket.setBirthday(null);
            ticketRepository.save(ticket);

            // 3. Xóa OrderTicket
            orderTicketRepository.delete(ot);
        }

        // 4. Cập nhật trạng thái của đơn hàng
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelType(CancelType.AUTO_EXPIRED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order {} cancelled, tickets cancelled, seats released, OT deleted", orderId);
    }


    public void updateOrderContactInfo(OrderPaymentDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setContactEmail(dto.getContactEmail());
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    @Transactional
    public void handlePaymentSuccess(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow();

        if (order.getStatus() == OrderStatus.PAID) {
            return; // IDEMPOTENT
        }

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());

        for (OrderTicket ot : order.getOrderTickets()) {

            Long ticketId = ot.getTicket().getId();

            Ticket t = ticketRepository.findById(ticketId)
                    .orElseThrow();

            t.setStatus(TicketStatus.PAID);
            t.setTicketCode(UUID.randomUUID().toString());
            t.setUsed(false);
            t.setUpdatedAt(LocalDateTime.now());

            ticketRepository.save(t);
        }

        orderRepository.save(order);
    }

//    @Transactional
//    public void markOrderPaid(Order order) {
//        order.setStatus(OrderStatus.PAID);
//        order.setUpdatedAt(LocalDateTime.now());
//        orderRepository.save(order);
//    }

    private String generateUniqueTicketCode() {
        String code ;
        do {
            code = "TRAINTICKET-"
                    + LocalDate.now()
                    + "-"
                    + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase()
            ;
        } while (ticketRepository.existsByTicketCode(code));
        return code;
    }

}
