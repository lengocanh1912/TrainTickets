-- 1. Tạo CSDL
CREATE DATABASE trainticketsdb;
USE trainticketsdb;

-- 2. Bảng người dùng
CREATE TABLE user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(25)  NOT NULL UNIQUE,
    password      VARCHAR(100) NOT NULL,
    fullname      VARCHAR(50)  NOT NULL,
    email         VARCHAR(50)  NOT NULL UNIQUE,
    phoneNumber   VARCHAR(15)  NOT NULL UNIQUE,
    gender        BIT          NOT NULL,
    address       VARCHAR(200) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    status        INT DEFAULT 1,
    createdAt     DATETIME NOT NULL,
    updatedAt     DATETIME
);

-- 3. Bảng nhà ga
CREATE TABLE station (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE,
    location  VARCHAR(200) NOT NULL,
    createdAt DATETIME NOT NULL,
    updatedAt DATETIME
);

-- 4. Bảng tàu
CREATE TABLE train (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(50)  NOT NULL,
    code      VARCHAR(20)  NOT NULL UNIQUE,
    capacity  SMALLINT     NOT NULL,
    createdAt DATETIME     NOT NULL,
    updatedAt DATETIME
);

-- 5. Bảng toa tàu
CREATE TABLE coach (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    trainId   BIGINT NOT NULL,
    code      VARCHAR(10) NOT NULL,
    type      VARCHAR(20) NOT NULL,
    capacity  SMALLINT NOT NULL,
    FOREIGN KEY (trainId) REFERENCES train(id),
    UNIQUE (trainId, code)
);

-- 6. Bảng ghế trong toa
CREATE TABLE seat (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    coachId   BIGINT NOT NULL,
    seatCode  VARCHAR(10) NOT NULL,
    FOREIGN KEY (coachId) REFERENCES coach(id),
    UNIQUE (coachId, seatCode)
);

-- 7. Bảng tuyến đường
CREATE TABLE route (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    departureId BIGINT NOT NULL,
    arrivalId   BIGINT NOT NULL,
    duration    TIME NOT NULL,
    distanceKm  FLOAT NOT NULL,
    createdAt   DATETIME NOT NULL,
    updatedAt   DATETIME,
    FOREIGN KEY (departureId) REFERENCES station(id),
    FOREIGN KEY (arrivalId) REFERENCES station(id),
    UNIQUE (departureId, arrivalId)
);

-- 8. Bảng chuyến tàu
CREATE TABLE trip (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    trainId     BIGINT NOT NULL,
    routeId     BIGINT NOT NULL,
    departureAt DATETIME NOT NULL,
    arrivalAt   DATETIME NOT NULL,
    price       FLOAT NOT NULL,
    status      TINYINT NOT NULL,
    createdAt   DATETIME NOT NULL,
    updatedAt   DATETIME,
    FOREIGN KEY (trainId) REFERENCES train(id),
    FOREIGN KEY (routeId) REFERENCES route(id)
);

-- 9. Bảng vé
CREATE TABLE ticket (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId    BIGINT NOT NULL,
    tripId    BIGINT NOT NULL,
    seatId    BIGINT NOT NULL,
    price     FLOAT NOT NULL,
    status    TINYINT NOT NULL,
    createdAt DATETIME NOT NULL,
    updatedAt DATETIME,
    FOREIGN KEY (userId) REFERENCES user(id),
    FOREIGN KEY (tripId) REFERENCES trip(id),
    FOREIGN KEY (seatId) REFERENCES seat(id),
    UNIQUE (tripId, seatId)
);

-- 10. Bảng mã giảm giá
CREATE TABLE discount (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(30) NOT NULL UNIQUE,
    description  VARCHAR(100),
    discountType VARCHAR(20) NOT NULL, 
    value        FLOAT NOT NULL,
    maxDiscount     FLOAT DEFAULT NULL, 
    expiredAt    DATETIME NOT NULL,
    createdAt    DATETIME NOT NULL
);

-- 11. Bảng đơn hàng
CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId       BIGINT NOT NULL,
    discountId   BIGINT,
    totalAmount  FLOAT NOT NULL,
    finalAmount  FLOAT NOT NULL,
    status       TINYINT NOT NULL,
    createdAt    DATETIME NOT NULL,
    updatedAt    DATETIME,
    FOREIGN KEY (userId) REFERENCES user(id),
    FOREIGN KEY (discountId) REFERENCES discount(id)
);

-- 12. Chi tiết đơn hàng
CREATE TABLE order_ticket (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    orderId   BIGINT NOT NULL,
    ticketId  BIGINT NOT NULL,
    FOREIGN KEY (orderId) REFERENCES orders(id),
    FOREIGN KEY (ticketId) REFERENCES ticket(id),
    UNIQUE (orderId, ticketId)
);

-- 13. Thanh toán
CREATE TABLE payment (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    orderId    BIGINT NOT NULL,
    method     VARCHAR(30) NOT NULL,
    status     VARCHAR(20) NOT NULL,
    amount     FLOAT NOT NULL,
    paidAt     DATETIME,
    FOREIGN KEY (orderId) REFERENCES orders(id),
    UNIQUE (orderId)
);

-- insert
-- ========================
-- Bảng user (cập nhật)
-- ========================
INSERT INTO user (username, password, fullname, email, phoneNumber, gender, address, role, status, createdAt)
VALUES
('hoangnguyen', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyễn Văn Hoàng', 'hoangnguyen@gmail.com', '0909123456', 1, '12 Lê Lợi, Q1, HCM', 'CUSTOMER', 1, NOW()),
('linhpham', 'e10adc3949ba59abbe56e057f20f883e', 'Phạm Thị Linh', 'linhpham@gmail.com', '0909112233', 0, '45 Hai Bà Trưng, Q3, HCM', 'CUSTOMER', 1, NOW()),
('trungtran', 'e10adc3949ba59abbe56e057f20f883e', 'Trần Minh Trung', 'trungtran@gmail.com', '0909888777', 1, '98 Trần Hưng Đạo, Hà Nội', 'CUSTOMER', 1, NOW()),
('hoaquach', 'e10adc3949ba59abbe56e057f20f883e', 'Quách Bảo Hoa', 'hoaquach@gmail.com', '0909554433', 0, '76 Nguyễn Huệ, Đà Nẵng', 'CUSTOMER', 1, NOW()),
('hieule', 'e10adc3949ba59abbe56e057f20f883e', 'Lê Quốc Hiếu', 'hieule@gmail.com', '0909222111', 1, '55 Phan Đình Phùng, Huế', 'CUSTOMER', 1, NOW()),
('adminngoc', '0192023a7bbd73250516f069df18b500', 'Ngọc Hà', 'adminngoc@train.vn', '0909444555', 0, 'Hệ thống văn phòng', 'ADMIN', 1, NOW()),
('manhtuan', 'e10adc3949ba59abbe56e057f20f883e', 'Phan Mạnh Tuấn', 'manhtuan@gmail.com', '0909333222', 1, '200 Lý Thường Kiệt, Q10, HCM', 'CUSTOMER', 1, NOW()),
('thuhoai', 'e10adc3949ba59abbe56e057f20f883e', 'Lê Thu Hoài', 'thuhoai@gmail.com', '0909111000', 0, '30 Nguyễn Trãi, HN', 'CUSTOMER', 1, NOW()),
('admintrung', '0192023a7bbd73250516f069df18b500', 'Trung Kiên', 'admintrung@train.vn', '0909777666', 1, 'Phòng Kỹ thuật, Ga HCM', 'ADMIN', 1, NOW()),
('thanhha', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyễn Thanh Hà', 'thanhha@gmail.com', '0909888999', 0, 'Quảng Trị', 'CUSTOMER', 1, NOW());

-- "123456"  e10adc3949ba59abbe56e057f20f883e
-- "admin123"  0192023a7bbd73250516f069df18b500
-- ========================
-- Bảng station
-- ========================
INSERT INTO station (name, location, createdAt)
VALUES
('Ga Sài Gòn', '01 Nguyễn Thông, Q3, HCM', NOW()),
('Ga Hà Nội', '120 Lê Duẩn, Hoàn Kiếm, Hà Nội', NOW()),
('Ga Đà Nẵng', '791 Hải Phòng, Thanh Khê, Đà Nẵng', NOW()),
('Ga Huế', '2 Bùi Thị Xuân, TP Huế', NOW()),
('Ga Biên Hòa', 'Số 1, Đường Hưng Đạo Vương, Biên Hòa', NOW()),
('Ga Nha Trang', '17 Thái Nguyên, TP Nha Trang', NOW()),
('Ga Vinh', '1 Lê Lợi, TP Vinh', NOW()),
('Ga Phan Thiết', 'Đường Trần Hưng Đạo, Phan Thiết', NOW()),
('Ga Quảng Ngãi', 'Tổ 3, P. Nghĩa Chánh, Quảng Ngãi', NOW()),
('Ga Thanh Hóa', '56 Đường Nguyễn Trãi, Thanh Hóa', NOW());

-- ========================
-- Bảng train
-- ========================
INSERT INTO train (name, code, capacity, createdAt)
VALUES
('Tàu SE1', 'SE1', 500, NOW()),
('Tàu SE2', 'SE2', 480, NOW()),
('Tàu TN1', 'TN1', 450, NOW()),
('Tàu TN2', 'TN2', 420, NOW()),
('Tàu SNT1', 'SNT1', 400, NOW()),
('Tàu SPT1', 'SPT1', 350, NOW()),
('Tàu SE3', 'SE3', 500, NOW()),
('Tàu SE4', 'SE4', 500, NOW()),
('Tàu NA1', 'NA1', 460, NOW()),
('Tàu QB1', 'QB1', 430, NOW());

-- ========================
-- Bảng coach
-- ========================
INSERT INTO coach (trainId, code, type, capacity)
VALUES
(1, 'C1', 'Ghế mềm', 50),
(1, 'C2', 'Ghế cứng', 60),
(1, 'C3', 'Giường nằm', 40),
(2, 'C1', 'Ghế mềm', 50),
(2, 'C2', 'Ghế cứng', 60),
(3, 'C1', 'Giường nằm', 40),
(3, 'C2', 'Ghế cứng', 60),
(4, 'C1', 'Ghế mềm', 50),
(5, 'C1', 'Giường nằm', 40),
(5, 'C2', 'Ghế mềm', 50);

-- ========================
-- Bảng seat
-- ========================
INSERT INTO seat (coachId, seatCode)
VALUES
(1, 'A01'), (1, 'A02'), (1, 'A03'), (1, 'A04'), (1, 'A05'),
(2, 'B01'), (2, 'B02'), (2, 'B03'), (2, 'B04'), (2, 'B05');

-- ========================
-- Bảng route
-- ========================
INSERT INTO route (departureId, arrivalId, duration, distanceKm, createdAt)
VALUES
(1, 2, '30:00:00', 1726.0, NOW()),
(1, 3, '18:00:00', 935.0, NOW()),
(2, 4, '13:00:00', 700.0, NOW()),
(3, 4, '3:30:00', 103.0, NOW()),
(2, 1, '30:00:00', 1726.0, NOW()),
(5, 1, '2:00:00', 70.0, NOW()),
(6, 2, '22:00:00', 1300.0, NOW()),
(1, 10, '25:00:00', 1500.0, NOW()),
(4, 2, '13:00:00', 700.0, NOW()),
(1, 7, '22:00:00', 1260.0, NOW());

-- ========================
-- Bảng trip
-- ========================
INSERT INTO trip (trainId, routeId, departureAt, arrivalAt, price, status, createdAt)
VALUES
(1, 1, '2025-05-01 08:00:00', '2025-05-02 14:00:00', 1200000, 1, NOW()),
(1, 2, '2025-05-01 10:00:00', '2025-05-01 22:00:00', 850000, 1, NOW()),
(2, 3, '2025-05-02 07:00:00', '2025-05-02 20:00:00', 900000, 1, NOW()),
(3, 4, '2025-05-03 13:00:00', '2025-05-03 16:30:00', 300000, 1, NOW()),
(4, 5, '2025-05-04 09:00:00', '2025-05-05 15:00:00', 1150000, 1, NOW()),
(5, 6, '2025-05-05 06:00:00', '2025-05-05 08:00:00', 150000, 1, NOW()),
(6, 7, '2025-05-06 11:00:00', '2025-05-07 09:00:00', 1100000, 1, NOW()),
(7, 8, '2025-05-07 14:00:00', '2025-05-08 15:00:00', 1000000, 1, NOW()),
(8, 9, '2025-05-08 12:00:00', '2025-05-09 01:00:00', 950000, 1, NOW()),
(9, 10, '2025-05-09 08:00:00', '2025-05-10 06:00:00', 980000, 1, NOW());

-- ========================
-- Bảng discount
-- ========================
INSERT INTO discount (code, description, discountType, value, maxDiscount, expiredAt, createdAt)
VALUES
('SPRING2025', 'Ưu đãi mùa xuân', 'percent', 10, 100000, '2025-06-01', NOW()),
('WELCOME', 'Chào mừng người dùng mới', 'fixed', 50000, NULL, '2025-12-31', NOW()),
('VIPCUSTOMER', 'Khách hàng thân thiết', 'percent', 15, 150000, '2025-07-01', NOW()),
('SUMMER25', 'Giảm mùa hè', 'fixed', 75000, NULL, '2025-08-31', NOW()),
('HOLIDAY2025', 'Ưu đãi lễ 30/4', 'percent', 20, 200000, '2025-05-01', NOW()),
('FAMILYTRIP', 'Ưu đãi cho nhóm gia đình', 'fixed', 100000, NULL, '2025-09-01', NOW()),
('RETURN2025', 'Giảm khi đặt vé khứ hồi', 'percent', 5, 50000, '2025-12-31', NOW()),
('LOYALTY25', 'Thưởng khách hàng lâu năm', 'percent', 12, 120000, '2025-07-01', NOW()),
('SEATSALE', 'Giảm giá ghế mềm', 'fixed', 30000, NULL, '2025-10-01', NOW()),
('TRAVELNOW', 'Đặt sớm - giá tốt', 'percent', 8, 80000, '2025-05-31', NOW());

-- ========================
-- Bảng ticket
-- ========================
INSERT INTO ticket (userId, tripId, seatId, price, status, createdAt)
VALUES
(1, 1, 1, 350000, 1, NOW()),
(2, 1, 2, 350000, 1, NOW()),
(3, 2, 3, 400000, 1, NOW()),
(4, 2, 4, 400000, 1, NOW()),
(5, 3, 5, 280000, 1, NOW()),
(6, 3, 6, 280000, 1, NOW()),
(7, 4, 7, 500000, 1, NOW()),
(8, 4, 8, 500000, 1, NOW()),
(9, 5, 9, 320000, 1, NOW()),
(10, 5, 10, 320000, 1, NOW());

-- ========================
-- Bảng orders
-- ========================
INSERT INTO orders (userId, discountId, totalAmount, finalAmount, status, createdAt)
VALUES
(1, NULL, 350000, 350000, 1, NOW()),
(2, NULL, 350000, 350000, 1, NOW()),
(3, 1, 400000, 360000, 1, NOW()),
(4, NULL, 400000, 400000, 1, NOW()),
(5, NULL, 280000, 280000, 1, NOW()),
(6, 2, 280000, 250000, 1, NOW()),
(7, NULL, 500000, 500000, 1, NOW()),
(8, NULL, 500000, 500000, 1, NOW()),
(9, NULL, 320000, 320000, 1, NOW()),
(10, NULL, 320000, 320000, 1, NOW());

-- ========================
-- Bảng order_ticket
-- ========================
INSERT INTO order_ticket (orderId, ticketId)
VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(9, 9),
(10, 10);

-- ========================
-- Bảng payment
-- ========================
INSERT INTO payment (orderId, method, status, amount, paidAt)
VALUES
(1, 'Credit Card', 'PAID', 350000, NOW()),
(2, 'ZaloPay', 'PAID', 350000, NOW()),
(3, 'Credit Card', 'PAID', 360000, NOW()),
(4, 'MoMo', 'PAID', 400000, NOW()),
(5, 'Cash', 'PAID', 280000, NOW()),
(6, 'ZaloPay', 'PAID', 250000, NOW()),
(7, 'Credit Card', 'PAID', 500000, NOW()),
(8, 'Cash', 'PAID', 500000, NOW()),
(9, 'MoMo', 'PAID', 320000, NOW()),
(10, 'ZaloPay', 'PAID', 320000, NOW());
