// Lưu danh sách ghế đã chọn cho từng loại vé
let selectedSeats = {
    adult: [],
    child: [],
    student: [],
    senior: []
};

function openModalFromAttr(button) {
    const tripId = button.dataset.tripId;
    const safeParse = (value) => isNaN(parseInt(value)) ? 0 : parseInt(value);
    const adult = safeParse(button.dataset.adult);
    const child = safeParse(button.dataset.child);
    const student = safeParse(button.dataset.student);
    const senior = safeParse(button.dataset.senior);

    const trip = {
        tripId
    };

    console.log("id chuyến đi là:", trip);
    fetch(`/trainticket/api/trips/${tripId}/detail`)
        .then(res => res.json())
        .then(data => {
            document.getElementById("modalDepartureStation").innerText = data.departureStation;
            document.getElementById("modalArrivalStation").innerText = data.arrivalStation;
            document.getElementById("modalDepartureDate").innerText = data.departureDate;
            document.getElementById("modalTrainName").innerText = data.trainName;

            selectedSeats = {adult: [], child: [], student: [], senior: []};

            renderTicketTypes({adult, child, student, senior});
            renderCoachCards(data.coaches);

            document.getElementById("seatGrid").innerHTML = "";
            document.getElementById("myModal").style.display = "block";
        })
        .catch(err => alert("Không tải được dữ liệu chuyến đi: " + err));
}

function renderTicketTypes(counts) {
    const container = document.getElementById("ticketList");
    container.innerHTML = "";
    const types = [
        {key: "adult", label: "Người lớn", discount: ""},
        {key: "child", label: "Trẻ em", discount: "-25%"},
        {key: "student", label: "Sinh viên", discount: "-10%"},
        {key: "senior", label: "Người cao tuổi", discount: "-15%"}
    ];


    let firstCardSelected = false;
    types.forEach(type => {
        for (let i = 0; i < counts[type.key]; i++) {
            const div = document.createElement("div");
            div.className = "ticket-card";
            div.dataset.type = type.key;
            div.dataset.index = i;
            div.innerHTML = `<strong>${type.label} ${i + 1}${type.discount ? ' ' + type.discount : ''}</strong><br><span>Chưa chọn</span>`;

            if (!firstCardSelected) {
                div.classList.add("active");
                firstCardSelected = true;
            }

            div.addEventListener("click", () => {
                document.querySelectorAll(".ticket-card").forEach(c => c.classList.remove("active"));
                div.classList.add("active");
            });

            container.appendChild(div);
        }
    });

}

function renderCoachCards(coaches) {
    const container = document.getElementById("coachCards");
    container.innerHTML = "";

    coaches.sort((a, b) => a.position - b.position).forEach((coach, index) => {
        const div = document.createElement("div");
        div.className = "coach-card";
        div.innerText = `Toa ${coach.position} - ${coach.code}`;

        div.onclick = () => {
            document.querySelectorAll(".coach-card").forEach(c => c.classList.remove("active"));
            div.classList.add("active");
            document.getElementById("selectedCoachName").innerText = `Toa ${coach.position} - ${coach.code}`;
            // Gắn thông tin toa cho từng ghế
            coach.seats.forEach(seat => {
                seat.coachCode = coach.code;
                seat.coachPosition = coach.position;
            });
            renderSeats(coach.seats);
        };

        container.appendChild(div);

        if (index === 0) setTimeout(() => div.click(), 0);
    });

    const trainHead = document.createElement("img");
    trainHead.src = "/trainticket/img/head-train.png";
    trainHead.className = "train-head";
    container.appendChild(trainHead);
}

function renderSeats(seats) {
    console.log("Seats data:", seats);
    const grid = document.getElementById("seatGrid");
    grid.innerHTML = "";
    const rows = 4;
    const rowBoxes = Array.from({length: rows}, () => {
        const div = document.createElement("div");
        div.className = "row-box";
        return div;
    });

    seats.forEach((seat, i) => {
        const row = i % rows;
        const div = document.createElement("div");
        div.className = `seat ${seat.booked ? "sold" : "empty"}`;
        div.innerText = seat.seatCode;
        div.dataset.code = seat.seatCode;
        div.dataset.price = seat.price;
        div.dataset.seatId = seat.id; // ✅ thêm dòng này

        if (!seat.booked) div.onclick = () => selectSeat(div, seat);

        rowBoxes[row].appendChild(div);
    });

    rowBoxes.forEach(box => grid.appendChild(box));
}

function selectSeat(div, seat) {
    let activeCard = document.querySelector(".ticket-card.active");
    if (!activeCard) {
        const firstCard = document.querySelector(".ticket-card");
        if (firstCard) {
            firstCard.classList.add("active");
            activeCard = firstCard;
        } else return;
    }

    const type = activeCard.dataset.type;
    const index = parseInt(activeCard.dataset.index);
    const currentSeat = selectedSeats[type][index];

    if (currentSeat?.code === seat.seatCode) {
        div.classList.remove("selected");
        selectedSeats[type][index] = null;
        activeCard.querySelector("span").innerText = "Chưa chọn";
        updateSeatInfo();
        return;
    }

    // Kiểm tra ghế đã được chọn bởi vé khác
    for (const group in selectedSeats) {
        if (selectedSeats[group]) {
            for (const s of selectedSeats[group]) {
                if (s?.code === seat.seatCode) {
                    alert("Ghế đã được chọn cho vé khác!");
                    return;
                }
            }
        }
    }

    if (currentSeat) {
        const oldSeat = document.querySelector(`.seat[data-code="${currentSeat.code}"]`);
        if (oldSeat) oldSeat.classList.remove("selected");
    }

    let finalPrice = calculateDiscountedPrice(parseFloat(seat.price), type);
    if (finalPrice >= 1000) {
        finalPrice = Math.round(finalPrice / 1000) * 1000;
    } else {
        finalPrice = Math.round(finalPrice); // hoặc giữ nguyên nếu muốn hiển thị số lẻ
    }

    div.classList.add("selected");
    selectedSeats[type][index] = {
        code: seat.seatCode,
        price: finalPrice,
        id: seat.id // ✅ lưu id để gửi lên backend

    };
    activeCard.querySelector("span").innerText = `chỗ ${seat.seatCode}, toa ${seat.coachPosition} - ${finalPrice.toLocaleString()}đ`;

    updateSeatInfo();

    const allCards = Array.from(document.querySelectorAll(".ticket-card"));
    const currentIndex = allCards.indexOf(activeCard);
    const nextCard = allCards[currentIndex + 1];
    if (nextCard) {
        setTimeout(() => {
            activeCard.classList.remove("active");
            nextCard.classList.add("active");
        }, 100);
    }
}

function calculateDiscountedPrice(originalPrice, type) {
    if (type === "child") return originalPrice * 0.75;
    if (type === "student") return originalPrice * 0.9;
    if (type === "senior") return originalPrice * 0.85;
    return originalPrice;
}

function updateSeatInfo() {
    const countDiv = document.getElementById("selectedCount");
    const totalDiv = document.getElementById("totalPrice");

    const totalTickets = parseInt(countDiv.dataset.total) || 0;

    let totalSeats = 0;
    let totalPrice = 0;

    Object.values(selectedSeats).forEach(list => {
        list.forEach(seat => {
            if (seat) {
                totalSeats++;
                totalPrice += seat.price;
            }
        });
    });

    countDiv.innerText = `Đã chọn: ${totalSeats} / ${totalTickets} chỗ`;
    totalDiv.innerText = `Tổng tiền: ${totalPrice.toLocaleString()}đ`;
}

function closeModal() {
    document.getElementById("myModal").style.display = "none";
    selectedSeats = {adult: [], child: [], student: [], senior: []};
    updateSeatInfo();
}

function selectDeparture(button) {
    const tripId = button.getAttribute("data-trip-id");
    const seatIds = [];
    const prices = [];
    const ticketTypes = [];

    for (const [type, seats] of Object.entries(selectedSeats)) {
        seats.forEach(seat => {
            // if (seat) {

                if (!seat || !seat.id) {
                    console.warn(`⚠️ Bỏ qua ghế không hợp lệ hoặc thiếu ID:`, seat);
                    return;
                }

                seatIds.push(seat.id); // 👈 cần thêm .id khi set trong selectSeat
                prices.push(seat.price);

                if (type === "adult") ticketTypes.push(0);
                else if (type === "child") ticketTypes.push(1);
                else if (type === "student") ticketTypes.push(2);
                else if (type === "senior") ticketTypes.push(3);
            // }
        });
    }

    const bookingData = {
        tripId,
        seatIds,
        prices,
        ticketTypes
    };

    console.log("Booking data gửi đi:", bookingData);

    fetch('/trainticket/api/trips/booking', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookingData)
    })
        .then(res => {
            if (res.redirected) {
                window.location.href = res.url;
            } else {
                return res.json();
            }
        })
        .catch(error => {
            alert("Có lỗi xảy ra khi đặt vé.");
            console.error("Booking error:", error);
        });
}


