package t3h.edu.vn.traintickets.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.*;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.enums.CoachState;
import t3h.edu.vn.traintickets.enums.SeatStatus;
import t3h.edu.vn.traintickets.enums.TrainState;
import t3h.edu.vn.traintickets.repository.CoachRepository;
import t3h.edu.vn.traintickets.repository.SeatRepository;
import t3h.edu.vn.traintickets.repository.TrainRepository;
import java.time.Instant;

import java.util.stream.Collectors;

@Service
public class TrainService {
    @Autowired
    TrainRepository trainRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private SeatRepository seatRepository;

    public Train findByName(String name) {
        return trainRepository.findByName(name);
    }

    public List<Train> findAll() {
        List<Train> trains = null;
        try {
            trains = trainRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return trains;
    }

//    @Transactional
//    public void createTrain(TrainCreateDto dto) {
//        Train train = new Train();
//        train.setName(dto.getName());
//        train.setCode(dto.getCode());
//        train.setCapacity(dto.getCoaches().stream().mapToInt(CoachDto::getCapacity).sum());
//        train.setCreatedAt(Instant.now());
//        train.setUpdatedAt(Instant.now());
//
//        List<Coach> coachList = new ArrayList<>();
//        int index = 1;
//
//        for (CoachDto coachDto : dto.getCoaches()) {
//            Coach coach = new Coach();
//            coach.setCode(coachDto.getCode());
//            coach.setType(coachDto.getType());
//            coach.setCapacity(coachDto.getCapacity());
//            coach.setPosition(index++);
//            coach.setTrain(train);
//
//            List<Seat> seats = coachDto.getSeats().stream().map(code -> {
//                Seat seat = new Seat();
//                seat.setSeatCode(code);
//                seat.setType("standard");
//                seat.setCoach(coach);
//                return seat;
//            }).collect(Collectors.toList());
//
//            coach.setSeats(seats);
//            coachList.add(coach);
//        }
//
//        train.setCoaches(coachList);
//        trainRepository.save(train); // tự cascade coach & seat
//    }

    @Transactional
    public void createTrain(TrainCreateDto dto) {
        // 1. Save train trước
        Train train = new Train();
        train.setName(dto.getName());
        train.setCode(dto.getCode());
        train.setCapacity(dto.getCoaches().stream().mapToInt(CoachDto::getCapacity).sum());
        train.setState(TrainState.ACTIVE);
        train.setCreatedAt(LocalDateTime.now());
//        train.setUpdatedAt(Instant.now());

        Train savedTrain = trainRepository.save(train);

        int index = 1;
        for (CoachDto coachDto : dto.getCoaches()) {
            // 2. Save coach
            Coach coach = new Coach();
            coach.setCode(coachDto.getCode());
            coach.setType(coachDto.getType());
            coach.setCapacity(coachDto.getCapacity());
            coach.setPosition(index++);
            coach.setTrain(savedTrain);

            Coach savedCoach = coachRepository.save(coach);

            // 3. Save seats
            List<Seat> seats = coachDto.getSeats().stream().map(seatDto -> {
                Seat seat = new Seat();
                seat.setSeatCode(seatDto.getSeatCode());
                seat.setType("standard"); // hoặc seatDto.getType()
                seat.setCoach(savedCoach);
                return seat;
            }).collect(Collectors.toList());

            seatRepository.saveAll(seats);
        }
    }

    @Transactional
    public Train findById(Long id) {
        Train train = null;
        try {
            train = trainRepository.findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return train;
    }

    @Transactional
    public void updateTrain(TrainUpdateDto dto) {

        Train train = trainRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Train not found"));

        train.setName(dto.getName());
        train.setCode(dto.getCode());
        train.setUpdatedAt(LocalDateTime.now());

        int totalCapacity = 0;

        if (dto.getCoaches() == null) return;

        for (CoachDto coachDto : dto.getCoaches()) {

            Coach coach = coachRepository.findById(coachDto.getId())
                    .orElseThrow(() -> new RuntimeException("Coach not found"));

            coach.setCode(coachDto.getCode());
            coach.setType(coachDto.getType());
            coach.setState(coachDto.getState());

            if (coach.getState() == CoachState.INACTIVE) {
                deactivateAllSeats(coach);   // tất cả seat → INACTIVE
                continue;                    // ❌ KHÔNG sync, ❌ KHÔNG đổi capacity
            }

            coach.setCapacity(coachDto.getCapacity());
            totalCapacity += coachDto.getCapacity();

            syncSeatsSafe(coach, coachDto.getCapacity());
        }

        train.setCapacity(totalCapacity);
    }

    private void syncSeatsSafe(Coach coach, int newCapacity) {

        List<Seat> seats = coach.getSeats();
        String coachCode = coach.getCode();

        List<Seat> validSeats = seats.stream()
                .sorted(Comparator.comparingInt(this::extractSeatNumber))
                .toList();

        int index = 1;

        for (Seat seat : validSeats) {

            if (seat.getStatus() == SeatStatus.BOOKED) {
                index++; // giữ nguyên số ghế đã bán
                continue;
            }

            if (index <= newCapacity) {
                seat.setSeatCode(coachCode + index);   // V31, V32
                seat.setStatus(SeatStatus.AVAILABLE);
            } else {
                seat.setStatus(SeatStatus.INACTIVE);
            }
            index++;
        }

        while (index <= newCapacity) {
            Seat seat = new Seat();
            seat.setCoach(coach);
            seat.setSeatCode(coachCode + index);
            seat.setStatus(SeatStatus.AVAILABLE);
            seats.add(seat);
            index++;
        }
    }

    private void deactivateAllSeats(Coach coach) {
        for (Seat seat : coach.getSeats()) {
            if (seat.getStatus() != SeatStatus.BOOKED) {
                seat.setStatus(SeatStatus.INACTIVE);
            }
        }
    }

    private int extractSeatNumber(Seat seat) {
        if (seat.getSeatCode() == null) return Integer.MAX_VALUE;

        String code = seat.getSeatCode();          // V31 | V3-1
        String coachCode = seat.getCoach().getCode(); // V3

        try {
            // CASE 1: có dấu "-"  → V3-12
            if (code.contains("-")) {
                return Integer.parseInt(code.substring(code.lastIndexOf("-") + 1));
            }

            // CASE 2: không có "-" → V312
            if (code.startsWith(coachCode)) {
                return Integer.parseInt(code.substring(coachCode.length()));
            }

            return Integer.MAX_VALUE;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }



    public void deleteById(Long id) {
        Optional<Train> train = trainRepository.findById(id);
        if (train.isPresent()) {
            trainRepository.deleteById(id);
        } else {
            throw new RuntimeException("Không tìm thấy tàu với ID = " + id);
        }
    }

    @Transactional(readOnly = true)
    public Page<TrainDto> paging(int pageNo, int perpage) {

        Pageable pageable = PageRequest.of(pageNo, perpage);
        Page<Train> trains = trainRepository.findAllWithCoaches(pageable);

        return trains.map(this::convertToDto);
    }




//    public Page<TrainDto> paging(int pageNo, int perPage) {
//        Pageable pageable = PageRequest.of(pageNo, perPage);
//
//        // Lấy page ID trước
//        Page<Long> trainIds = trainRepository.findAll(pageable).map(Train::getId);
//
//        // Sau đó load full data bằng JOIN FETCH
//        List<Train> trains = trainRepository.findAllWithCoachesByIds(trainIds.getContent());
//
//        // Convert sang DTO
//        List<TrainDto> dtos = trains.stream()
//                .map(this::convertToDto)
//                .toList();
//
//        // Trả về Page thủ công
//        return new PageImpl<>(dtos, pageable, trainIds.getTotalElements());
//    }


    // lấy ttin tàu muốn chỉnh sửa
    @Transactional(readOnly = true)
    public TrainUpdateDto getTrainUpdateDtoById(Long id) {
        Train train = trainRepository.findTrainAndCoachesById(id); // query 1

        if (train == null) {
            throw new RuntimeException("Không tìm thấy tàu với id: " + id);
        }

        // Query 2: load đầy đủ Coach có ghế
        List<Coach> coachesWithSeats = coachRepository.findCoachesWithSeatsByTrainId(id);

        // Convert sang map cho tiện nối ghế
        Map<Long, List<Seat>> coachIdToSeats = coachesWithSeats.stream()
                .collect(Collectors.toMap(
                        Coach::getId,
                        Coach::getSeats
                ));

        TrainUpdateDto dto = new TrainUpdateDto();
        dto.setId(train.getId());
        dto.setName(train.getName());
        dto.setCode(train.getCode());

        List<CoachDto> coachDtos = train.getCoaches().stream().map(coach -> {
            CoachDto c = new CoachDto();
            c.setId(coach.getId());
            c.setType(coach.getType());
            c.setCode(coach.getCode());
            c.setState(coach.getState());
            c.setPosition(coach.getPosition());
            c.setCapacity(coach.getCapacity());

            List<Seat> seats = coachIdToSeats.getOrDefault(coach.getId(), Collections.emptyList());
//            c.setCapacity(seats.size());

            List<SeatDto> seatDtos = seats.stream()
                    .map(seat -> new SeatDto(seat.getId(), seat.getSeatCode() ))
                    .collect(Collectors.toList());
            c.setSeats(seatDtos);

            return c;
        }).collect(Collectors.toList());

        dto.setCoaches(coachDtos);
        return dto;
    }

    public List<Train> searchTrains(String keyword) {
        return trainRepository.searchByKeyword(keyword);
    }

    private TrainDto convertToDto(Train train) {
        TrainDto dto = new TrainDto();

        dto.setId(train.getId());
        dto.setName(train.getName());
        dto.setCode(train.getCode());
        dto.setCapacity(train.getCapacity());
        dto.setState(train.getState());
        dto.setCreatedAt(train.getCreatedAt());
        dto.setUpdatedAt(train.getUpdatedAt());

        if (train.getCoaches() != null) {
            dto.setCoaches(
                    train.getCoaches().stream()
                            .sorted(Comparator.comparingInt(Coach::getPosition))
                            .map(c -> {
                                CoachDto cd = new CoachDto();
                                cd.setId(c.getId());
                                cd.setCode(c.getCode());
                                cd.setCapacity(c.getCapacity());
                                cd.setType(c.getType());
                                cd.setPosition(c.getPosition());
                                return cd;
                            })
                            .toList()
            );
        }

        return dto;
    }


}
