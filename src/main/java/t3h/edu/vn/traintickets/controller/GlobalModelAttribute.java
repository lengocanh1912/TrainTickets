package t3h.edu.vn.traintickets.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import t3h.edu.vn.traintickets.entities.Station;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.repository.UserRepository;
import t3h.edu.vn.traintickets.security.UserDetailsImpl;
import t3h.edu.vn.traintickets.service.StationService;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttribute {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    StationService stationService;

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return userRepository.findByUsername(auth.getName());
    }
    @ModelAttribute("stations")
    public List<Station> getStations(Model model) {
        List<Station> stations = stationService.findAll();
        return stations;
    }
    @ModelAttribute("request")
    public HttpServletRequest getRequest(HttpServletRequest request) {
        return request;
    }

}

