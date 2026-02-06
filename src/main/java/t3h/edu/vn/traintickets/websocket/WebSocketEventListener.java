package t3h.edu.vn.traintickets.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import t3h.edu.vn.traintickets.service.UserPresenceService;

@Component
public class WebSocketEventListener {

    private final UserPresenceService presenceService;

    public WebSocketEventListener(UserPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String userIdStr =
                accessor.getFirstNativeHeader("userId");

        if (userIdStr != null) {
            accessor.getSessionAttributes()
                    .put("userId", userIdStr);

            presenceService.online(Long.parseLong(userIdStr));
            System.out.println("🟢 USER ONLINE: " + userIdStr);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        Object userIdObj =
                accessor.getSessionAttributes().get("userId");

        System.out.println("❌ WS DISCONNECT userId=" + userIdObj);

        if (userIdObj != null) {
            presenceService.offline(Long.parseLong(userIdObj.toString()));
        }
    }


}

