package t3h.edu.vn.traintickets.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SeatAlreadyBookedException.class)
    public ResponseEntity<?> handleSeatConflict(SeatAlreadyBookedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

}