package t3h.edu.vn.traintickets.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.TripCreateDto;
import t3h.edu.vn.traintickets.dto.TripDto;
import t3h.edu.vn.traintickets.dto.TripUpdateDto;
import t3h.edu.vn.traintickets.entities.Route;
import t3h.edu.vn.traintickets.entities.Train;
import t3h.edu.vn.traintickets.entities.Trip;
import t3h.edu.vn.traintickets.repository.RouteRepository;
import t3h.edu.vn.traintickets.repository.TripRepository;
import t3h.edu.vn.traintickets.repository.TrainRepository;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TransferQueue;
import java.util.stream.Collectors;

@Service
public class TripService {
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private TrainRepository trainRepository;
    @Autowired
    private RouteRepository routeRepository;

    public List<Trip> searchTripsByRoute(String keyword) {
        return tripRepository.searchByRouteName(keyword);
    }
    public Page paging(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Trip> trips = tripRepository.findAll(pageable);
        return trips;
    }

    public void deleteById(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new IllegalArgumentException("Trip không tồn tại");
        }
        tripRepository.deleteById(id);
    }


    public List<Trip> getAll() {
        List<Trip> trips = null;
        try {
            trips = tripRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return trips;
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

    public void createTrip(TripCreateDto dto) {
        if (tripRepository.existsByTrainAndTimeOverlap(dto.getTrainId(), dto.getDepartureAt(), dto.getArrivalAt())) {
            throw new IllegalArgumentException("Tàu đã được sử dụng trong khung giờ này.");
        }

        Trip trip = new Trip();
        trip.setTrain(trainRepository.getReferenceById(dto.getTrainId()));
        trip.setRoute(routeRepository.getReferenceById(dto.getRouteId()));
        trip.setDepartureAt(dto.getDepartureAt());
        trip.setArrivalAt(dto.getArrivalAt());
        trip.setPrice(dto.getPrice());
        trip.setStatus(1==1);
        trip.setCreatedAt(Instant.now());

        tripRepository.save(trip);
    }

    public List<TripDto> findAllDto() {
        return tripRepository.findAll().stream().map(t -> {
            return new TripDto(
                    t.getId(),
                    t.getTrain().getName(),
                    t.getRoute().getDeparture().getName(),
                    t.getRoute().getArrival().getName(),
                    t.getPrice(),
                    t.getDepartureAt(),
                    t.getArrivalAt()
            );
        }).collect(Collectors.toList());
    }


    public TripUpdateDto findById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip không tồn tại với ID: " + id));

        TripUpdateDto dto = new TripUpdateDto();
        dto.setId(trip.getId());
        dto.setTrainId(trip.getTrain().getId());
        dto.setRouteId(trip.getRoute().getId());
        dto.setDepartureAt(trip.getDepartureAt());
        dto.setArrivalAt(trip.getArrivalAt());
        dto.setPrice(trip.getPrice());
        dto.setStatus(trip.getStatus());

        return dto;
    }

    public void updateTrip(TripUpdateDto dto) {
        Trip trip = tripRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chuyến đi với ID: " + dto.getId()));

        // Cập nhật lại thông tin chuyến đi
        Train train = trainRepository.findById(dto.getTrainId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tàu"));

        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tuyến đường"));

        trip.setTrain(train);
        trip.setRoute(route);
        trip.setDepartureAt(dto.getDepartureAt());
        trip.setArrivalAt(dto.getArrivalAt());
        trip.setPrice(dto.getPrice());
        trip.setStatus(dto.getStatus());

        // Lưu lại
        tripRepository.save(trip);
    }

}