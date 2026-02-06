package t3h.edu.vn.traintickets.controller.restcontroller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t3h.edu.vn.traintickets.dto.TrainCreateDto;
import t3h.edu.vn.traintickets.service.TrainService;

@RestController
@RequestMapping("/api/admin/trains")
@RequiredArgsConstructor
public class TrainRestController {

    private final TrainService trainService;

    @PostMapping
    public ResponseEntity<?> createTrain(@RequestBody TrainCreateDto dto) {
        try {
            trainService.createTrain(dto);
            return ResponseEntity.ok("Tạo tàu thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Tạo tàu thất bại: " + e.getMessage());
        }
    }
}

