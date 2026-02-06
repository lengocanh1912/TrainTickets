package t3h.edu.vn.traintickets.dto;

import t3h.edu.vn.traintickets.entities.Ticket;

public record QrVerifyResult(
        boolean valid,
        String message,
        Ticket ticket
) {}
