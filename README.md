# TheXuong - Sport Apparel E-commerce

Dự án TheXuong là một trang web bán đồ thể thao được phát triển bởi công nghệ Spring Boot và Spring Security. Dự án này nhằm mục đích cung cấp nền tảng thương mại điện tử chuyên biệt cho các sản phẩm thể thao.

## 🛠 Công nghệ sử dụng
- **Java:** JDK 21
- **Framework:** Spring Boot 3.5.9
- **Database:** SQL Server
- **Frontend:** HTML5, Bootstrap 5, Thymeleaf
- **Build Tool:** Gradle

## 🔧 Hướng dẫn chạy dự án (Getting Started)

### 1. Cấu hình Database
Để cấu hình kết nối với cơ sở dữ liệu, mở file `application.yml` hoặc `application.properties`. Cập nhật thông tin sau:
- **Database Name:** `dbTheXuong`
- **Username/Password:** Thay thế bằng thông tin đăng nhập SQL Server của bạn

Bạn cần chạy file SQL `dbTheXuong.sql` để tạo các bảng và dữ liệu mẫu.

### 2. Cấu hình Biến Môi trường (Bắt buộc)
Dự án sử dụng Google Login, do đó bạn cần cung cấp hai biến môi trường:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Bạn có thể đặt chúng trong file cấu hình hoặc sửa trực tiếp tại code.

### 3. Chạy ứng dụng
Để chạy dự án, hãy mở terminal tại thư mục gốc và thực hiện lệnh sau:
```bash
./gradlew bootRun
```

### 4. Cấu hình tuổi thọ web
Để thay đổi tuổi thọ web, bạn cần chỉnh sửa các file sau:

- **application.yml**:
  ```yaml
  spring:
    security:
      oauth2:
        client:
          registration:
            google:
              redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
  ```

- **JwtService.java**:
  ```java
  @Value("${jwt.expiration}")
  private long jwtExpiration;

  // ... code ...
  ```

- **AuthController.java**:
  ```java
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    // ... code ...
  }
  ```

- **OAuth2SuccessHandler.java**:
  ```java
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String token = jwtService.generateToken(userDetails.getUsername());

    // ... code ...
  }
  ```

Sau khi thực hiện các thay đổi, bạn có thể nhấn nút Apply để áp dụng chúng vào mã.

## 📚 Tài liệu tham khảo
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)