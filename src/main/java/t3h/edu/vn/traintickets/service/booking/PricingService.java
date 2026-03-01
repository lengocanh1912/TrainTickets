package t3h.edu.vn.traintickets.service.booking;

import org.springframework.stereotype.Service;
import t3h.edu.vn.traintickets.entities.Seat;
import t3h.edu.vn.traintickets.entities.Trip;

import java.math.BigDecimal;

@Service
public class PricingService {

    public BigDecimal calculatePrice(Trip trip,
                                     Seat seat,
                                     Byte ticketType) {

        BigDecimal basePrice = getBasePriceBySeatType(trip, seat.getType());

        return applyDiscount(basePrice, ticketType);
    }

    private BigDecimal getBasePriceBySeatType(Trip trip, String seatType) {

        // Ví dụ: giá phụ thuộc seat type
        switch (seatType) {
            case "vip":
                return trip.getPrice().multiply(BigDecimal.valueOf(1.2));
            case "standard":
            default:
                return trip.getPrice();
        }
    }

    private BigDecimal applyDiscount(BigDecimal price, Byte type) {

        if (type == 1) { // child
            return price.multiply(BigDecimal.valueOf(0.75));
        }
        if (type == 2) { // student
            return price.multiply(BigDecimal.valueOf(0.9));
        }
        if (type == 3) { // senior
            return price.multiply(BigDecimal.valueOf(0.85));
        }

        return price;
    }
}
