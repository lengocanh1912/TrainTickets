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
