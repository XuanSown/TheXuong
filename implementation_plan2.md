# Kế hoạch lưu Key vào file `.env` và sửa lỗi thanh toán VNPay

## 1. Lưu trữ Key vào file `.env` (Bảo mật tuyệt đối)

Dự án hiện tại **đã có sẵn thư viện `me.paulschwarz:springboot3-dotenv`** trong `build.gradle` (tự động load file `.env` vào Spring Environment) và `.env` **đã nằm trong `.gitignore`** nên key sẽ không bị lộ lên Git / GitHub.

Tạo file [`.env`](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/.env) tại thư mục gốc với nội dung:
```env
# VNPay Sandbox Configuration
VNPAY_TMN_CODE=VSU1QOMH
VNPAY_HASH_SECRET=W3KWLMYBGF7SN8JRFTAQNUXN6F2U5OUS
VNPAY_PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:5173/orders
```

---

## 2. Chi tiết các file chỉnh sửa

### 1. File cấu hình ứng dụng

#### [MODIFY] [application.properties](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/resources/application.properties)
Trỏ các cấu hình VNPay tới các biến môi trường trong `.env`:
```properties
# VNPay Configuration (Loads from .env)
vnpay.tmn-code=${VNPAY_TMN_CODE}
vnpay.secret-key=${VNPAY_HASH_SECRET}
vnpay.pay-url=${VNPAY_PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}
vnpay.return-url=${VNPAY_RETURN_URL:http://localhost:5173/orders}
```

#### [MODIFY] [application.yml](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/resources/application.yml)
Khai báo block `vnpay` đọc từ `.env`:
```yaml
vnpay:
  tmn-code: ${VNPAY_TMN_CODE}
  secret-key: ${VNPAY_HASH_SECRET}
  pay-url: ${VNPAY_PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}
  return-url: ${VNPAY_RETURN_URL:http://localhost:5173/orders}
```

---

### 2. File mã nguồn Java Backend

#### [MODIFY] [VNPayConfig.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/config/VNPayConfig.java)
- Chuẩn hóa getter/setter (`getTmnCode`, `setTmnCode`, `getSecretKey`, `setSecretKey`, `getPayUrl`, `setPayUrl`, `getReturnUrl`, `setReturnUrl`) và các alias tương thích ngược (`getVnp_TmnCode`, `getVnp_PayUrl`, `getVnp_ReturnUrl`).
- Cải thiện hàm lấy IP `getIpAddress(HttpServletRequest request)`: xử lý an toàn `null-safe` khi `request == null`, lấy IP thực từ `X-Forwarded-For`, fallback về `"127.0.0.1"`.
- Cải thiện hàm `hmacSHA512(String key, String data)`: kiểm tra chặt chẽ, log thông báo rõ ràng nếu thiếu secret key.

#### [MODIFY] [VNPayService.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/service/VNPayService.java)
- Cập nhật hàm `createOrder(Long orderId, int total, String orderInfo, HttpServletRequest request)`.
- Gắn `orderId` vào mã giao dịch `vnp_TxnRef` dạng: `orderId + "_" + VNPayConfig.getRandomNumber(6)`.
- Tạo chuỗi băm chuẩn SHA-512 với `secretKey` từ `.env`.

#### [MODIFY] [OrderRestController.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/OrderRestController.java)
- Thêm tham số `HttpServletRequest httpRequest` vào method `createOrder`.
- Truyền `order.getId()` và `httpRequest` vào `vnPayService.createOrder(order.getId(), order.getTotalMoney().intValue(), orderInfo, httpRequest);`.

---

## 3. Kế hoạch kiểm thử (Verification Plan)

1. **Biên dịch Java Backend:** Chạy `./gradlew compileJava` kiểm tra build thành công 100%.
2. **Kiểm tra luồng thực tế:** 
   - Đặt hàng với phương thức VNPay trên giao diện Web (`/checkout`).
   - Kiểm tra `POST /api/v1/orders` trả về `200 OK` chứa link thanh toán VNPay Sandbox với mã merchant `VSU1QOMH`.
   - Trình duyệt tự động chuyển tiếp tới cổng VNPay thanh toán thành công.
