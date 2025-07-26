package t3h.edu.vn.traintickets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.RouteCreateDto;
import t3h.edu.vn.traintickets.dto.RouteUpdateDto;
import t3h.edu.vn.traintickets.entities.Route;
import t3h.edu.vn.traintickets.repository.RouteRepository;
import t3h.edu.vn.traintickets.repository.StationRepository;

import java.time.Instant;
import java.util.List;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StationRepository stationRepository;

    public List<Route> searchRoutes(String keyword) {
        return routeRepository.searchByKeyword(keyword);
    }

//    public Page<Route> paging(int page, int perpage) {
//        Pageable pageable = PageRequest.of(page, perpage, Sort.by("id").descending());
//        return routeRepository.findAll(pageable); // dùng EntityGraph
//    }
    public Page<Route> paging(int page, int perpage) {
        Pageable pageable = PageRequest.of(page, perpage );
        return routeRepository.findAll(pageable); // dùng EntityGraph
    }

    public List<Route> findAll() {
        List<Route> routes = null;
        try {
            routes = routeRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return routes;
    }

    public void create(RouteCreateDto dto) {
        Route route = new Route();
        route.setDeparture(stationRepository.findById(dto.getDepartureId()).orElseThrow());
        route.setArrival(stationRepository.findById(dto.getArrivalId()).orElseThrow());
//        route.setDuration(dto.getDuration());
        route.setDistanceKm(dto.getDistanceKm());
        route.setCreatedAt(Instant.now());
        routeRepository.save(route);
    }

    public void update(RouteUpdateDto dto) {
        Route route = routeRepository.findById(dto.getId()).orElseThrow();
        route.setDeparture(stationRepository.findById(dto.getDepartureId()).orElseThrow());
        route.setArrival(stationRepository.findById(dto.getArrivalId()).orElseThrow());
//        route.setDuration(dto.getDuration());
        route.setDistanceKm(dto.getDistanceKm());
        route.setUpdatedAt(Instant.now());
        routeRepository.save(route);
    }

    public RouteUpdateDto findByIdForUpdate(Long id) {
        Route route = routeRepository.findById(id).orElseThrow();
        RouteUpdateDto dto = new RouteUpdateDto();
        dto.setId(route.getId());
        dto.setDepartureId(route.getDeparture().getId());
        dto.setArrivalId(route.getArrival().getId());
        dto.setDistanceKm(route.getDistanceKm());
//        dto.setDuration(route.getDuration());
        return dto;
    }

    public Route findById(Long id) {
        return routeRepository.findById(id).orElseThrow();
    }

    public void deleteById(Long id) {
        routeRepository.deleteById(id);
    }
}
