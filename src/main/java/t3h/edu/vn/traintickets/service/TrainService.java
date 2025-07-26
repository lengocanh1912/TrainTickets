package t3h.edu.vn.traintickets.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.dto.CoachDto;
import t3h.edu.vn.traintickets.dto.SeatDto;
import t3h.edu.vn.traintickets.dto.TrainCreateDto;
import t3h.edu.vn.traintickets.dto.TrainUpdateDto;
import t3h.edu.vn.traintickets.entities.*;
import t3h.edu.vn.traintickets.repository.CoachRepository;
import t3h.edu.vn.traintickets.repository.SeatRepository;
import t3h.edu.vn.traintickets.repository.TrainRepository;
import java.time.Instant;

import java.util.List;
import java.util.Optional;
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
        train.setCreatedAt(Instant.now());
        train.setUpdatedAt(Instant.now());

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
    public void updateTrain(Train updatedTrain) {
        // 1. Lấy train cũ từ DB
        Train existingTrain = trainRepository.findWithCoachesById(updatedTrain.getId());
        if (existingTrain == null) {
            throw new RuntimeException("Không tìm thấy tàu với ID = " + updatedTrain.getId());
        }

        // 2. Cập nhật thông tin cơ bản
        existingTrain.setName(updatedTrain.getName());
        existingTrain.setCode(updatedTrain.getCode());
        existingTrain.setUpdatedAt(Instant.now());

        // 3. Xóa toàn bộ coach và seat cũ
        for (Coach oldCoach : existingTrain.getCoaches()) {
            seatRepository.deleteAll(oldCoach.getSeats());
        }
        coachRepository.deleteAll(existingTrain.getCoaches());

        existingTrain.getCoaches().clear(); // clear danh sách trong entity để tránh lỗi Hibernate

        // 4. Lưu lại coach và seat mới từ updatedTrain
        int index = 1;
        for (Coach updatedCoach : updatedTrain.getCoaches()) {
            Coach newCoach = new Coach();
            newCoach.setCode(updatedCoach.getCode());
            newCoach.setType(updatedCoach.getType());
            newCoach.setCapacity(updatedCoach.getCapacity());
            newCoach.setState(updatedCoach.getState());
            newCoach.setPosition(index++);
            newCoach.setTrain(existingTrain);

            // Lưu coach trước để có ID cho seat
            Coach savedCoach = coachRepository.save(newCoach);

            // Gắn seat
            List<Seat> newSeats = updatedCoach.getSeats().stream().map(seat -> {
                Seat s = new Seat();
                s.setSeatCode(seat.getSeatCode());
                s.setType("standard");
                s.setCoach(savedCoach);
                return s;
            }).collect(Collectors.toList());

            seatRepository.saveAll(newSeats);
            savedCoach.setSeats(newSeats);

            existingTrain.getCoaches().add(savedCoach);
        }

        // 5. Cập nhật lại tổng số chỗ
        existingTrain.setCapacity(
                existingTrain.getCoaches().stream().mapToInt(Coach::getCapacity).sum()
        );

        trainRepository.save(existingTrain);
    }


    public void deleteById(Long id) {
        Optional<Train> train = trainRepository.findById(id);
        if (train.isPresent()) {
            trainRepository.deleteById(id);
        } else {
            throw new RuntimeException("Không tìm thấy tàu với ID = " + id);
        }
    }

    public Page paging(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Train> trains = trainRepository.findAll(pageable);
        return trains;
    }


    @Transactional
    public void updateTrainFromDto(TrainUpdateDto dto) {
        Train train = trainRepository.findWithCoachesById(dto.getId());
        if (train == null) {
            throw new RuntimeException("Không tìm thấy tàu");
        }

        train.setName(dto.getName());
        train.setCode(dto.getCode());
        train.setUpdatedAt(Instant.now());

        // Xóa coach và seat cũ
        for (Coach oldCoach : train.getCoaches()) {
            seatRepository.deleteAll(oldCoach.getSeats());
        }
        coachRepository.deleteAll(train.getCoaches());
        train.getCoaches().clear();

        // Tạo lại coach và seat mới
        int index = 1;
        for (CoachDto coachDto : dto.getCoaches()) {
            Coach coach = new Coach();
            coach.setCode(coachDto.getCode());
            coach.setType(coachDto.getType());
            coach.setCapacity(coachDto.getCapacity());
            coach.setPosition(index++);
            coach.setState(coachDto.getState());
            coach.setTrain(train);

            Coach savedCoach = coachRepository.save(coach);

            List<Seat> seats = coachDto.getSeats().stream()
                    .map(seatDto -> {
                        Seat seat = new Seat();
                        seat.setSeatCode(seatDto.getSeatCode());
                        seat.setType("standard");
                        seat.setCoach(savedCoach);
                        return seat;
                    })
                    .collect(Collectors.toList());

            seatRepository.saveAll(seats);
            savedCoach.setSeats(seats);
            train.getCoaches().add(savedCoach);
        }

        train.setCapacity(train.getCoaches().stream().mapToInt(Coach::getCapacity).sum());
        trainRepository.save(train);
    }

    @Transactional(readOnly = true)
    public TrainUpdateDto getTrainUpdateDtoById(Long id) {
        Train train = trainRepository.findWithCoachesById(id);
        if (train == null) {
            throw new RuntimeException("Không tìm thấy tàu với id: " + id);
        }

        // Lấy seats
        train.getCoaches().forEach(coach -> coach.getSeats().size());

        TrainUpdateDto dto = new TrainUpdateDto();
        dto.setId(train.getId());
        dto.setName(train.getName());
        dto.setCode(train.getCode());

        List<CoachDto> coachDtos = train.getCoaches().stream().map(coach -> {
            CoachDto c = new CoachDto();
            c.setId(coach.getId());
            c.setType(coach.getType());
            c.setCapacity(coach.getSeats().size());
            c.setCode(coach.getCode());
            c.setState(coach.getState());

            List<SeatDto> seatDtos = coach.getSeats().stream()
                    .map(seat -> new SeatDto(seat.getId(), seat.getSeatCode(), false, 0))
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


}
