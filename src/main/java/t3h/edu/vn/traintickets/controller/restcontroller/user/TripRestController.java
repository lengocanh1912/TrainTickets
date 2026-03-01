package t3h.edu.vn.traintickets.controller.restcontroller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import t3h.edu.vn.traintickets.dto.BookingRequest;
import t3h.edu.vn.traintickets.dto.BookingResponse;
import t3h.edu.vn.traintickets.dto.TripDetailDto;
import t3h.edu.vn.traintickets.service.booking.BookingService;
import t3h.edu.vn.traintickets.service.TripDtoService;

import java.security.Principal;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripRestController {

    private final TripDtoService tripDtoService;
    private final BookingService bookingService;

    @GetMapping("/{id}/detail")
    public ResponseEntity<TripDetailDto> getTripDetail(@PathVariable Long id) {
        return ResponseEntity.ok(tripDtoService.getTripDetail(id));
    }

    @PostMapping("/{tripId}/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable Long tripId,
            @RequestBody BookingRequest request,
            Principal principal
    ) {
        BookingResponse response =
                bookingService.createBooking(tripId, request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}