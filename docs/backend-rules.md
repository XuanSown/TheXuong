# Backend Rules — TheXuong

> Quy tắc bắt buộc cho code backend Spring Boot (Java 21) của dự án TheXuong.
> Mọi PR/commit vi phạm các rule dưới đây đều sẽ bị từ chối.

---

## 1. Công nghệ & Môi trường

* **Java & Boot**: Sử dụng **Java 21** và **Spring Boot 3.5.x**.
* **Build Tool**: Sử dụng **Gradle Wrapper** (`./gradlew`) để đảm bảo tính nhất quán của build.

---

## 2. API Endpoint & CORS

* **Tiền tố API**: Mọi API cung cấp cho frontend phải bắt đầu bằng `/api/v1/` để đồng bộ với cấu hình Vite proxy của frontend.
* **CORS**: Cho phép origin `https://thexuong.xuansown.id.vn` và các domain local được chỉ định trong `application.yml` (app.cors.allowed-origins). Không được cấu hình `*`.

---

## 3. Quản lý Cơ sở dữ liệu (Database Schema)

* **Hệ quản trị**: Sử dụng **SQL Server** (cổng kết nối local mặc định: `1444`, database: `dbTheXuong`).
* **JPA/Hibernate**: Cấu hình `spring.jpa.hibernate.ddl-auto=none` ở môi trường chính để bảo toàn dữ liệu.
* **Cập nhật Schema**: 
  * Dự án sử dụng file SQL thủ công [dbTheXuong.sql](file:///D:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/dbTheXuong.sql) để khởi tạo và lưu trữ cấu trúc database.
  * Khi có bất kỳ thay đổi nào về bảng hoặc cột, phải thực hiện thay đổi schema thủ công trên DB cục bộ và cập nhật lại file SQL backup. *Tuyệt đối không sử dụng công cụ tự động đẩy (như push) gây ghi đè dữ liệu sản xuất.*

---

## 4. Quản lý Biến môi trường & Bảo mật

* **Không hardcode thông tin nhạy cảm**: Không commit mật khẩu, khóa bí mật API (như Mail, Cloudflare R2, Google Maps, OAuth2 Client ID/Secret) lên GitHub.
* **Tải biến môi trường**: Sử dụng library `springboot3-dotenv` để load các biến từ file `.env` vào config Spring Boot.
* **Cấu hình Cookie & Security**:
  * Các Cookie JWT Auth phải sử dụng `httpOnly=true`, `secure=true`, và `same-site=lax` để phòng tránh tấn công XSS và CSRF.
  * Giữ cấu hình `server.forward-headers-strategy=native` để TomCat tin tưởng và nhận diện IP của client được chuyển tiếp qua Cloudflare Tunnel.

---

## 5. Quy trình kiểm tra chất lượng code trước khi Commit/PR

Trước khi commit hoặc gửi PR, lập trình viên bắt buộc chạy các lệnh kiểm tra sau tại thư mục gốc của dự án:

```bash
# 1. Biên dịch thử mã nguồn (kiểm tra lỗi syntax nhanh)
./gradlew compileJava

# 2. Chạy toàn bộ các test case JUnit 5 để đảm bảo không lỗi logic cũ
./gradlew test
```
