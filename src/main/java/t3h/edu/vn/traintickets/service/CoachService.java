package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.CoachDto;
import t3h.edu.vn.traintickets.entities.Coach;
import t3h.edu.vn.traintickets.entities.Seat;
import t3h.edu.vn.traintickets.entities.Trip;
import t3h.edu.vn.traintickets.repository.CoachRepository;
import t3h.edu.vn.traintickets.repository.SeatRepository;
import t3h.edu.vn.traintickets.repository.TripRepository;

import java.util.List;

@Service
public class CoachService {
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private SeatRepository seatRepository;

    public List<Coach> findByTrainId(Long id) {
        List<Coach> coach = null;
        try{
            coach = coachRepository.findByTrainId(id);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return coach;
    }

}
