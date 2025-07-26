let selectedSeats = [];

function openModalFromAttr(button) {
    const tripId = button.dataset.tripId;
    openBookingModal(tripId);
}

function openBookingModal(tripId) {
    fetch(`/trainticket/api/trips/${tripId}/detail`)
        .then(response => response.json())
        .then(data => {
            document.getElementById("modalDepartureStation").innerText = data.departureStation;
            document.getElementById("modalArrivalStation").innerText = data.arrivalStation;
            document.getElementById("modalDepartureDate").innerText = data.departureDate;
            document.getElementById("modalTrainName").innerText = data.trainName;
            selectedSeats = []; // reset ghế

            renderCoachCards(data.coaches);
            document.getElementById("seatGrid").innerHTML = "";
            updateSeatInfo(); // reset hiển thị

            document.getElementById("myModal").style.display = "block";
        })
        .catch(err => {
            alert("Lỗi khi tải dữ liệu chuyến đi: " + err);
        });
}

const container = document.querySelector(".ticket-type-container");

const ticketTypes = [
    {label: "Người lớn 1", discount: null},
    {label: "Người lớn 2", discount: null},
    {label: "Trẻ em", discount: "-25%"},
    {label: "Sinh viên", discount: "-10%"},
    {label: "Người cao tuổi", discount: "-15%"},
    // thêm tùy ý
];

container.innerHTML = "";

ticketTypes.forEach(type => {
    const div = document.createElement("div");
    div.className = "ticket-type-item";

    div.innerHTML = `${type.label} ${type.discount ? `<span class="discount">${type.discount}</span>` : ""}<br><span>Chưa chọn</span>`;

    container.appendChild(div);
});

function renderCoachCards(coaches) {
    const container = document.getElementById("coachCards");
    container.innerHTML = "";

    const sortedCoaches = [...coaches].sort((a, b) => a.position - b.position); // hoặc b.position - a.position

    sortedCoaches.forEach(coach => {
        const div = document.createElement("div");
        div.className = "coach-card";
        div.innerText = `Toa ${coach.position} - ${coach.code}`;

        div.onclick = () => {
            document.querySelectorAll(".coach-card").forEach(c => c.classList.remove("active"));
            div.classList.add("active");

            document.getElementById("selectedCoachName").innerText = `Toa ${coach.position} - ${coach.code}`;
            renderSeats(coach.seats);
        };

        container.appendChild(div);
    });

    const trainHead = document.createElement("img");
    trainHead.src = "/trainticket/img/head-train.png";
    trainHead.className = "train-head";
    container.appendChild(trainHead);
}

function renderSeats(seats) {
    const grid = document.getElementById("seatGrid");
    grid.innerHTML = "";

    const totalSeats = seats.length;
    const rows = 4;
    const cols = Math.ceil(totalSeats / rows);

    // Tạo 4 hàng trống
    const rowBoxes = [];
    for (let i = 0; i < rows; i++) {
        const rowBox = document.createElement("div");
        rowBox.className = "row-box";
        rowBoxes.push(rowBox);
    }

    // Gán ghế theo cột
    seats.forEach((seat, index) => {
        const col = Math.floor(index / rows); // số cột hiện tại
        const row = index % rows; // số hàng (0 -> 3)

        const div = document.createElement("div");
        div.className = "seat " + (seat.booked ? "sold" : "empty");
        div.innerText = seat.seatCode;
        div.dataset.code = seat.seatCode;
        div.dataset.price = seat.price;

        if (!seat.booked) {
            div.onclick = () => selectSeat(div, seat);
        }

        rowBoxes[row].appendChild(div);
    });

    // Thêm 4 hàng vào grid
    rowBoxes.forEach(rowBox => grid.appendChild(rowBox));
}


function selectSeat(div, seat) {
    if (div.classList.contains("selected")) {
        div.classList.remove("selected");
        selectedSeats = selectedSeats.filter(s => s.code !== seat.seatCode);
    } else {
        if (selectedSeats.length >= 1) {
            alert("Bạn chỉ được chọn 1 ghế mỗi chiều.");
            return;
        }
        div.classList.add("selected");
        selectedSeats.push({code: seat.seatCode, price: seat.price});
    }

    updateSeatInfo();
}

function updateSeatInfo() {
    const seatInfo = document.getElementById("selectedSeatInfo");
    const priceDiv = document.getElementById("selectedPrice");
    const countDiv = document.getElementById("selectedCount");

    if (selectedSeats.length === 0) {
        seatInfo.innerText = "Người lớn: Chưa chọn";
        priceDiv.style.display = "none";
        countDiv.innerText = "Đã chọn: 0/1 chỗ";
    } else {
        const seat = selectedSeats[0];
        seatInfo.innerText = "Người lớn: " + seat.code;
        priceDiv.style.display = "block";
        priceDiv.innerText = Number(seat.price).toLocaleString() + " VNĐ";
        countDiv.innerText = "Đã chọn: 1/1 chỗ";
    }
}

function closeModal() {
    document.getElementById("myModal").style.display = "none";
    selectedSeats = [];
    updateSeatInfo();
}

