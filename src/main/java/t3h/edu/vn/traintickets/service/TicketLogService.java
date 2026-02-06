package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.entities.Ticket;
import t3h.edu.vn.traintickets.entities.TicketLog;
import t3h.edu.vn.traintickets.enums.TicketStatus;
import t3h.edu.vn.traintickets.repository.TicketLogRepository;
import t3h.edu.vn.traintickets.repository.TicketRepository;


import java.time.LocalDateTime;

@Service
public class TicketLogService {

    @Autowired
    private TicketLogRepository ticketLogRepository;
    @Autowired
    private TicketRepository ticketRepository;

//    public void logChange(Ticket ticket,
//                          String action,
//                          String oldStatus,
//                          String newStatus,
//                          String updatedBy) {
//        TicketLog log = new TicketLog();
//        log.setTicket(ticket);
//        log.setAction(action);
//        log.setOldStatus(oldStatus);
//        log.setNewStatus(newStatus);
//        log.setUpdatedBy(updatedBy);
//        log.setUpdatedAt(LocalDateTime.now());
//        ticketLogRepository.save(log);
//    }




}

