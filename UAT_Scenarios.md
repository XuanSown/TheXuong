# Tài Liệu Hướng Dẫn UAT (User Acceptance Testing) - The Xưởng

Tài liệu này được biên soạn dành cho Product Owner (PO), Khách hàng và các Stakeholders sử dụng để nghiệm thu chất lượng dự án The Xưởng dưới góc nhìn của người dùng thực tế.

## Yêu Cầu Môi Trường
- Ứng dụng đã được deploy lên môi trường Staging (hoặc UAT Environment).
- Sử dụng trình duyệt Chrome/Edge/Safari (Khuyến nghị Chrome bản mới nhất).
- Chuẩn bị sẵn tài khoản test (Tài khoản User thường và tài khoản Admin).

---

## Kịch Bản 1: Trải Nghiệm Mua Sắm & Thanh Toán (The Buyer Journey)
**Mục tiêu:** Xác nhận luồng mua hàng cốt lõi từ lúc tìm sản phẩm đến lúc thanh toán và kiểm tra tình trạng đơn hàng.

| Bước | Hành động của người dùng | Kết quả mong đợi | Trạng thái (Pass/Fail) |
|---|---|---|---|
| 1 | Truy cập trang chủ, xem các Banner quảng cáo. | Banner hiển thị rõ nét, slide chuyển động mượt. | |
| 2 | Tìm kiếm "Giày bóng đá" ở thanh search. | Ra đúng các sản phẩm giày bóng đá. | |
| 3 | Click vào 1 sản phẩm bất kỳ, xem ảnh chi tiết và mô tả. | Load nhanh, ảnh zoom được, thông tin size hiện đúng. | |
| 4 | Chọn Size và số lượng, bấm "Thêm vào giỏ". | Popup thông báo thành công, số lượng trên icon giỏ hàng tăng lên 1. | |
| 5 | Mở Giỏ hàng, bấm "Thanh toán". | Chuyển sang màn hình điền địa chỉ. | |
| 6 | Nhập mã giảm giá hợp lệ. | Tổng tiền được trừ tương ứng. | |
| 7 | Chọn phương thức thanh toán VNPay và tiến hành thanh toán. | Chuyển sang cổng VNPay giả lập, thanh toán xong tự quay về trang "Đặt hàng thành công". | |
| 8 | Vào mục "Đơn hàng của tôi". | Thấy đơn hàng vừa tạo có trạng thái "PENDING". | |

---

## Kịch Bản 2: Trải Nghiệm Khách Hàng Thân Thiết (The Loyalty Journey)
**Mục tiêu:** Xác nhận logic tích điểm và đổi voucher hoạt động đúng nghiệp vụ.

| Bước | Hành động của người dùng | Kết quả mong đợi | Trạng thái (Pass/Fail) |
|---|---|---|---|
| 1 | Truy cập trang Profile -> Mục "Khách hàng thân thiết". | Thấy điểm tích lũy được cộng từ đơn hàng ở Kịch Bản 1. | |
| 2 | Chuyển sang tab "Đổi Voucher". | Xem danh sách Voucher có thể đổi bằng điểm. | |
| 3 | Bấm đổi 1 Voucher trị giá 100 điểm. | Báo đổi thành công, số dư điểm bị trừ đi 100. | |
| 4 | Vào kho "Voucher của tôi". | Nhìn thấy Voucher vừa đổi, trạng thái "Chưa sử dụng". | |

---

## Kịch Bản 3: Trải Nghiệm Quản Trị Hệ Thống (The Admin Journey)
**Mục tiêu:** Đảm bảo Admin có đủ công cụ để kiểm soát và điều hành hệ thống.

| Bước | Hành động của người dùng | Kết quả mong đợi | Trạng thái (Pass/Fail) |
|---|---|---|---|
| 1 | Đăng nhập bằng tài khoản có quyền `ADMIN`. | Truy cập được vào `/admin/dashboard`. | |
| 2 | Xem Dashboard thống kê. | Các biểu đồ doanh thu, số lượng đơn hàng, users hiển thị data hợp lý. | |
| 3 | Vào mục "Quản lý đơn hàng", tìm đơn hàng vừa đặt ở Kịch Bản 1. | Đơn hàng nằm trong danh sách. | |
| 4 | Cập nhật trạng thái đơn hàng sang `DELIVERING` (Đang giao). | Đơn hàng đổi trạng thái thành công. | |
| 5 | Vào mục "Quản lý User", tìm tài khoản User vừa test. | Nhìn thấy thông tin chi tiết. | |
| 6 | Thử "Khóa tài khoản" User đó. | Nút khóa hoạt động, User đó nếu đang đăng nhập sẽ bị văng ra hoặc không gọi API mua hàng được nữa. | |
| 7 | Vào "Nhật ký hệ thống" (Audit Logs). | Thấy log ghi nhận "Admin vừa khóa user X" và "Admin đổi trạng thái đơn Y". | |

---

## Kịch Bản 4: Thử Thách Bot AI
**Mục tiêu:** Đánh giá độ nhạy bén và thông minh của Chatbot.

| Bước | Hành động của người dùng | Kết quả mong đợi | Trạng thái (Pass/Fail) |
|---|---|---|---|
| 1 | Bấm vào bong bóng Chatbot góc màn hình. | Bảng chat hiện lên mượt mà. | |
| 2 | Hỏi: "Cửa hàng có giày chạy bộ nào dưới 2 triệu không?". | Bot trả lời gợi ý các mẫu giày có trong CSDL thỏa mãn yêu cầu. | |
| 3 | Hỏi một câu không liên quan (vd: "Hôm nay trời mưa không?"). | Bot từ chối trả lời khéo léo và điều hướng về thời trang thể thao. | |

---

**Lưu ý khi nghiệm thu:**
Nếu phát hiện bất kỳ giao diện vỡ layout nào, hoặc lỗi nghiệp vụ (Voucher hết hạn vẫn đổi được...), vui lòng ghi chú thẳng vào cột "Trạng thái" và cung cấp Ảnh chụp màn hình cho Team Dev.
