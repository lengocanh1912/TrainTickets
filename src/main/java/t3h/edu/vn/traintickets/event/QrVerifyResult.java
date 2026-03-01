package t3h.edu.vn.traintickets.event;

import t3h.edu.vn.traintickets.entities.Ticket;

public record QrVerifyResult(
        boolean valid,
        String message,
        Ticket ticket
) {}
