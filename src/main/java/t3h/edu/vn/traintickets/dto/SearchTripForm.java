package t3h.edu.vn.traintickets.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class SearchTripForm {
    private String tripType; // "oneway" hoặc "roundtrip"
    private String departureName;
    private String arrivalName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    private int adultQuantity;    // Người lớn (11 - 59 tuổi)
    private int childQuantity;    // Trẻ em (6 - 10 tuổi)
    private int seniorQuantity;   // Người cao tuổi (60+)
    private int studentQuantity;  // Sinh viên (có thẻ SV)

    // Tính tổng vé từ từng loại vé
    public int getTicketQuantity() {
        return adultQuantity + childQuantity + seniorQuantity + studentQuantity;
    }

    // getter & setter

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getDepartureName() {
        return departureName;
    }

    public void setDepartureName(String departureName) {
        this.departureName = departureName;
    }

    public String getArrivalName() {
        return arrivalName;
    }

    public void setArrivalName(String arrivalName) {
        this.arrivalName = arrivalName;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getAdultQuantity() {
        return adultQuantity;
    }

    public void setAdultQuantity(int adultQuantity) {
        this.adultQuantity = adultQuantity;
    }

    public int getChildQuantity() {
        return childQuantity;
    }

    public void setChildQuantity(int childQuantity) {
        this.childQuantity = childQuantity;
    }

    public int getSeniorQuantity() {
        return seniorQuantity;
    }

    public void setSeniorQuantity(int seniorQuantity) {
        this.seniorQuantity = seniorQuantity;
    }

    public int getStudentQuantity() {
        return studentQuantity;
    }

    public void setStudentQuantity(int studentQuantity) {
        this.studentQuantity = studentQuantity;
    }
}