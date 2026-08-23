# Kế hoạch sửa logic & hiển thị trạng thái Tồn kho, Giỏ hàng và Thanh toán

Tài liệu này phân tích chi tiết các vị trí cần sửa đổi trong hệ thống (Backend Spring Boot & Frontend Vue 3 + Pinia) để giải quyết triệt để 2 vấn đề:
1. **Xử lý sản phẩm & size hết hàng/không đủ số lượng**: Vô hiệu hóa size hết hàng, hiển thị số lượng tồn kho, chặn thêm vượt quá số lượng trong kho, thông báo lỗi rõ ràng.
2. **Xử lý xung đột thanh toán đồng thời (Người A và B cùng mua sản phẩm cuối)**: Người B không bị logout ra `/login`, không bị mất giỏ hàng, hiển thị thông báo lỗi chi tiết sản phẩm nào đã hết, tự động cập nhật lại giỏ hàng và trạng thái tồn kho.

---

## User Review Required

> [!IMPORTANT]
> - Các thay đổi này tuân thủ luồng Stateless JWT hiện tại và không làm ảnh hưởng đến cơ chế thanh toán VNPay / COD.
> - Khi anh duyệt kế hoạch này, em sẽ tiến hành chỉnh sửa code theo đúng từng bước bên dưới.

---

## Phân tích nguyên nhân gốc rễ (Root Cause Analysis)

| Vấn đề | Nguyên nhân tại Backend | Nguyên nhân tại Frontend |
| :--- | :--- | :--- |
| **1. Size hết hàng vẫn chọn và thêm vào giỏ được** | `CartService` ném `RuntimeException` chung chung, `GlobalExceptionHandler` bắt thành 500 thay vì 400. `CartItemDto` thiếu `stockQuantity`. | `ProductDetail.vue` không kiểm tra `size.quantity <= 0` trên nút size, nút `+` không giới hạn `max = size.quantity`. `cart.store.ts` bắt lỗi `addItem` rồi âm thầm lưu vào `guestCart` (localStorage) khiến UI tưởng thêm thành công và báo "Thành công". |
| **2. B thanh toán sau A bị logout ra `/login` và mất giỏ hàng** | `OrderRestController` bắt exception và trả về lỗi chung chung `"Đã xảy ra lỗi khi đặt hàng..."`. | 1) `cart.store.ts` khi bị lỗi âm thầm đẩy item vào guest cart. Khi auth check hoặc login lại thì `authStore.clear()` xóa sạch `guest_cart_items`.<br>2) `Checkout.vue` không lấy message lỗi cụ thể từ server, không refresh lại giỏ hàng khi order thất bại.<br>3) Thiếu pre-check tồn kho và cảnh báo trực tiếp trên giao diện Checkout/Cart khi sản phẩm bị mua mất. |

---

## Proposed Changes

### 1. Backend (Spring Boot)

#### [NEW] [InsufficientStockException.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/exception/InsufficientStockException.java)
- Tạo custom exception `InsufficientStockException` kế thừa `RuntimeException` chuyên xử lý các lỗi thiếu hàng, hết size, vượt tồn kho.

#### [MODIFY] [GlobalExceptionHandler.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java)
- Thêm `@ExceptionHandler(InsufficientStockException.class)` trả về `HTTP 400 BAD_REQUEST` với `ApiResponse.error(ex.getMessage())`.

#### [MODIFY] [CartItemDto.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/dto/CartItemDto.java)
- Bổ sung trường `private Integer stockQuantity;` để frontend biết số lượng tồn kho còn lại của size đó.

#### [MODIFY] [CartRestController.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/CartRestController.java)
- Cập nhật hàm `toCartItemDto(CartItem item)` để gán `stockQuantity = variant.getQuantity()`.

#### [MODIFY] [CartService.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/service/CartService.java)
- Trong `addToCart`: Ném `InsufficientStockException` với thông báo tiếng Việt rõ ràng khi `totalQtyAfterAdd > currentStock` hoặc khi `currentStock <= 0`.
- Trong `updateCartItemQuantity`: Ném `InsufficientStockException` khi `quantity > currentStock`.

#### [MODIFY] [InventoryService.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/service/InventoryService.java)
- Cập nhật `deductStock` ném `InsufficientStockException` kèm thông tin sản phẩm và size nếu không đủ tồn kho.

#### [MODIFY] [OrderService.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/service/OrderService.java)
- Trong `placeOrder`: Thêm bước tiền kiểm tra (Pre-validate) tồn kho cho **tất cả** items trong giỏ trước khi tạo OrderDetail và trừ kho. Nếu có sản phẩm hết hàng hoặc không đủ số lượng, ném `InsufficientStockException` chỉ rõ tên sản phẩm và size cụ thể (ví dụ: `"Sản phẩm 'Nike Air Jordan' (Size 42) chỉ còn 0 trong kho, không đủ số lượng để đặt hàng."`).

#### [MODIFY] [OrderRestController.java](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/OrderRestController.java)
- Trong `createOrder`: Bắt `InsufficientStockException` và `Exception`, trả về thông báo lỗi cụ thể `{"error": e.getMessage()}` với mã `HTTP 400` thay vì thông báo chung chung.

---

### 2. Frontend (Vue 3, Pinia, TypeScript)

#### [MODIFY] [types/cart.types.ts](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/types/cart.types.ts) & [types/product.types.ts](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/types/product.types.ts)
- Bổ sung trường `stockQuantity?: number` vào interface `CartItem` và `Product`.

#### [MODIFY] [cart.store.ts](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/stores/cart.store.ts)
- Sửa hàm `addItem`: Bỏ việc âm thầm bắt lỗi rồi chuyển sang guest cart khi người dùng đã đăng nhập. Nếu API trả lỗi (400 / hết hàng), `rethrow error` để component hiển thị toast lỗi cụ thể.
- Sửa hàm `updateItem`: Đảm bảo ném lỗi khi cập nhật số lượng thất bại.

#### [MODIFY] [ProductDetail.vue](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/views/ProductDetail.vue)
- **Vô hiệu hóa size hết hàng**:
  - Kiểm tra `size.quantity <= 0`: Disable nút size (`:disabled="size.quantity <= 0"`), thêm style mờ / gạch ngang / nhãn `(Hết)`.
  - Không cho phép người dùng click chọn size đã hết hàng.
- **Hiển thị trạng thái tồn kho**:
  - Khi chọn 1 size còn hàng: Hiển thị dòng thông báo tồn kho `Còn X sản phẩm trong kho`.
  - Nếu sản phẩm hết sạch tất cả các size (`totalStock <= 0`): Hiển thị badge `TẠM HẾT HÀNG`, vô hiệu hóa 2 nút "Thêm vào giỏ hàng" và "Mua ngay".
- **Giới hạn bộ đếm số lượng**:
  - Nút `+` bị disable khi `quantity >= selectedVariant.quantity`.
  - Không cho phép nhập hoặc chọn số lượng vượt quá số lượng tồn kho của size được chọn.
- **Thông báo lỗi khi thêm vào giỏ / mua ngay**:
  - Bắt lỗi từ `cartStore.addItem` và hiển thị toast lỗi với nội dung chi tiết từ backend (`error.response?.data?.error || error.response?.data?.message`).

#### [MODIFY] [Cart.vue](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/views/Cart.vue)
- **Cảnh báo tồn kho từng sản phẩm trong giỏ**:
  - Nếu `item.stockQuantity === 0`: Hiện cảnh báo đỏ `⚠️ Sản phẩm size này hiện đã hết hàng trong kho. Vui lòng xóa khỏi giỏ để tiếp tục thanh toán.`
  - Nếu `item.quantity > item.stockQuantity`: Hiện cảnh báo vàng `⚠️ Kho chỉ còn ${item.stockQuantity} sản phẩm. Vui lòng giảm số lượng.`
  - Disable nút `+` khi số lượng đạt mức tồn kho tối đa (`item.quantity >= item.stockQuantity`).
- **Khóa nút Thanh toán**:
  - Nếu giỏ hàng có bất kỳ sản phẩm nào bị hết hàng hoặc vượt tồn kho, disable nút "Thanh toán" (Checkout) và hiển thị thông báo nhắc nhở người dùng cập nhật lại giỏ hàng.
- **Bắt lỗi khi tăng/giảm số lượng**:
  - Thêm `toast.error` khi `increaseQuantity` thất bại.

#### [MODIFY] [Checkout.vue](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/views/Checkout.vue)
- **Xử lý lỗi khi Người B thanh toán sản phẩm vừa bị Người A mua hết**:
  - Khi `createOrder` thất bại:
    1. Lấy thông báo lỗi chi tiết từ backend (`error.response?.data?.error || error.response?.data?.message`) và hiển thị bằng `checkoutToast.error(...)`.
    2. **Không** gọi `cartStore.clearCart()`.
    3. **Không** redirect về `/login`. Giữ nguyên người dùng ở trang thanh toán / giỏ hàng.
    4. Tự động gọi `await cartStore.fetchCart()` để cập nhật lại số lượng tồn kho mới nhất, giúp người dùng B thấy ngay sản phẩm nào đã hết và có thể gỡ bỏ để hoàn tất đơn hàng.

#### [MODIFY] [ProductCard.vue](file:///d:/FPT%20Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/components/ui/ProductCard.vue)
- Hiển thị nhãn/overlay `HẾT HÀNG` trên ảnh sản phẩm nếu `product.stockQuantity === 0`.

---

## Verification Plan

### Automated Tests
- Chạy unit test backend: `./gradlew test`
- Chạy unit test frontend: `cd frontend && npm run test:unit`

### Manual Verification Flow
1. **Kiểm tra hiển thị và chọn size trên trang Chi tiết sản phẩm (`/product-detail/{id}`)**:
   - Sản phẩm có size có số lượng = 0: Nút size đó bị gạch ngang, mờ và không bấm được.
   - Chọn size còn 2 cái: Nút `+` chỉ cho tăng tối đa đến 2, không thể tăng lên 3.
   - Chọn size còn hàng -> Thêm vào giỏ -> Hiển thị thông báo thành công.
2. **Kiểm tra trong Giỏ hàng (`/cart`)**:
   - Tăng số lượng đến mức tối đa của kho -> Nút `+` bị disable.
   - Nếu trong DB chỉnh sửa stock về 0 -> Trang giỏ hàng hiển thị cảnh báo đỏ và khóa nút "Thanh toán".
3. **Kịch bản 2 người cùng thanh toán sản phẩm cuối (Người A và B)**:
   - Cả A và B đều thêm 1 sản phẩm cuối cùng vào giỏ và cùng vào trang `/checkout`.
   - A bấm "Đặt hàng" trước -> Thành công, số lượng trong kho về 0.
   - B bấm "Đặt hàng" sau -> Thông báo lỗi rõ ràng `"Sản phẩm '...' (Size ...) chỉ còn 0 trong kho, không đủ số lượng để đặt hàng."`
   - B không bị logout ra `/login`, không bị mất giỏ hàng, giỏ hàng tự cập nhật cảnh báo hết hàng.
