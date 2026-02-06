package t3h.edu.vn.traintickets.controller.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import t3h.edu.vn.traintickets.dto.ConversationDTO;
import t3h.edu.vn.traintickets.dto.CustomerInfoDTO;
import t3h.edu.vn.traintickets.dto.MessageDTO;
import t3h.edu.vn.traintickets.entities.Conversation;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.repository.UserRepository;
import t3h.edu.vn.traintickets.security.UserDetailsImpl;
import t3h.edu.vn.traintickets.service.AuthService;
import t3h.edu.vn.traintickets.service.ChatService;
import t3h.edu.vn.traintickets.service.UserPresenceService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
//@RequiredArgsConstructor
public class ChatController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    private final ChatService chatService;
    @Autowired
    private UserPresenceService userPresenceService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // User chat interface
    @GetMapping("/user/support")
    public String userChat(Authentication auth,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        Long userId = getCurrentUserId(auth);
        System.out.println("🔍 userId: " + userId);

        Conversation conversation = chatService.getOrCreateConversation(userId);

        model.addAttribute("conversationId", conversation.getId());
        model.addAttribute("userId", userId);
        model.addAttribute("isAdmin", false);

        return "chat/user-chat";
    }

    @GetMapping("/user/support/unread-count")
    @ResponseBody
    public int getUnreadCount(Authentication authentication) {
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        return chatService.countUnreadForUser(userDetails.getId());
    }



    // Admin chat interface
    @GetMapping("/admin/support")
    public String adminChat(Authentication auth, Model model) {
        Long adminId = getCurrentUserId(auth);
        System.out.println( "🔍 adminid: " + adminId);
        model.addAttribute("adminId", adminId);
        model.addAttribute("isAdmin", true);
        return "chat/admin-chat";
    }

    @GetMapping("/admin/support/unread-count")
    @ResponseBody
    public int getUnreadForAdmin(Authentication auth) {
        Long adminId = getCurrentUserId(auth);
        return chatService.countUnreadForAdmin(adminId);
    }

    // Get messages for a conversation
    @GetMapping("/api/chat/{conversationId}/messages")
    @ResponseBody
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable Long conversationId,
            Authentication auth) {

        Long userId = getCurrentUserId(auth);
        List<MessageDTO> messages = chatService.getMessages(conversationId, userId);
        return ResponseEntity.ok(messages);
    }

    // Send message
    @PostMapping("/api/chat/{conversationId}/send")
    @ResponseBody
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable Long conversationId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String clientTempId,
            @RequestParam(required = false) MultipartFile file,
            Authentication auth) throws IOException {

        Long userId = getCurrentUserId(auth);
        MessageDTO message =
                chatService.sendMessage(conversationId, userId, content, file, clientTempId);
        return ResponseEntity.ok(message);
    }

    // Mark messages as read
    @PostMapping("/api/chat/{conversationId}/read")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long conversationId,
            Authentication auth) {

        Long userId = getCurrentUserId(auth);
        chatService.markAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    // WebSocket endpoints
    @MessageMapping("/chat.send")
    public void sendMessageWS(@Payload Map<String, Object> message) throws IOException {

        Long conversationId =
                Long.parseLong(message.get("conversationId").toString());

        Long userId =
                Long.parseLong(message.get("userId").toString());

        String content =
                (String) message.get("content");

        String clientTempId =
                (String) message.get("clientTempId"); // 🔥 THÊM

        chatService.sendMessage(
                conversationId,
                userId,
                content,
                null,
                clientTempId // 🔥 TRUYỀN XUỐNG
        );
    }


    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload Map<String, Object> data) {
        Long conversationId = Long.parseLong(data.get("conversationId").toString());
        Long userId = Long.parseLong(data.get("userId").toString());
        Boolean isTyping = (Boolean) data.get("isTyping");

        chatService.sendTypingIndicator(conversationId, userId, isTyping);
    }

    private Long getCurrentUserId(Authentication auth) {

        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Authentication is null");
        }

        if (auth.getPrincipal() instanceof UserDetails userDetails) {

            User user = userRepository.findByUsername(userDetails.getUsername());

            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return user.getId();
        }

        throw new RuntimeException("Invalid authentication type");
    }

    // Thêm vào ChatController.java
    // Admin: Lấy danh sách conversations
    @GetMapping("/api/admin/conversations")
    @ResponseBody
    public ResponseEntity<List<ConversationDTO>> getAdminConversations(Authentication auth) {
        Long adminId = getCurrentUserId(auth);
        List<ConversationDTO> conversations = chatService.getConversationsForAdmin(adminId);
        return ResponseEntity.ok(conversations);
    }

    // Admin: Đóng conversation
    @PostMapping("/api/admin/conversation/{conversationId}/close")
    @ResponseBody
    public ResponseEntity<Void> closeConversation(
            @PathVariable Long conversationId,
            Authentication auth) {

        Long adminId = getCurrentUserId(auth);
        chatService.closeConversation(conversationId, adminId);
        return ResponseEntity.ok().build();
    }

    // Admin: Claim conversation
    @PostMapping("/api/admin/conversation/{conversationId}/claim")
    @ResponseBody
    public ResponseEntity<Void> claimConversation(
            @PathVariable Long conversationId,
            Authentication auth) {

        Long adminId = getCurrentUserId(auth);
        chatService.claimConversation(conversationId, adminId);
        return ResponseEntity.ok().build();
    }

    // Admin: Transfer conversation
    @PostMapping("/api/chat/{conversationId}/transfer")
    @ResponseBody
    public ResponseEntity<Void> transferConversation(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> request,
            Authentication auth) {

        Long fromAdminId = getCurrentUserId(auth);
        Long toAdminId = Long.parseLong(request.get("toAdminId").toString());
        String reason = (String) request.get("reason");

        chatService.transferConversation(conversationId, fromAdminId, toAdminId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/conversation/{conversationId}/info")
    @ResponseBody
    public ResponseEntity<CustomerInfoDTO> getCustomerInfo(
            @PathVariable Long conversationId,
            Authentication auth) {

        Long adminId = getCurrentUserId(auth);

        CustomerInfoDTO dto =
                chatService.getCustomerInfo(conversationId, adminId);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/admin/user/{userId}/online")
    @ResponseBody
    public boolean isUserOnline(@PathVariable Long userId) {
        return userPresenceService.isOnline(userId);
    }


}
