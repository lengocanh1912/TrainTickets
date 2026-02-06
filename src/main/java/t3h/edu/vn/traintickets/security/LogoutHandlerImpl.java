package t3h.edu.vn.traintickets.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import t3h.edu.vn.traintickets.service.UserPresenceService;

@Component
public class LogoutHandlerImpl implements LogoutHandler {

    private final UserPresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public LogoutHandlerImpl(
            UserPresenceService presenceService,
            SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        if (authentication != null &&
                authentication.getPrincipal() instanceof UserDetailsImpl user) {

            presenceService.offline(user.getId());

            messagingTemplate.convertAndSend(
                    "/topic/presence",
                    user.getId()
            );
        }
    }
}
