# Các lỗi và chức năng đã sửa

1. **Icon giỏ hàng:**
   - Đã sửa lỗi icon giỏ hàng không cập nhật số lượng theo số lượng sản phẩm đang có trong giỏ. Hiện tại icon đã hiển thị đúng số lượng.

2. **Luồng thêm sản phẩm vào giỏ hàng đối với khách chưa đăng nhập:**
   - **Trước đây:** Khách chưa đăng nhập thêm sản phẩm vào giỏ hàng thì hệ thống bắt đăng nhập; đăng nhập xong lại bị chuyển về trang chủ và các sản phẩm trong giỏ biến mất.
   - **Đã sửa thành:** Khách chưa đăng nhập vẫn thêm vào giỏ hàng được bình thường. Khi ấn nút "Thanh toán" mới yêu cầu đăng nhập. Sau khi đăng nhập thành công, người dùng vẫn ở lại trang giỏ hàng với đầy đủ các sản phẩm đã thêm từ trước.

3. **Thêm trang chi tiết thông tin hỗ trợ:**
   - Đã cập nhật giao diện và thêm nội dung chi tiết cho các trang:
     - Hướng dẫn chọn size
     - Chính sách đổi trả
     - Phương thức thanh toán
     - Kiểm tra đơn hàng
     - Chính sách bảo mật
     - Điều khoản dịch vụ
     - Chính sách vận chuyển
     - Liên hệ hợp tác

4. **Giao diện mục Thương hiệu:**
   - **Trước đây:** Mục "Khác" của phần Thương hiệu bị lấy nhầm dữ liệu/link của mục "Khác" bên phần Thể thao.
   - **Đã sửa thành:** Cập nhật lại để phần Thương hiệu lấy đúng mục "Khác" của riêng nó.

5. **Chi tiết chức năng đăng nhập bằng Google (Mới fix):**
   - **Database & Entity:** Thêm thuộc tính `provider` (nhận giá trị LOCAL hoặc GOOGLE) trong Entity `User` để phân loại tài khoản.
   - **Xử lý đăng nhập (`OAuth2SuccessHandler`):**
     - Khi đăng nhập thành công qua OAuth2, hệ thống tự động lấy `email` và `name` (tên hiển thị) từ tài khoản Google.
     - Đồng bộ với cơ sở dữ liệu: Nếu là người dùng mới, hệ thống tự động khởi tạo tài khoản với `provider` = "GOOGLE", không cần mật khẩu (`password=""`), tự động gán role `USER` và nhóm quyền mặc định là "Khách hàng".
     - Đồng bộ quyền hạn (Authorities): Nạp lại đầy đủ các Role và RoleGroup từ Database vào Spring Security Context để nhận diện chính xác Admin hay User.
   - **Bảo mật và Quản lý (`UserManagementController`, `ForgotPasswordController`):**
     - Vô hiệu hóa tính năng lấy lại/đổi mật khẩu thông thường cho tài khoản Google.
     - Trong trang Quản lý người dùng của Admin, tài khoản Google bị khóa cứng chức danh là "Khách hàng", không cho phép chuyển đổi sang các nhóm quyền quản trị khác nhằm đảm bảo an toàn.
   - **Trải nghiệm người dùng:** Sau khi đăng nhập bằng Google thành công, người dùng tự động được chuyển hướng về trang chủ một cách mượt mà.
   - **Nguyên nhân gốc rễ lỗi giỏ hàng sau khi login Google:** Khi đăng nhập bằng tài khoản Google, thông tin Principal (đại diện cho phiên đăng nhập trong Spring Security) theo mặc định sẽ lấy mã số ID định danh của Google (một chuỗi số rất dài) làm `name`. Tuy nhiên, trong `CartService` (Giỏ hàng) và `OrderService` (Đơn hàng), hệ thống lại đang dùng `principal.getName()` và mặc định kỳ vọng đó là Email để tìm kiếm User trong Database. Kết quả là khi nhấn "Thêm vào giỏ hàng", hệ thống lấy cái ID bằng số kia đem đi tìm Email -> Không thấy User -> Báo lỗi văng ra màn hình Whitelabel Error Page.
   - **Cách đã fix:** Đã cập nhật lại `OAuth2SuccessHandler`. Từ nay trở đi, khi đăng nhập bằng Google, hệ thống ép Spring Security phải dùng Email làm tên đại diện (`getName()`) thay vì dùng ID của Google, giúp luồng thêm giỏ hàng và thanh toán hoạt động bình thường.

6. **Lỗi "Invalid column name 'token'" khi thao tác với giỏ hàng:**
   - **Nguyên nhân:** Xảy ra do thiếu cột `token` trong bảng `Carts` dưới Database. Khi làm tính năng "Giỏ hàng cho Khách (Guest)", hệ thống có bổ sung thêm cột `token` vào bảng `Carts`. Tuy nhiên do cấu hình `spring.jpa.hibernate.ddl-auto=none` (trước đó là `update`), Hibernate không tự động can thiệp sửa Database nên cột này chưa được tạo ra dưới SQL Server.
   - **Cách khắc phục:** Xử lý thủ công qua SQL Server Management Studio (SSMS). Mở SSMS, chọn database `dbTheXuong`, mở New Query và chạy lệnh SQL thêm cột `token` vào bảng `Carts` (ví dụ: `ALTER TABLE Carts ADD token VARCHAR(255);`).

7. **Hoàn thiện luồng lưu Số điện thoại và tự động điền thông tin Thanh toán:**
   - **Trang Hồ sơ cá nhân (`profile.html`):** Bổ sung thêm một ô nhập "Số điện thoại" ngay bên dưới "Họ và tên".
   - **Logic cập nhật (`ProfileController.java`):** Đã thêm code để lưu thông tin "Số điện thoại" xuống Database mỗi khi nhấn "Lưu thay đổi".
   - **Trang Thanh toán (`checkout.html`):** Thiết lập tự động lấy `phoneNumber` và `address` của tài khoản đang đăng nhập để điền sẵn vào 2 ô nhập liệu trên form thanh toán, giúp người dùng không phải nhập lại nhiều lần.

8. **Hoàn thiện logic quản lý số lượng tồn kho theo trạng thái đơn hàng:**
   - **Từ "Chờ duyệt" sang "Đã duyệt":** Trừ số lượng tồn kho tương ứng với từng sản phẩm/size trong đơn. (Nếu kho không đủ sẽ báo lỗi và không cho duyệt).
   - **Từ "Đã duyệt" sang "Đã giao":** Giữ nguyên số lượng (vì đã trừ ở bước Duyệt rồi).
   - **Từ "Đã duyệt" hoặc "Đã giao" về "Đã hủy":** Hoàn trả (cộng lại) đúng số lượng tồn kho đó vào cơ sở dữ liệu.
   - **Những file đã sửa đổi:**
     - `ProductVariantRepository.java`: Viết thêm lệnh truy vấn tìm tồn kho theo Sản phẩm và Size.
     - `OrderService.java`: Viết hàm xử lý nghiệp vụ thay đổi Status và cập nhật Tồn kho (kiểm tra chặt chẽ số lượng âm).
     - `OrderManagementController.java`: Đưa hàm này vào 2 chỗ cập nhật đơn hàng của Admin (tại nút duyệt nhanh và cả lúc ấn Lưu trong trang Edit).

9. **Chức năng Xem chi tiết đơn hàng:**
   - Đã bổ sung đầy đủ tính năng xem chi tiết thông tin đơn hàng dành cho cả Khách hàng (trong trang hồ sơ cá nhân) và Admin (trong trang quản lý đơn hàng).
