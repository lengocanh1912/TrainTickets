package t3h.edu.vn.traintickets.enums;

public enum TicketStatus {

    PENDING,     // Chờ thanh toán - warning
    PAID,        // Đã thanh toán - success
    CHECKED_IN,  // Đã xác nhận lên tàu (nếu có) - info
    USED,        // Đã sử dụng - secondary
    CANCELLED,   // Đã huỷ - danger
    EXPIRED,     // Quá hạn - warning / dark
    REFUNDED,    // Đã hoàn tiền - primary

}
