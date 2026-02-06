package t3h.edu.vn.traintickets.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        if (exception instanceof UsernameNotFoundException) {
            response.sendRedirect("/trainticket/login?error=username");
        } else if (exception instanceof BadCredentialsException) {
            response.sendRedirect("/trainticket/login?error=password");
        } else {
            response.sendRedirect("/trainticket/login?error=unknown");
        }
    }
}

