package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.dto.*;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.exception.SeatAlreadyBookedException;
import t3h.edu.vn.traintickets.repository.OrderRepository;
import t3h.edu.vn.traintickets.repository.SeatRepository;
import t3h.edu.vn.traintickets.repository.TicketLogRepository;
import t3h.edu.vn.traintickets.repository.TicketRepository;
import t3h.edu.vn.traintickets.service.booking.PricingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketLogRepository ticketLogRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private SeatRepository seatRepository;

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));
    }

    public List<Ticket> getAll() {
        List<Ticket> tickets = null;
        try {
            tickets = ticketRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tickets;
    }


    // Lấy lịch sử cập nhật vé
    public List<TicketLog> getTicketLogs(Long ticketId) {
        return ticketLogRepository.findAllByTicketIdOrderByUpdatedAtDesc(ticketId);
    }

    private TicketDto convertToDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setUserFullname(ticket.getUser().getFullname());
        dto.setTrainName(ticket.getTrip().getTrain().getName());
        dto.setCoachCode(ticket.getSeat().getCoach().getCode());
        dto.setSeatCode(ticket.getSeat().getSeatCode());
        dto.setDepartureStation(ticket.getTrip().getRoute().getDeparture().getName());
        dto.setArrivalStation(ticket.getTrip().getRoute().getArrival().getName());
        dto.setDepartureAt(ticket.getTrip().getDepartureAt());
        dto.setArrivalAt(ticket.getTrip().getArrivalAt());
        dto.setPrice(ticket.getPrice());
        dto.setTicketType(ticket.getTicketType());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setStatus(ticket.getStatus());

        return dto;
    }

    public Page<TicketDto> paging(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> tickets = ticketRepository.findAllWithDependencies(pageable);
        return tickets.map(this::convertToDto);
    }

    public List<Ticket> findByTripId(Long tripId) {
        List<Ticket> ticket = null;
        try{
            ticket = ticketRepository.findByTripId(tripId);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return ticket;
    }


    @Transactional
    public List<TicketAdminDto> searchTickets(String keyword,
                                              TicketStatus status,
                                              Long trainId,
                                              LocalDate departureDate,
                                              String departure,
                                              String arrival) {
        List<Ticket> tickets = ticketRepository.searchTickets(keyword,
                status,
                trainId,
                departureDate,
                departure,
                arrival);

        return tickets.stream().map(t -> new TicketAdminDto(
                t.getId(),
                t.getUser().getFullname(),
                t.getTrip().getTrain().getName(),
                t.getTrip().getRoute().getDeparture().getName(),
                t.getTrip().getRoute().getArrival().getName(),
                t.getTrip().getDepartureAt(),
                t.getTrip().getArrivalAt(),
                t.getSeat().getCoach().getCode() + "-" + t.getSeat().getSeatCode(),
                t.getTicketType(),
                t.getPrice(),
                t.getStatus(),
                t.getCreatedAt()
        )).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public TicketDetailDto getTicketDetail(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        Order order = orderRepository.findByTicketId(ticketId)
                .orElse(null);

        TicketDetailDto dto = new TicketDetailDto();
        dto.setId(ticket.getId());
        dto.setPrice(ticket.getPrice());
        dto.setTicketStatus(ticket.getStatus().name());
        dto.setTicketType(getTicketTypeName(ticket.getTicketType()));

        dto.setUserFullName(ticket.getUser().getFullname());
        dto.setUserEmail(ticket.getUser().getEmail());

        dto.setTrainName(ticket.getTrip().getTrain().getName());
        dto.setDepartureStation(ticket.getTrip().getRoute().getDeparture().getName());
        dto.setArrivalStation(ticket.getTrip().getRoute().getArrival().getName());
        dto.setDepartureAt(ticket.getTrip().getDepartureAt());
        dto.setArrivalAt(ticket.getTrip().getArrivalAt());

        dto.setSeatName(ticket.getSeat().getSeatCode());
        dto.setCoachName(ticket.getSeat().getCoach().getCode());

        if (order != null) {
            dto.setOrderCode(order.getOrderCode());
            dto.setPaymentMethod("Thanh toán qua VNPay"); // sau này thay = field thực
            dto.setRefundPolicy("Theo chính sách của công ty (24h trước khởi hành)");
        }

        dto.setLogs(
                ticketLogRepository.findAllByTicketIdOrderByUpdatedAtDesc(ticketId)
                        .stream()
                        .map(log -> {
                            TicketLogDto logDto = new TicketLogDto();
                            logDto.setAction(log.getAction());
                            logDto.setOldStatus(log.getOldStatus());
                            logDto.setNewStatus(log.getNewStatus());
                            logDto.setUpdatedBy(log.getUpdatedBy());
                            logDto.setUpdatedAt(log.getUpdatedAt());
                            logDto.setReason(log.getReason());
                            return logDto;
                        })
                        .toList()
        );

        return dto;
    }

    private String getTicketTypeName(Byte type) {
        return switch (type) {
            case 0 -> "Người lớn";
            case 1 -> "Trẻ em";
            case 2 -> "Sinh viên";
            case 3 -> "Người cao tuổi";
            default -> "Khác";
        };
    }

    @Transactional
    public void updateTicketStatus(Long id,
                                   TicketStatus newStatus,
                                   String adminName,
                                   String reason) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        TicketLog log = new TicketLog();
        log.setTicket(ticket);
        log.setAction("CẬP NHẬT TRẠNG THÁI");
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setReason(reason);
        log.setUpdatedBy(adminName);
        log.setUpdatedAt(LocalDateTime.now());
        ticketLogRepository.save(log);
    }

    public TicketCancelFormDto getTicketViewById(Long id) {
        Order order = orderRepository.findByTicketId(id)
                .orElse(null);
        TicketCancelFormDto dto = ticketRepository.findCancelViewById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));
        if (order != null) {
            dto.setOrderCode(order.getOrderCode());
        }
        return dto;
    }

    public void cancelTicket(TicketCancelDto dto) {
        Ticket ticket = ticketRepository.findById(dto.getTicketId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new RuntimeException("Vé đã bị hủy trước đó!");
        }

        TicketStatus old = ticket.getStatus();
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        TicketLog log = new TicketLog();
        log.setTicket(ticket);
        log.setAction("Cancel Ticket");
        log.setReason(dto.getReason());
        log.setOldStatus(old);
        log.setNewStatus(TicketStatus.CANCELLED);
        log.setUpdatedAt(LocalDateTime.now());
        log.setUpdatedBy("Admin");
        ticketLogRepository.save(log);
    }

    public void refundTicket(TicketRefundDto dto) {
        Ticket ticket = ticketRepository.findById(dto.getTicketId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé"));

        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new RuntimeException("Chỉ hoàn tiền cho vé đã thanh toán!");
        }

        TicketStatus old = ticket.getStatus();
        ticket.setStatus(TicketStatus.REFUNDED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        TicketLog log = new TicketLog();
        log.setTicket(ticket);
        log.setAction("Refund Ticket");
        log.setReason(dto.getNote());
        log.setOldStatus(old);
        log.setNewStatus(TicketStatus.REFUNDED);
        log.setUpdatedAt(LocalDateTime.now());
        log.setUpdatedBy("Admin");
        ticketLogRepository.save(log);
    }

    public void updateTicketPassengerInfo(List<TicketPaymentDto> tickets) {
        if (tickets == null || tickets.isEmpty()) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (TicketPaymentDto dto : tickets) {
            Ticket ticket = ticketRepository.findById(dto.getTicketId())
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            ticket.setPassengerName(dto.getPassengerName());
            ticket.setBirthday(LocalDate.parse(dto.getBirthDate(), formatter)); // ⬅ Sửa ở đây
            ticket.setCccd(dto.getCccd());
            ticket.setPhone(dto.getPhone());
            ticket.setAddress(dto.getAddress());
            ticket.setUpdatedAt(LocalDateTime.now());

            ticketRepository.save(ticket);
        }
    }

    @Transactional
    public List<Ticket> createTickets(User user,
                                      Trip trip,
                                      List<BookingRequest.TicketRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Ticket request list cannot be empty");
        }

        // 1️⃣ Lấy danh sách seatId và sort để tránh deadlock
        List<Long> seatIds = requests.stream()
                .map(BookingRequest.TicketRequest::getSeatId)
                .distinct()
                .sorted()
                .toList();

        // 2️⃣ Lock toàn bộ ghế 1 lần
        List<Seat> seats = seatRepository.findAllByIdForUpdate(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        // Map để tra cứu nhanh
        Map<Long, Seat> seatMap = seats.stream()
                .collect(Collectors.toMap(Seat::getId, s -> s));

        // 3️⃣ Check ghế đã được đặt chưa
        List<Ticket> existedTickets = ticketRepository
                .findByTripAndSeatIdInAndStatusIn(
                        trip,
                        seatIds,
                        List.of(TicketStatus.PENDING, TicketStatus.PAID)
                );

        if (!existedTickets.isEmpty()) {
            String bookedSeats = existedTickets.stream()
                    .map(t -> t.getSeat().getSeatCode())
                    .distinct()
                    .collect(Collectors.joining(", "));

            throw new SeatAlreadyBookedException(
                    "Ghế: "   + bookedSeats + "đã được đặt "
            );
        }

        // 4️⃣ Tạo ticket
        List<Ticket> tickets = new ArrayList<>();

        for (BookingRequest.TicketRequest req : requests) {

            Seat seat = seatMap.get(req.getSeatId());

            BigDecimal price = pricingService
                    .calculatePrice(trip, seat, req.getTicketType());

            Ticket ticket = new Ticket();
            ticket.setUser(user);
            ticket.setTrip(trip);
            ticket.setSeat(seat);
            ticket.setTicketType(req.getTicketType());
            ticket.setPrice(price);
            ticket.setStatus(TicketStatus.PENDING);
            ticket.setCreatedAt(LocalDateTime.now());

            tickets.add(ticket);
        }

        return ticketRepository.saveAll(tickets);
    }

}
