import sys

html_content = '''<div class="container py-5">
    <a th:href="@{/orders}" class="btn btn-light shadow-sm mb-4 fw-bold rounded-pill px-4">
        <i class="fa-solid fa-arrow-left me-2"></i>Trở về danh sách
    </a>

    <div th:if="${successMessage}" class="alert alert-success border-0 shadow-sm rounded-3" th:text="${successMessage}"></div>
    <div th:if="${errorMessage}" class="alert alert-danger border-0 shadow-sm rounded-3" th:text="${errorMessage}"></div>

    <div class="row">
        <div class="col-12 mb-4">
            <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
                <div class="card-body p-4 p-md-5 position-relative">
                    <!-- Background pattern -->
                    <div class="position-absolute top-0 end-0 opacity-10" style="font-size: 15rem; transform: translate(20%, -20%);">
                        <i class="fa-solid fa-box-open"></i>
                    </div>

                    <div class="row align-items-center position-relative z-1">
                        <div class="col-md-6 mb-4 mb-md-0">
                            <span class="badge bg-dark px-3 py-2 rounded-pill mb-2">ĐƠN HÀNG #[[${order.id}]]</span>
                            <h2 class="fw-bold mb-1">Chi tiết đơn hàng</h2>
                            <p class="text-muted mb-0">
                                Cảm ơn bạn đã mua sắm tại TheXuong. Dưới đây là thông tin chi tiết về đơn hàng của bạn.
                            </p>
                        </div>
                        <div class="col-md-6 text-md-end">
                            <p class="mb-1 text-muted">Tổng cộng</p>
                            <h1 class="text-danger fw-bold mb-0" th:text="${#numbers.formatDecimal(order.totalMoney, 0, 'COMMA', 0, 'POINT')} + ' đ'"></h1>
                        </div>
                    </div>
                    
                    <hr class="my-4 opacity-25">
                    
                    <!-- Stepper -->
                    <div class="px-2 px-md-4 mt-5 mb-3">
                        <div class="position-relative d-flex justify-content-between align-items-center">
                            <!-- Progress Bar Base -->
                            <div class="progress position-absolute" style="height: 4px; top: 25px; left: 10%; width: 80%; z-index: 1;">
                                <div class="progress-bar bg-success" role="progressbar" 
                                     th:style="'width: ' + (${order.status == 'PENDING'} ? '0%' : (${order.status == 'APPROVED'} ? '50%' : (${order.status == 'SHIPPED'} ? '100%' : '0%')))"></div>
                            </div>
                            
                            <!-- Step 1: PENDING -->
                            <div class="text-center position-relative z-3" style="width: 80px;">
                                <div class="rounded-circle d-flex align-items-center justify-content-center mb-2 mx-auto" 
                                     th:classappend="${order.status != 'CANCELLED'} ? 'bg-success text-white' : 'bg-danger text-white'" 
                                     style="width: 50px; height: 50px; border: 4px solid #fff; box-shadow: 0 0 15px rgba(0,0,0,0.1);">
                                    <i class="fa-solid" th:classappend="${order.status == 'CANCELLED'} ? 'fa-xmark fs-4' : 'fa-clipboard-list fs-5'"></i>
                                </div>
                                <small class="fw-bold d-block" th:text="${order.status == 'CANCELLED'} ? 'Đã hủy' : 'Chờ duyệt'"></small>
                            </div>

                            <!-- Step 2: APPROVED -->
                            <div class="text-center position-relative z-3" style="width: 80px;">
                                <div class="rounded-circle d-flex align-items-center justify-content-center mb-2 mx-auto" 
                                     th:classappend="${order.status == 'APPROVED' or order.status == 'SHIPPED'} ? 'bg-success text-white' : 'bg-light text-muted'" 
                                     style="width: 50px; height: 50px; border: 4px solid #fff; box-shadow: 0 0 15px rgba(0,0,0,0.1);">
                                    <i class="fa-solid fa-truck-fast fs-5"></i>
                                </div>
                                <small class="fw-bold d-block text-muted" th:classappend="${order.status == 'APPROVED' or order.status == 'SHIPPED'} ? 'text-success' : ''">Đã duyệt</small>
                            </div>

                            <!-- Step 3: SHIPPED -->
                            <div class="text-center position-relative z-3" style="width: 80px;">
                                <div class="rounded-circle d-flex align-items-center justify-content-center mb-2 mx-auto" 
                                     th:classappend="${order.status == 'SHIPPED'} ? 'bg-success text-white' : 'bg-light text-muted'" 
                                     style="width: 50px; height: 50px; border: 4px solid #fff; box-shadow: 0 0 15px rgba(0,0,0,0.1);">
                                    <i class="fa-solid fa-box-check fs-5"></i>
                                </div>
                                <small class="fw-bold d-block text-muted" th:classappend="${order.status == 'SHIPPED'} ? 'text-success' : ''">Đã giao</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-4 mb-4">
            <div class="card shadow-sm border-0 rounded-4 h-100">
                <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
                    <h5 class="fw-bold mb-0"><i class="fa-solid fa-address-card text-primary me-2"></i>Thông tin người nhận</h5>
                </div>
                <div class="card-body p-4">
                    <form th:action="@{/order/update}" method="post">
                        <input type="hidden" name="orderId" th:value="${order.id}">

                        <div class="mb-3">
                            <label class="form-label text-muted small fw-bold text-uppercase">Họ và tên</label>
                            <input type="text" class="form-control bg-light border-0" th:value="${order.fullName}" readonly>
                        </div>

                        <div class="mb-3">
                            <label class="form-label text-muted small fw-bold text-uppercase">Số điện thoại</label>
                            <input type="text" name="phoneNumber" class="form-control" 
                                   th:classappend="${order.status != 'PENDING'} ? 'bg-light border-0' : ''"
                                   th:value="${order.phoneNumber}"
                                   th:readonly="${order.status != 'PENDING'}">
                        </div>

                        <div class="mb-4">
                            <label class="form-label text-muted small fw-bold text-uppercase">Địa chỉ giao hàng</label>
                            <textarea name="address" class="form-control" rows="3"
                                      th:classappend="${order.status != 'PENDING'} ? 'bg-light border-0' : ''"
                                      th:readonly="${order.status != 'PENDING'}"
                                      th:text="${order.address}"></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary w-100 rounded-pill fw-bold mb-2"
                                th:if="${order.status == 'PENDING'}">
                            <i class="fa-solid fa-floppy-disk me-2"></i>Cập nhật địa chỉ
                        </button>
                    </form>

                    <div th:if="${order.status == 'PENDING'}">
                        <button type="button" class="btn btn-outline-danger w-100 rounded-pill fw-bold"
                                data-bs-toggle="modal" data-bs-target="#cancelModal">
                            <i class="fa-solid fa-ban me-2"></i>Hủy đơn hàng
                        </button>
                    </div>

                    <div class="alert alert-secondary border-0 rounded-3 small mt-3" th:unless="${order.status == 'PENDING'}">
                        <i class="fa-solid fa-circle-info text-primary me-2"></i>Đơn hàng đã được xử lý, không thể thay đổi thông tin hay hủy bỏ.
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-8 mb-4">
            <div class="card shadow-sm border-0 rounded-4 h-100">
                <div class="card-header bg-white border-0 pt-4 pb-0 px-4 d-flex justify-content-between align-items-center">
                    <h5 class="fw-bold mb-0"><i class="fa-solid fa-bag-shopping text-primary me-2"></i>Sản phẩm đã mua</h5>
                    <span class="badge bg-light text-dark border">[[${order.orderDetails.size()}]] sản phẩm</span>
                </div>
                <div class="card-body p-4">
                    <div class="table-responsive">
                        <table class="table align-middle table-borderless">
                            <thead class="table-light rounded-3">
                            <tr>
                                <th class="text-uppercase small fw-bold text-muted rounded-start" style="width: 45%;">Sản phẩm</th>
                                <th class="text-uppercase small fw-bold text-muted">Size</th>
                                <th class="text-uppercase small fw-bold text-muted text-center">Số lượng</th>
                                <th class="text-uppercase small fw-bold text-muted text-end rounded-end">Thành tiền</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr th:each="detail : ${order.orderDetails}" class="border-bottom">
                                <td class="py-3">
                                    <div class="d-flex align-items-center">
                                        <div class="bg-light rounded p-2 me-3 d-flex align-items-center justify-content-center" style="width: 60px; height: 60px;">
                                            <i class="fa-solid fa-box text-secondary fs-4"></i>
                                        </div>
                                        <div>
                                            <h6 class="mb-0 fw-bold" th:text="${detail.productName}"></h6>
                                            <small class="text-muted" th:text="${#numbers.formatDecimal(detail.price, 0, 'COMMA', 0, 'POINT')} + ' đ/sp'"></small>
                                        </div>
                                    </div>
                                </td>
                                <td class="py-3"><span class="badge bg-dark px-2 py-1" th:text="${detail.size}"></span></td>
                                <td class="py-3 text-center fw-bold" th:text="'x' + ${detail.quantity}"></td>
                                <td class="py-3 text-end fw-bold text-danger" th:text="${#numbers.formatDecimal(detail.totalPrice, 0, 'COMMA', 0, 'POINT')} + ' đ'"></td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>'''

file_path = r'c:\Users\ndnan\TheXuong\src\main\resources\templates\my-order-detail.html'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

start_marker = '<div class="container py-5">'
end_marker = '<!--footer-->'

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + html_content + '\n\n' + content[end_idx:]
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print('Successfully updated my-order-detail.html')
else:
    print('Failed to find markers')
