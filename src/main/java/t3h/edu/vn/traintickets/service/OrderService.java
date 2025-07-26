package t3h.edu.vn.traintickets.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.dto.OrderGroupedTicketDto;
import t3h.edu.vn.traintickets.dto.OrderPaymentDto;
import t3h.edu.vn.traintickets.dto.OrderTicketDetailDto;
import t3h.edu.vn.traintickets.dto.TicketDto;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.repository.OrderRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

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

    public String generateUniqueOrderCode() {
        int retry = 0;
        while (retry < 5) {
            String code = generateOrderCode();
            if (!orderRepository.existsByOrderCode(code)) {
                return code;
            }
            retry++;
        }
        throw new RuntimeException("Không thể sinh mã đơn hàng duy nhất sau nhiều lần thử.");
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
        Order order = orderRepository.findWithTicketsById(id).orElseThrow(() ->
                new RuntimeException("Order not found"));
        return getOrderPaymentDto(order);
    }

    @Transactional
    public OrderPaymentDto getOrderPaymentDto(Order order) {
        OrderPaymentDto dto = new OrderPaymentDto();
        dto.setOrderId(order.getId());
        dto.setOrderCode("ORDER-" + order.getId()); // tuỳ bạn đặt mã đơn
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setFinalAmount(order.getFinalAmount());

        List<TicketDto> ticketDtos = new ArrayList<>();

        for (OrderTicket orderTicket : order.getOrderTickets()) {
            Ticket ticket = orderTicket.getTicket();
            Trip trip = ticket.getTrip();
            Route route = trip.getRoute();

            TicketDto ticketDto = new TicketDto();
            ticketDto.setTrainName(trip.getTrain().getName());
            ticketDto.setCoachCode(ticket.getSeat().getCoach().getCode());
            ticketDto.setSeatCode(ticket.getSeat().getSeatCode());
            ticketDto.setDepartureStation(route.getDeparture().getName());
            ticketDto.setArrivalStation(route.getArrival().getName());
            ticketDto.setDepartureAt(trip.getDepartureAt());
            ticketDto.setArrivalAt(trip.getArrivalAt());
            ticketDto.setPrice(ticket.getPrice());
            ticketDto.setTicketType(ticket.getTicketType());

            ticketDtos.add(ticketDto);
        }

        dto.setTickets(ticketDtos);
        return dto;
    }

    @Transactional
    public Page<OrderTicketDetailDto> getUserOrderTickets(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);

        List<OrderTicketDetailDto> dtoList = new ArrayList<>();
        for (Order order : ordersPage.getContent()) {
            for (OrderTicket ot : order.getOrderTickets()) {
                Ticket ticket = ot.getTicket();
                Trip trip = ticket.getTrip();
                Seat seat = ticket.getSeat();
                Coach coach = seat.getCoach();
                Train train = coach.getTrain();
                Route route = trip.getRoute();

                OrderTicketDetailDto dto = new OrderTicketDetailDto();
                dto.setTrainName(train.getName());
                dto.setCoachCode(coach.getCode());
                dto.setSeatCode(seat.getSeatCode());
                dto.setOrderCode(order.getOrderCode());

                dto.setDepartureStation(route.getDeparture().getName());
                dto.setArrivalStation(route.getArrival().getName());

                dto.setDepartureAt(trip.getDepartureAt());
                dto.setArrivalAt(trip.getArrivalAt());

                dto.setPrice(ticket.getPrice());
                dto.setTicketType(ticket.getTicketType());
                dto.setTicketStatus(ticket.getStatus());

                dtoList.add(dto);
            }
        }

        // Tạo Page từ danh sách DTO
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtoList.size());
        List<OrderTicketDetailDto> pageContent = dtoList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, dtoList.size());
    }

    @Transactional
    public List<OrderGroupedTicketDto> getGroupedOrderTickets(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithTickets(userId); // đã fetch sẵn

        List<OrderGroupedTicketDto> result = new ArrayList<>();

        for (Order order : orders) {
            List<OrderTicket> orderTickets = order.getOrderTickets();
            if (orderTickets == null || orderTickets.isEmpty()) continue; // tránh lỗi IndexOutOfBounds

            OrderGroupedTicketDto dto = new OrderGroupedTicketDto();
            dto.setOrderCode(order.getOrderCode());

            // Lấy vé đầu tiên để lấy thông tin chuyến đi
            Optional<Ticket> optionalFirstTicket = order.getOrderTickets().stream()
                    .map(OrderTicket::getTicket)
                    .findFirst();

            if (optionalFirstTicket.isEmpty()) continue;

            Ticket firstTicket = optionalFirstTicket.get();
            Trip trip = firstTicket.getTrip();

            dto.setDepartureAt(trip.getDepartureAt());
            dto.setArrivalAt(trip.getArrivalAt());
            dto.setDepartureStation(trip.getRoute().getDeparture().getName());
            dto.setArrivalStation(trip.getRoute().getArrival().getName());
            dto.setId(order.getId());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setStatus(order.getStatus());
            // Gom vé theo loại (người lớn, trẻ em,...)
            Map<String, List<TicketDto>> grouped = new LinkedHashMap<>();

            for (OrderTicket ot : orderTickets) {
                Ticket ticket = ot.getTicket();

                String label = switch (ticket.getTicketType()) {
                    case 0 -> "Người lớn";
                    case 1 -> "Trẻ em";
                    case 2 -> "Sinh viên";
                    case 3 -> "Người cao tuổi";
                    default -> "Không rõ";
                };

                grouped.computeIfAbsent(label, k -> new ArrayList<>());

                TicketDto detail = new TicketDto();
                detail.setCoachCode(ticket.getSeat().getCoach().getCode());
                detail.setSeatCode(ticket.getSeat().getSeatCode());
                detail.setPrice(ticket.getPrice());

                grouped.get(label).add(detail);
            }

            dto.setGroupedTickets(grouped);
            result.add(dto);

        }
        LocalDateTime now = LocalDateTime.now();
        result.sort(Comparator.comparing(OrderGroupedTicketDto::getCreatedAt).reversed());

        return result;
    }

    @Transactional
    public void updateOrderStatusToPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() == 0) {
            order.setStatus((byte) 1); // Đã thanh toán
//            order.setPaidAt(LocalDateTime.now()); // Nếu bạn có trường paidAt
            orderRepository.save(order);
        }
    }

    @Transactional
    public List<OrderGroupedTicketDto> getAllGroupedOrderTickets() {
        List<Order> orders = orderRepository.findAllWithTickets(); // cần viết query JOIN FETCH

        List<OrderGroupedTicketDto> result = new ArrayList<>();

        for (Order order : orders) {
            List<OrderTicket> orderTickets = order.getOrderTickets();
            if (orderTickets == null || orderTickets.isEmpty()) continue;

            OrderGroupedTicketDto dto = new OrderGroupedTicketDto();
            dto.setOrderCode(order.getOrderCode());
            dto.setId(order.getId());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setStatus(order.getStatus());

            // Lấy thông tin chuyến đi từ vé đầu
            Optional<Ticket> optionalFirstTicket = orderTickets.stream()
                    .map(OrderTicket::getTicket)
                    .findFirst();
            if (optionalFirstTicket.isEmpty()) continue;

            Ticket firstTicket = optionalFirstTicket.get();
            Trip trip = firstTicket.getTrip();
            dto.setDepartureAt(trip.getDepartureAt());
            dto.setArrivalAt(trip.getArrivalAt());
            dto.setDepartureStation(trip.getRoute().getDeparture().getName());
            dto.setArrivalStation(trip.getRoute().getArrival().getName());

            // Gom nhóm vé
            Map<String, List<TicketDto>> grouped = new LinkedHashMap<>();
            for (OrderTicket ot : orderTickets) {
                Ticket ticket = ot.getTicket();

                String label = switch (ticket.getTicketType()) {
                    case 0 -> "Người lớn";
                    case 1 -> "Trẻ em";
                    case 2 -> "Sinh viên";
                    case 3 -> "Người cao tuổi";
                    default -> "Không rõ";
                };

                grouped.computeIfAbsent(label, k -> new ArrayList<>());

                TicketDto td = new TicketDto();
                td.setCoachCode(ticket.getSeat().getCoach().getCode());
                td.setSeatCode(ticket.getSeat().getSeatCode());
                td.setPrice(ticket.getPrice());
                td.setTicketType(ticket.getTicketType());
                td.setTrainName(ticket.getTrip().getTrain().getName());
                td.setUserFullname(order.getUser().getFullname());

                grouped.get(label).add(td);
            }

            dto.setGroupedTickets(grouped);
            result.add(dto);
        }

        result.sort(Comparator.comparing(OrderGroupedTicketDto::getCreatedAt).reversed());
        return result;
    }

}
