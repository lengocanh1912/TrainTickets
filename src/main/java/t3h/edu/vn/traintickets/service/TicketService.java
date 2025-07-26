package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.TicketDto;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.entities.Train;
import t3h.edu.vn.traintickets.repository.TicketRepository;

import java.util.List;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    public List<Ticket> getAll() {
        List<Ticket> tickets = null;
        try {
            tickets = ticketRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tickets;
    }

//    public Page paging(int pageNo, int pageSize) {
//        Pageable pageable = PageRequest.of(pageNo, pageSize);
//        Page<Ticket> tickets = ticketRepository.findAll(pageable);
//        return tickets;
//    }

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

        return dto;
    }

    public Page<TicketDto> paging(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        // Dùng EntityGraph để tránh lỗi Lazy
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

    public List<Ticket> searchTickets(String keyword) {
        return ticketRepository.searchByUserOrTripName(keyword);
    }
}
