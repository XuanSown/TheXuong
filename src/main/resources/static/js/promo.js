// src/main/resources/static/js/promo.js

document.addEventListener("DOMContentLoaded", () => {
    // 1. Định nghĩa HTML của Popup (Bạn sửa link ảnh ở đây)
    const promoHTML = `
    <div id="promoPopup" class="promo-overlay">
        <div class="promo-content">
            <button class="close-btn" onclick="closePromo()">
                <i class="fa-solid fa-xmark"></i>
            </button>
            <img src="/img/promo.png" style="width: 650px; height: 500px" 
                 alt="Khuyến mãi" class="promo-img">
        </div>
    </div>
    `;

    // 2. Chèn HTML vào cuối thẻ body
    document.body.insertAdjacentHTML('beforeend', promoHTML);

    // 3. Kích hoạt hiển thị (Chỉ hiện ở trang chủ)
    if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname === '') {
        showPromo();
    }
});

// --- Logic điều khiển ---

function showPromo() {
    const popup = document.getElementById('promoPopup');
    if (popup) {
        setTimeout(() => { popup.classList.add('show'); }, 500); // Hiện sau 0.5s
        setTimeout(() => { closePromo(); }, 5500); // Tự tắt sau 5s
    }
}

// Gán hàm vào window để nút close gọi được
window.closePromo = function() {
    const popup = document.getElementById('promoPopup');
    if (popup) {
        popup.classList.remove('show');
    }
}