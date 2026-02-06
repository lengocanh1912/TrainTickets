package t3h.edu.vn.traintickets.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private String type;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String status;
    private LocalDateTime createdAt;
    private boolean isOwn;
    private boolean read;
    private String clientTempId;


}