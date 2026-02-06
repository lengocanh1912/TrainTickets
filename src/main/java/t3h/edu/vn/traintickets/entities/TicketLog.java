package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import t3h.edu.vn.traintickets.enums.TicketStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ticket_log")
public class TicketLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false)
    private String action; // ví dụ: "CẬP NHẬT TRẠNG THÁI", "HỦY VÉ", "HOÀN TIỀN"

    @Column
    @Enumerated(EnumType.STRING)
    private TicketStatus oldStatus;

    @Column
    @Enumerated(EnumType.STRING)
    private TicketStatus newStatus;


    @Column(length = 255)
    private String reason;

    @Column(nullable = false)
    private String updatedBy; // admin name

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public TicketStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TicketStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TicketStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
