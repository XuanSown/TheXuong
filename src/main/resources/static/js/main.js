const API_URL = "http://localhost:8080/api";

//1. Ktr token khi load cho index.html
async function checkAuth() {
    //TH 1: login gg xóa token trên url

    const userArea = document.getElementById('userArea');
    try {
        const response = await fetch(`${API_URL}/auth/profile`);
        if (response.ok) {
            const data = await response.json();
            userArea.innerHTML = `
            <span class="navbar-text text-light me-3">Xin chào, <b>${data.name}</b></span>
            <button onclick="logout()" class="btn btn-outline-light btn-sm">
            <i class="fa-solid fa-right-from-bracket me-1"></i> Đăng xuất
        </button>
        `;
        }  else {
            //chưa login/hết hạn token hiện login
            userArea.innerHTML = `<a href="login.html" class="btn btn-light btn-sm fw-bold">Đăng nhập ngay</a>`;
        }
    } catch (e) {
        console.error("Lỗi kiểm tra Auth: ",e);
        //Lỗi do mạng hiện nút login
        userArea.innerHTML = `<a href="login.html" class="btn btn-light btn-sm fw-bold">Đăng nhập ngay</a>`;
    }
}

// 2. Hàm Đăng nhập (Dùng cho Login.html)
async function handleLogin() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorMsg = document.getElementById('errorMsg');

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            window.location.href = 'index.html'; // Chuyển về trang chủ
        } else {
            errorMsg.style.display = 'block';
            errorMsg.innerText = 'Sai tài khoản hoặc mật khẩu!';
        }
    } catch (e) {
        console.error(e);
        alert('Lỗi kết nối server!');
    }
}

// 3. Hàm Load Sản phẩm (Gọi API Products)
async function loadProducts() {
    const grid = document.getElementById('productGrid');
    if (!grid) return;

    try {
        const response = await fetch(`${API_URL}/products`);
        if (!response.ok) throw new Error("Lỗi tải dữ liệu");
        const products = await response.json();
        // Clear spinner cũ
        grid.innerHTML = '';

        if(products.length === 0) {
            grid.innerHTML = '<div class="col-12 text-center">Chưa có sản phẩm.</div>';
            return;
        }

        // Render HTML chuẩn Bootstrap Card
        grid.innerHTML = products.map(p => `
            <div class="col-12 col-sm-6 col-md-3">
                <div class="card h-100 shadow-sm border-0">
                    <img src="${p.imageUrl}" class="card-img-top product-img-custom" alt="${p.name}" 
                         onerror="this.src='https://placehold.co/300x200?text=No+Image'">
                    <div class="card-body d-flex flex-column">
                        <small class="text-muted text-uppercase mb-1">${p.category || 'Thể thao'}</small>
                        <h5 class="card-title fw-bold">${p.name}</h5>
                        <p class="card-text text-danger fw-bold fs-5 mb-3">
                            ${new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p.price)}
                        </p>
                        <button class="btn btn-dark w-100 mt-auto">Thêm vào giỏ</button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error(e);
        grid.innerHTML = '<div class="col-12 text-center text-danger">Không thể tải dữ liệu server.</div>';
    }
}

// 4. Hàm Đăng xuất
async function logout() {
    await fetch(`${API_URL}/auth/logout`, { method: 'POST' }); // Gọi server để xóa cookie
    window.location.href = 'login.html';
}

// Tự động chạy khi load trang
document.addEventListener("DOMContentLoaded", () => {
    // Chạy checkAuth ở cả trang chủ và các trang khác (trừ login) để hiện Navbar đúng
    if (!window.location.pathname.includes('login.html')) {
        checkAuth();
    }
    if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname === '') {
        loadProducts();
    }
});