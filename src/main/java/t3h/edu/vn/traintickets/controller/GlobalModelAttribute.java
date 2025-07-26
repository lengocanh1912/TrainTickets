package t3h.edu.vn.traintickets.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import t3h.edu.vn.traintickets.config.UserDetailServiceImpl;
import t3h.edu.vn.traintickets.entities.Station;
import t3h.edu.vn.traintickets.entities.User;
import t3h.edu.vn.traintickets.service.StationService;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttribute {

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailServiceImpl.UserDetailImpl userDetails) {
                return userDetails.getUser();
            }
        }
        return null;
    }

    @Autowired
    StationService stationService;
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

