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

    console.log("🚀 Opening modal for trip:", trip);
    console.log("🎫 Ticket counts:", {adult, child, student, senior});

    fetch(`/trainticket/api/trips/${tripId}/detail`)
        .then(res => res.json())
        .then(data => {
            document.getElementById("modalDepartureStation").innerText = data.departureStation;
            document.getElementById("modalArrivalStation").innerText = data.arrivalStation;
            document.getElementById("modalDepartureDate").innerText = data.departureDate;
            document.getElementById("modalTrainName").innerText = data.trainName;

            // ✅ Đảm bảo reset selectedSeats đúng cách
            selectedSeats = {
                adult: new Array(adult).fill(null),
                child: new Array(child).fill(null),
                student: new Array(student).fill(null),
                senior: new Array(senior).fill(null)
            };

            console.log("🔄 Initialized selectedSeats:", selectedSeats);

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

    // ❌ KHÔNG reset selectedSeats ở đây nữa vì đã reset trong openModalFromAttr
    // selectedSeats đã được khởi tạo trong openModalFromAttr

    console.log("🎫 renderTicketTypes - Current selectedSeats:", selectedSeats);

    let firstCardSelected = false;
    let totalTickets = 0;

    types.forEach(type => {
        for (let i = 0; i < counts[type.key]; i++) {
            totalTickets++;

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

    // Update total count
    const countDiv = document.getElementById("selectedCount");
    if (countDiv) {
        countDiv.dataset.total = totalTickets;
        countDiv.innerHTML = `Đã chọn: 0/${totalTickets} chỗ`;
    }
}
function renderCoachCards(coaches) {
    const container = document.getElementById("coachCards");
    container.innerHTML = "";

    coaches
        .sort((a, b) => a.position - b.position)
        .forEach((coach, index) => {

            const div = document.createElement("div");
            div.className = "coach-card";
            div.innerHTML = `
                <div>Toa ${coach.position} - ${coach.code}</div>
                ${coach.state !== 'ACTIVE'
                    ? `<small class="text-muted">⛔ Tạm dừng hoạt động</small>`
                    : ''
                }
            `;

            // ❌ COACH INACTIVE
            if (coach.state !== 'ACTIVE') {
                div.classList.add("inactive");
                div.onclick = () => {
                    alert("Toa này đang tạm dừng hoạt động");
                };
            }
            // ✅ COACH ACTIVE
            else {
                div.onclick = () => {
                    document.querySelectorAll(".coach-card")
                        .forEach(c => c.classList.remove("active"));
                    div.classList.add("active");

                    document.getElementById("selectedCoachName").innerText =
                        `Toa ${coach.position} - ${coach.code}`;

                    // gắn info toa cho ghế
                    coach.seats.forEach(seat => {
                        seat.coachCode = coach.code;
                        seat.coachPosition = coach.position;
                    });

                    renderSeats(coach);
                };

                // auto click toa đầu tiên ACTIVE
                if (index === 0) setTimeout(() => div.click(), 0);
            }

            container.appendChild(div);
        });
}
function renderSeats(coach) {
    const grid = document.getElementById("seatGrid");
    grid.innerHTML = "";

    const validSeats = coach.seats
        .filter(seat => seat.state !== 'INACTIVE')
        .slice(0, coach.capacity);

    validSeats.forEach(seat => {
        const div = document.createElement("div");
        div.className = `seat ${seat.booked ? "sold" : "empty"}`;
        div.innerText = seat.seatCode;
        div.dataset.code = seat.seatCode;
        div.dataset.price = seat.price;
        div.dataset.seatId = seat.id;

        if (seat.state !== 'sold') {
            div.onclick = () => selectSeat(div, seat);
        }

        grid.appendChild(div);
    });
}

function selectSeat(div, seat) {
    console.log("🖱️ selectSeat called with:", {div, seat});

    let activeCard = document.querySelector(".ticket-card.active");
    if (!activeCard) {
        const firstCard = document.querySelector(".ticket-card");
        if (firstCard) {
            firstCard.classList.add("active");
            activeCard = firstCard;
        } else {
            console.error("❌ Không tìm thấy ticket card nào!");
            return;
        }
    }

    const type = activeCard.dataset.type;
    const index = parseInt(activeCard.dataset.index);

    console.log(`🎫 Active ticket: ${type}[${index}]`);

    if (!selectedSeats[type]) {
        console.warn(`⚠️ ${type} array not initialized, creating...`);
        selectedSeats[type] = [];
    }

    const currentSeat = selectedSeats[type][index];
    console.log(`🪑 Current seat at ${type}[${index}]:`, currentSeat);

    // Nếu click vào ghế đã chọn -> bỏ chọn
    if (currentSeat && currentSeat.code === seat.seatCode) {
        console.log("🔄 Deselecting seat");
        div.classList.remove("selected");
        selectedSeats[type][index] = null;
        activeCard.querySelector("span").innerText = "Chưa chọn";
        updateSeatInfo();
        return;
    }

    // Kiểm tra ghế đã được chọn bởi vé khác
    for (const group in selectedSeats) {
        if (selectedSeats[group] && Array.isArray(selectedSeats[group])) {
            for (const s of selectedSeats[group]) {
                if (s && s.code === seat.seatCode) {
                    alert("Ghế đã được chọn cho vé khác!");
                    return;
                }
            }
        }
    }

    // Bỏ chọn ghế cũ nếu có
    if (currentSeat && currentSeat.code) {
        const oldSeat = document.querySelector(`.seat[data-code="${currentSeat.code}"]`);
        if (oldSeat) oldSeat.classList.remove("selected");
    }

    // Tính giá sau discount
    let finalPrice = calculateDiscountedPrice(parseFloat(seat.price), type);
    if (finalPrice >= 1000) {
        finalPrice = Math.round(finalPrice / 1000) * 1000;
    } else {
        finalPrice = Math.round(finalPrice);
    }

    // ✅ FIX: Sử dụng seatCode làm ID tạm thời nếu không có id
    let seatId = seat.id;
    if (!seatId) {
        console.warn("⚠️ seat.id missing, using seatCode as temporary ID");
        seatId = seat.seatCode; // Hoặc có thể dùng index khác

        // 🔍 Kiểm tra xem có trường nào khác có thể là ID không
        console.log("🔍 Available seat properties:", Object.keys(seat));

        // Thử các trường có thể là ID
        if (seat.seatId) seatId = seat.seatId;
        else if (seat.ID) seatId = seat.ID;
        else if (seat.seat_id) seatId = seat.seat_id;
    }

    // Chọn ghế mới
    div.classList.add("selected");
    const newSeatData = {
        code: seat.seatCode,
        price: finalPrice,
        id: seatId, // ✅ Sử dụng ID đã fix
        coachCode: seat.coachCode || '',
        coachPosition: seat.coachPosition || ''
    };

    selectedSeats[type][index] = newSeatData;

    console.log(`✅ Seat selected for ${type}[${index}]:`, newSeatData);
    console.log(`🔍 Full selectedSeats after selection:`, selectedSeats);

    activeCard.querySelector("span").innerText = `chỗ ${seat.seatCode}, toa ${seat.coachPosition} - ${finalPrice.toLocaleString()}đ`;

    updateSeatInfo();

    // Chuyển sang card tiếp theo
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
    const tripId = parseInt(button.getAttribute("data-trip-id"));

    // ... existing code ...
    // DEBUG: In ra toàn bộ selectedSeats
    console.log("🔍 FULL selectedSeats object:", selectedSeats);
    console.log("🔍 selectedSeats type:", typeof selectedSeats);
    console.log("🔍 selectedSeats keys:", Object.keys(selectedSeats));

    // DEBUG: Kiểm tra từng loại vé
    Object.keys(selectedSeats).forEach(type => {
        console.log(`🔍 ${type}:`, selectedSeats[type]);
        console.log(`🔍 ${type} is array:`, Array.isArray(selectedSeats[type]));
        console.log(`🔍 ${type} length:`, selectedSeats[type] ? selectedSeats[type].length : 'null/undefined');
    });

    const tickets = [];

    const typeMapping = {
        adult: 0,
        child: 1,
        student: 2,
        senior: 3
    };

    for (const [type, seats] of Object.entries(selectedSeats)) {

        if (!Array.isArray(seats)) continue;

        seats.forEach(seat => {
            if (seat && seat.id) {
                tickets.push({
                    seatId: seat.id,
                    ticketType: typeMapping[type]
                });
            }
        });
    }

    if (tickets.length === 0) {
        alert("Vui lòng chọn ít nhất một ghế!");
        return;
    }

    console.log("\n📊 Final arrays:");

    // ✅ Validation chi tiết
    console.log("🔍 Validation checks:");
    console.log("- tripId type:", typeof tripId, "value:", tripId);

    const bookingData = {
        tripId,
        tickets
    };

    console.log("📤 Final booking data:", bookingData);

    // ... rest of fetch code ...
    fetch(`/trainticket/api/trips/${tripId}/bookings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(bookingData)
    })
        .then(res => {
            console.log("🔍 Response status:", res.status);

            if (res.status === 401) { // chưa login
                alert("⚠️ Vui lòng đăng nhập trước khi đặt vé!");
                const currentUrl = window.location.href;
                localStorage.setItem('pendingBooking', JSON.stringify({
                    url: currentUrl,
                    seats: selectedSeats
                }));
                // redirect kèm param redirect
                window.location.href = '/trainticket/login?redirect=' + encodeURIComponent(currentUrl);
                return;
            }

            if (res.status === 409) {
                return res.json().then(error => {
                    alert(error.message);
                    location.reload();
                    return null;
                });
            }

            if (!res.ok) {
                alert("Có lỗi xảy ra, vui lòng thử lại!");
                return null;
            }

            return res.json();
        })
        .then(data => {
            if (data) {
                console.log("✅ Booking thành công", data);

                if (data.orderId) {
                    window.location.href = "/trainticket/user/trip/passengerInfo/" + data.orderId;
                } else {
                    alert("Đặt vé thành công, nhưng không có đường dẫn tiếp theo!");
                }
            }
        })
        .catch(err => console.error("❌ Lỗi kết nối:", err));



}