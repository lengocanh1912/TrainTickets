package t3h.edu.vn.traintickets.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import t3h.edu.vn.traintickets.dto.UserCreateDto;

import t3h.edu.vn.traintickets.dto.UserPasswordDto;
import t3h.edu.vn.traintickets.security.UserDetailsImpl;
import t3h.edu.vn.traintickets.service.UserPresenceService;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserPresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public CustomAuthenticationSuccessHandler(
            UserPresenceService presenceService,
            SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // ===============================
        // 1️⃣ LƯU SESSION (GIỮ NGUYÊN)
        // ===============================
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        UserPasswordDto userPassword = new UserPasswordDto();
        userPassword.setNewPassword(userDetails.getPassword());

        HttpSession session = request.getSession();
        session.setAttribute("user", userPassword);

        // ===============================
        // 3️⃣ REDIRECT (GIỮ NGUYÊN 100%)
        // ===============================
        String redirectAfterLogin =
                (String) session.getAttribute("REDIRECT_AFTER_LOGIN");

        if (redirectAfterLogin != null) {
            session.removeAttribute("REDIRECT_AFTER_LOGIN");
            response.sendRedirect(redirectAfterLogin);
            return;
        }

        String redirectParam = request.getParameter("redirect");
        if (redirectParam != null && !redirectParam.isEmpty()) {
            response.sendRedirect(redirectParam);
            return;
        }

        SavedRequest savedRequest =
                new HttpSessionRequestCache()
                        .getRequest(request, response);

        if (savedRequest != null) {
            response.sendRedirect(savedRequest.getRedirectUrl());
            return;
        }

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if ("ROLE_ADMIN".equals(role)
                    || "ROLE_EMPLOYEE".equals(role)) {
                response.sendRedirect("/trainticket/admin/home");
                return;
            }

            if ("ROLE_CUSTOMER".equals(role)) {
                response.sendRedirect("/trainticket/user/home");
                return;
            }
        }

        response.sendRedirect("/trainticket/");
    }
}

