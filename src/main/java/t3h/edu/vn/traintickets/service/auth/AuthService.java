package t3h.edu.vn.traintickets.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.security.UserDetailsImpl;

@Service
@RequiredArgsConstructor
public class AuthService {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        return ((UserDetailsImpl) auth.getPrincipal()).getUser();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}


