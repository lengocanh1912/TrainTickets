package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t3h.edu.vn.traintickets.dto.CoachDto;
import t3h.edu.vn.traintickets.dto.TripDetailDto;
import t3h.edu.vn.traintickets.dto.TripDto;
import t3h.edu.vn.traintickets.dto.SeatDto;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.repository.*;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripDtoService {
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TrainRepository trainRepository;
    @Autowired
    private TicketRepository ticketRepository;

    public List<TripDto> getAllTripDtos(){
        List<TripDto> tripList = null;
        try {
            tripList = tripRepository.getAllTripDtos();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tripList;
    }

    public List<TripDto> findByIdDto(Long id) {
        List<TripDto> trip = null;
        try {
            trip = tripRepository.findByIdDto(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return trip;
    }
    public List<TripDto> findTripsByStationNames(String departureName, String arrivalName, LocalDate departureDate, int ticketQuantity) {
        List<TripDto> tripDtoList = null;
        try{
            tripDtoList = tripRepository.findTripsByStationNames(departureName, arrivalName, departureDate, ticketQuantity);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return tripDtoList;
    }

    // TripDtoServiceImpl.java
    @Transactional
    public TripDetailDto getTripDetail(Long tripId) {
        // 1. Tìm chuyến đi
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi với ID = " + tripId));

        Train train = trip.getTrain();
        Route route = trip.getRoute();

        // 2. Lấy danh sách toa theo tàu
        List<Coach> coaches = coachRepository.findByTrainId(train.getId());

        // 3. Tạo danh sách CoachDto
        List<CoachDto> coachDtos = coaches.stream().map(coach -> {
            // 3.1 Lấy danh sách ghế theo coach
            List<Seat> seats = seatRepository.findByCoachId(coach.getId());

            // 3.2 Convert sang SeatDto
            List<SeatDto> seatDtos = seats.stream().map(seat -> {
                boolean booked = ticketRepository.existsBySeatIdAndTripId(seat.getId(), tripId);
                return new SeatDto(seat.getId(), seat.getSeatCode(), booked, trip.getPrice());
            }).toList();

            // 3.3 Tạo CoachDto
            CoachDto coachDto = new CoachDto();
            coachDto.setId(coach.getId());
            coachDto.setCode(coach.getCode());
            coachDto.setType(coach.getType()); // nếu coach.getType() là Enum
            coachDto.setCapacity(coach.getCapacity());
            coachDto.setState(coach.getState());
            coachDto.setPosition(coach.getPosition());
            coachDto.setSeats(seatDtos);

            return coachDto;
        }).toList();

        // 4. Tạo TripDetailDto
        TripDetailDto dto = new TripDetailDto();
        dto.setTrainName(train.getName());
        dto.setDepartureStation(route.getDeparture().getName());
        dto.setArrivalStation(route.getArrival().getName());
        dto.setDepartureDate(trip.getDepartureAt().toLocalDate().toString());
        dto.setCoaches(coachDtos);

        return dto;
    }


}
