package t3h.edu.vn.traintickets.enums;

public enum MessageStatus {
    SENDING,   // client gửi
    SENT,      // server lưu OK
    READ,      // bên kia đã đọc
    FAILED     // lỗi
}
