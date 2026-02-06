package t3h.edu.vn.traintickets.entities;

import lombok.*;
import jakarta.persistence.*;
import t3h.edu.vn.traintickets.enums.ConversationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ConversationStatus status; // OPEN, CLOSED, WAITING

    private String bookingId;

    @Column(name = "unread_for_admin")
    private Integer unreadForAdmin;

    @Column(name = "unread_for_user")
    private Integer unreadForUser;

    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
        status = ConversationStatus.WAITING;
        unreadForAdmin = 0;
        unreadForUser = 0;
    }

}
