package t3h.edu.vn.traintickets.entities;

import jakarta.persistence.*;
import lombok.*;
import t3h.edu.vn.traintickets.enums.MessageStatus;
import t3h.edu.vn.traintickets.enums.MessageType;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ Để LAZY, query sẽ fetch
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type; // TEXT, IMAGE, VIDEO, FILE

    private String fileUrl;
    private String fileName;
    private Long fileSize;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = MessageStatus.SENT;
    }

}
