//package t3h.edu.vn.traintickets.service.impl;
//
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import t3h.edu.vn.traintickets.dto.CoachDetailDto;
//import t3h.edu.vn.traintickets.dto.SeatDetailDto;
//import t3h.edu.vn.traintickets.dto.TripDetailResponseDto;
//import t3h.edu.vn.traintickets.entities.Coach;
//import t3h.edu.vn.traintickets.entities.Train;
//import t3h.edu.vn.traintickets.entities.Trip;
//import t3h.edu.vn.traintickets.repository.TripRepository;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class TripServiceImpl implements TripService {
//
//    @Autowired
//    private final TripRepository tripRepository;
//
//    @Override
//    public Trip getTripDetail(Long tripId) {
//        Trip trip = tripRepository.findTripWithTrainAndCoaches(tripId)
//                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
//
//        // Load lazy seats manually (to avoid LazyInitializationException)
//        for (Coach coach : trip.getTrain().getCoaches()) {
//            coach.getSeats().size(); // force lazy fetch
//        }
//
//        return trip;
//    }
//}
//
//
