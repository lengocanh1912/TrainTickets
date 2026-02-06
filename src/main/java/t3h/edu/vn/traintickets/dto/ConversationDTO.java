package t3h.edu.vn.traintickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    private String bookingId;
    private Long assignedAdminId;
    private String status;

    private Long lastMessageSenderId;
    private String lastMessageSenderName;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;

    private int unreadCount;


}
