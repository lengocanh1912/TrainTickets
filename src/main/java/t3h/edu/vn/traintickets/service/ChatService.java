package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import t3h.edu.vn.traintickets.dto.*;
import t3h.edu.vn.traintickets.entities.Conversation;
import t3h.edu.vn.traintickets.entities.Message;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.enums.ConversationStatus;
import t3h.edu.vn.traintickets.enums.MessageStatus;
import t3h.edu.vn.traintickets.enums.MessageType;
import t3h.edu.vn.traintickets.repository.ConversationRepository;
import t3h.edu.vn.traintickets.repository.MessageRepository;
import t3h.edu.vn.traintickets.repository.UserRepository;
import t3h.edu.vn.traintickets.service.auth.UserPresenceService;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(MessageRepository messageRepository,
                       ConversationRepository conversationRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private static final String UPLOAD_DIR = "uploads/chat/";

    @Autowired
    private UserPresenceService userPresenceService;

    @Transactional
    public MessageDTO sendMessage(Long conversationId,
                                  Long senderId,
                                  String content,
                                  MultipartFile file,
                                  String clientTempId)
                                                        throws IOException {

        // ===== LOAD CONVERSATION =====
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // ===== LOAD SENDER =====
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean senderIsUser = senderId.equals(conversation.getUser().getId());

        // ===== REOPEN LOGIC =====
        // User gửi khi conversation đã CLOSED → chuyển về WAITING
        if (senderIsUser && ConversationStatus.CLOSED.equals(conversation.getStatus())) {
            conversation.setStatus(ConversationStatus.WAITING);
            conversation.setClosedAt(null);
            conversation.setAdmin(null); // cho phép admin khác claim
        }

        // ===== ADMIN MUST CLAIM & MATCH =====
        if (!senderIsUser) {
            if (!ConversationStatus.OPEN.equals(conversation.getStatus())) {
                throw new RuntimeException("Conversation has not been claimed");
            }

            if (conversation.getAdmin() == null ||
                    !conversation.getAdmin().getId().equals(senderId)) {
                throw new RuntimeException("Admin has not claimed this conversation");
            }
        }


        // ===== CREATE MESSAGE =====
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setType(MessageType.TEXT);
        message.setStatus(MessageStatus.SENT);

        // ===== FILE HANDLING =====
        if (file != null && !file.isEmpty()) {
            String fileUrl = saveFile(file);
            message.setFileUrl(fileUrl);
            message.setFileName(file.getOriginalFilename());
            message.setFileSize(file.getSize());

            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    message.setType(MessageType.IMAGE);
                } else if (contentType.startsWith("video/")) {
                    message.setType(MessageType.VIDEO);
                } else {
                    message.setType(MessageType.FILE);
                }
            }
        }

        // ===== SAVE MESSAGE =====
        message = messageRepository.save(message);

        // ===== UPDATE CONVERSATION =====
        conversation.setLastMessageAt(LocalDateTime.now());

        if (senderIsUser) {
            // USER gửi → admin chưa đọc
            conversation.setUnreadForAdmin(
                    conversation.getUnreadForAdmin() + 1
            );
        } else {
            // ADMIN gửi → user chưa đọc
            conversation.setUnreadForUser(
                    conversation.getUnreadForUser() + 1
            );
        }

        conversationRepository.save(conversation);

        // ===== REALTIME SUPPORT BADGE (USER) =====
        if (!senderIsUser) {
            // Admin gửi → cập nhật badge cho USER
            messagingTemplate.convertAndSend(
                    "/topic/user/" + conversation.getUser().getId() + "/support-unread",
                    conversation.getUnreadForUser()
            );
        }

        // ===== DTO =====
        MessageDTO messageDTO = convertToDTO(message, senderId);
        messageDTO.setClientTempId(clientTempId);
        // ===== REALTIME MESSAGE =====
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                messageDTO
        );
        // ===== NOTIFY ADMIN =====
        if (senderIsUser) {
            if (conversation.getAdmin() != null) {
                // 🔔 Đã có admin → notify admin đó
                messagingTemplate.convertAndSend(
                        "/queue/admin/" + conversation.getAdmin().getId() + "/notification",
                        messageDTO
                );
            } else {
                // 🔔 Chưa có admin → notify ALL ADMIN
                messagingTemplate.convertAndSend(
                        "/topic/admin/notification",
                        messageDTO
                );
            }
        }
        // ===== NOTIFY USER =====
        if (!senderIsUser) {
            messagingTemplate.convertAndSend(
                    "/queue/user/" + conversation.getUser().getId() + "/notification",
                    messageDTO
            );
        }

        return messageDTO;
    }


    @Transactional
    public void markAsRead(Long conversationId, Long readerId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        boolean isUser = conversation.getUser().getId().equals(readerId);

        // 🔥 LẤY TIN CHƯA ĐỌC (CHỈ SENT)
        List<Message> unreadMessages =
                messageRepository.findUnreadMessages(conversationId, readerId);

        if (unreadMessages.isEmpty()) return;

        // 🔥 UPDATE STATUS
        unreadMessages.forEach(msg -> {
            msg.setStatus(MessageStatus.READ);
            msg.setReadAt(LocalDateTime.now());
        });

        messageRepository.saveAll(unreadMessages);

        // 🔥 RESET UNREAD COUNT
        if (isUser) {
            conversation.setUnreadForUser(0);
        } else {
            conversation.setUnreadForAdmin(0);
        }

        conversationRepository.save(conversation);

        // 🔥 GỬI EVENT READ (GỬI TỪNG MESSAGE ID)
        unreadMessages.forEach(msg -> {
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId + "/read",
                    new MessageReadDTO(msg.getId(), readerId)
            );
        });
    }



    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(Long conversationId, Long currentUserId) {
        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        return messages.stream()
                .map(msg -> convertToDTO(msg, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Conversation getOrCreateConversation(Long userId) {

        List<Conversation> conversations =
                conversationRepository.findLatestForUser(
                        userId,
                        List.of(ConversationStatus.OPEN, ConversationStatus.WAITING, ConversationStatus.CLOSED)
                );

        if (!conversations.isEmpty()) {
            Conversation c = conversations.get(0);

            // 🔁 REOPEN nếu đã đóng
            if (c.getStatus() == ConversationStatus.CLOSED) {
                c.setStatus(ConversationStatus.WAITING);
                c.setAdmin(null);
                c.setClosedAt(null);
                c.setUnreadForAdmin(0);
                c.setUnreadForUser(0);
                return conversationRepository.save(c);
            }


            return c;
        }

        // 👉 Chỉ khi user CHƯA BAO GIỜ chat
        Conversation newConversation = new Conversation();
        newConversation.setUser(userRepository.getReferenceById(userId));
        newConversation.setStatus(ConversationStatus.WAITING);
        newConversation.setCreatedAt(LocalDateTime.now());
        newConversation.setLastMessageAt(LocalDateTime.now());
        newConversation.setUnreadForAdmin(0);


        return conversationRepository.save(newConversation);
    }


    // ✅ SỬA METHOD NÀY
    public void sendTypingIndicator(Long conversationId, Long userId, boolean isTyping) {
        TypingIndicatorDTO indicator = new TypingIndicatorDTO(userId, isTyping);
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/typing",
                indicator
        );
    }

    private String saveFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/chat/" + fileName;
    }

    private MessageDTO convertToDTO(Message message, Long currentUserId) {
        MessageDTO dto = new MessageDTO();

        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullname());
        dto.setContent(message.getContent());
        dto.setType(message.getType().name());
        dto.setFileUrl(message.getFileUrl());
        dto.setFileName(message.getFileName());
        dto.setFileSize(message.getFileSize());
        dto.setStatus(message.getStatus().name());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setOwn(message.getSender().getId().equals(currentUserId));

        return dto;
    }

    // Thêm vào ChatService.java

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForAdmin(Long adminId) {

        List<Conversation> conversations =
                conversationRepository.findAdminInbox(adminId);

        return conversations.stream()
                .map(this::convertToConversationDTO)
                .toList();
    }


    @Transactional
    public void closeConversation(Long conversationId, Long adminId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getAdmin().getId().equals(adminId)) {
            throw new RuntimeException("Not authorized");
        }

        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());

        conversationRepository.save(conversation);

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/closed",
                "Conversation closed"
        );
    }

    @Transactional
    public void claimConversation(Long conversationId, Long adminId) {

        // ===== LOAD CONVERSATION =====
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // ===== ONLY WAITING CAN BE CLAIMED =====
        if (!ConversationStatus.WAITING.equals(conversation.getStatus())) {
            throw new RuntimeException("Conversation is not waiting");
        }

        // ===== LOAD ADMIN =====
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // ===== CLAIM =====
        conversation.setAdmin(admin);
        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setUnreadForAdmin(0);

        conversationRepository.save(conversation);

        // ===== NOTIFY USER =====
        messagingTemplate.convertAndSend(
                "/queue/user/" + conversation.getUser().getId() + "/claimed",
                conversationId
        );
    }



    @Transactional
    public void transferConversation(Long conversationId, Long fromAdminId,
                                     Long toAdminId, String reason) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

//        if (!conversation.getAssignedAdminId().equals(fromAdminId)) {
//            throw new RuntimeException("Not authorized");
//        }

//        conversation.setAssignedAdminId(toAdminId);
        conversationRepository.save(conversation);

        // Gửi system message
        User systemUser = userRepository.findById(fromAdminId).orElseThrow();
        Message transferMsg = new Message();
        transferMsg.setConversation(conversation);
        transferMsg.setSender(systemUser);
        transferMsg.setContent("Conversation transferred: " + reason);
        transferMsg.setType(MessageType.SYSTEM);
        transferMsg.setStatus(MessageStatus.SENT);
        messageRepository.save(transferMsg);

        // Thông báo
        messagingTemplate.convertAndSend("/queue/admin/" + toAdminId + "/new", conversationId);
    }

    private ConversationDTO convertToConversationDTO(Conversation conversation) {

        ConversationDTO dto = new ConversationDTO();

        // ===== BASIC INFO =====
        dto.setId(conversation.getId());
        dto.setBookingId(conversation.getBookingId());
        dto.setStatus(conversation.getStatus().name());
        dto.setUnreadCount(conversation.getUnreadForAdmin());

        // ===== USER INFO (người mở conversation) =====
        if (conversation.getUser() != null) {
            dto.setUserId(conversation.getUser().getId());
            dto.setUserName(conversation.getUser().getFullname());
            dto.setUserEmail(conversation.getUser().getEmail());
        }

        // ===== ADMIN INFO (admin được assign, có thể null) =====
        dto.setAssignedAdminId(
                conversation.getAdmin() != null
                        ? conversation.getAdmin().getId()
                        : null
        );

        // ===== LAST MESSAGE =====
        Message lastMessage = messageRepository
                .findLastMessage(conversation.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);

        if (lastMessage != null) {
            dto.setLastMessageContent(lastMessage.getContent());
            dto.setLastMessageTime(lastMessage.getCreatedAt());
            dto.setLastMessageSenderId(lastMessage.getSender().getId());
            dto.setLastMessageSenderName(lastMessage.getSender().getFullname());
        }

        return dto;
    }

    public int countUnreadForUser(Long userId) {
        return conversationRepository.sumUnreadConversationForUser(userId);
    }

    public int countUnreadForAdmin(Long adminId) {
        return conversationRepository.sumUnreadConversationForAdmin(adminId);
    }


    @Transactional(readOnly = true)
    public CustomerInfoDTO getCustomerInfo(Long conversationId, Long adminId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User user = conversation.getUser();

        CustomerInfoDTO dto = new CustomerInfoDTO();
        dto.setUserId(user.getId());
        dto.setUserName(user.getFullname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhoneNumber());

        // 🔥 ONLINE REAL
        dto.setOnline(userPresenceService.isOnline(user.getId()));

        return dto;
    }




}