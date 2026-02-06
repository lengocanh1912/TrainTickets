package t3h.edu.vn.traintickets.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.TripCreateDto;
import t3h.edu.vn.traintickets.dto.TripDto;
import t3h.edu.vn.traintickets.dto.TripUpdateDto;
import t3h.edu.vn.traintickets.entities.Route;
import t3h.edu.vn.traintickets.entities.Train;
import t3h.edu.vn.traintickets.entities.Trip;
import t3h.edu.vn.traintickets.enums.TripState;
import t3h.edu.vn.traintickets.repository.RouteRepository;
import t3h.edu.vn.traintickets.repository.TripRepository;
import t3h.edu.vn.traintickets.repository.TrainRepository;
import t3h.edu.vn.traintickets.utils.PriceUtils;


import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public Page<Trip> paging(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return tripRepository.findAllWithDetails(pageable);
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
        // 1. Kiểm tra trùng lịch tàu
        if (tripRepository.existsByTrainAndTimeOverlap(dto.getTrainId(), dto.getDepartureAt(), dto.getArrivalAt())) {
            throw new IllegalArgumentException("Tàu đã được sử dụng trong khung giờ này.");
        }

        // 2. Kiểm tra thời gian đến phải sau thời gian đi
        if (dto.getArrivalAt().isBefore(dto.getDepartureAt()) || dto.getArrivalAt().isEqual(dto.getDepartureAt())) {
            throw new IllegalArgumentException("Thời gian đến phải sau thời gian xuất phát.");
        }

        // 3. Khởi tạo trip
        Trip trip = new Trip();
        trip.setTrain(trainRepository.getReferenceById(dto.getTrainId()));
        trip.setRoute(routeRepository.getReferenceById(dto.getRouteId()));
        trip.setDepartureAt(dto.getDepartureAt());
        trip.setArrivalAt(dto.getArrivalAt());

        // 4. Xử lý giá
        String rawPrice = dto.getPrice();
        BigDecimal numericPrice = new BigDecimal(rawPrice.replace(".", ""));
        trip.setPrice(numericPrice);

        trip.setStatus(TripState.ACTIVE);
        trip.setCreatedAt(LocalDateTime.now());

        // 5. Lưu DB
        tripRepository.save(trip);
    }


    public void updateTrip(TripUpdateDto dto) {
        Trip trip = tripRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chuyến đi với ID: " + dto.getId()));

        Train train = trainRepository.findById(dto.getTrainId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tàu"));

        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tuyến đường"));

        // ✅ Kiểm tra thời gian đến phải sau thời gian đi
        if (dto.getArrivalAt().isBefore(dto.getDepartureAt()) || dto.getArrivalAt().isEqual(dto.getDepartureAt())) {
            throw new IllegalArgumentException("Thời gian đến phải sau thời gian xuất phát.");
        }

        // ✅ Kiểm tra trùng lịch với các chuyến khác (tránh đè lên một chuyến khác)
        boolean isOverlapping = tripRepository.existsByTrainAndTimeOverlapExceptCurrent(
                dto.getTrainId(), dto.getDepartureAt(), dto.getArrivalAt(), dto.getId()
        );
        if (isOverlapping) {
            throw new IllegalArgumentException("Tàu đã có chuyến khác trong khung giờ này.");
        }

        // ✅ Cập nhật dữ liệu
        trip.setTrain(train);
        trip.setRoute(route);
        trip.setDepartureAt(dto.getDepartureAt());
        trip.setArrivalAt(dto.getArrivalAt());

        // ✅ Xử lý giá
        String rawPrice = dto.getPrice();
        BigDecimal numericPrice = new BigDecimal(rawPrice.replace(".", ""));
        trip.setPrice(numericPrice);

        trip.setStatus(dto.getStatus());
        trip.setUpdatedAt(LocalDateTime.now());

        tripRepository.save(trip);
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
        // set price as formatted string (no currency), e.g. "300.000"
        dto.setPrice(PriceUtils.formatNoCurrency(trip.getPrice()));
        dto.setStatus(trip.getStatus());

        return dto;
    }


    public List<TripDto> findAllDto() {
        return tripRepository.findAll().stream().map(t -> {
            return new TripDto(
                    t.getId(),
                    t.getTrain().getName(),
                    t.getRoute().getDeparture().getName(),
                    t.getRoute().getArrival().getName(),
                    t.getPrice(), // BigDecimal
                    t.getDepartureAt(),
                    t.getArrivalAt()
            );
        }).collect(Collectors.toList());
    }



}