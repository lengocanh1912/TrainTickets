package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.entities.Seat;
import t3h.edu.vn.traintickets.repository.SeatRepository;

import java.util.List;

@Service
public class SeatService {
    @Autowired
    private SeatRepository seatRepository;
    public List<Seat> findByCoachId(Long id) {
        List<Seat> seat = null;
        try{
            seat = seatRepository.findByCoachId(id);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return seat;
    }
}
