# LỖI COMPILE - QUICK FIX GUIDE

## Nguyên nhân:
Lombok annotation processing không hoạt động với Java 21 + Gradle. Các @Data, @Builder không generate code.

## Giải pháp tạm thời: THÊM GETTERS/SETTERS THỦ CÔNG

### Các entity cần fix (thêm getters/setters thủ công):

1. **User.java** - Có đầy đủ getters/setters qua @Data? ✓
2. **Product.java** - Có @Data ✓
3. **Cart.java** - Có @Data ✓
4. **Order.java** - Có @Data ✓
5. **ProductVariant.java** - Có @Data ✓
6. **Size.java** - Có @Data ✓
7. **PointTransaction.java** - Có @Data ✓
8. **PointTier.java** - Cần check
9. **UserPoints.java** - Cần check
10. **Voucher.java** - Cần check
11. **ChatMessage.java** - Cần check (thiếu setters)
12. **OrderStatus.java** - Enum

## FIX CHÍNH:

### 1. Thêm getters/setters thủ công vào ChatMessage.java
ChatMessage thiếu setter methods. Cần bổ sung:
- setSender(String)
- setType(MessageType)
- setContent(String)
- setOnlineUsers(List<String>)

### 2. Thêm getters/setters vào PointTier.java
- getMinTotalSpent(), setMinTotalSpent(BigDecimal)
- getMinTotalPoints(), setMinTotalPoints(Integer)
- getBonusPoints(), setBonusPoints(Integer)
- getDiscountPercentage(), setDiscountPercentage(BigDecimal)

### 3. Thêm getters/setters vào UserPoints.java
- getCurrentPoints(), setCurrentPoints(Integer)
- getUserId(), setUserId(Long)
- getTierCode(), setTierCode(String)

### 4. Thêm getters/setters vào Voucher.java
- getId(), setId(Long)
- getCode(), setCode(String)
- getName(), setName(String)
- getDescription(), setDescription(String)
- getDiscountAmount(), setDiscountAmount(BigDecimal)
- getRequiredPoints(), setRequiredPoints(Integer)
- getMinOrderAmount(), setMinOrderAmount(BigDecimal)
- getVipOnly(), setVipOnly(Boolean)
- getStatus(), setStatus(Status)
- getStartDate(), setStartDate(LocalDateTime)
- getEndDate(), setEndDate(LocalDateTime)
- getCreatedAt(), setCreatedAt(LocalDateTime)
- getUpdatedAt(), setUpdatedAt(LocalDateTime)

### 5. Thêm getters/setters vào AdminVoucherRestController DTOs:
- VoucherForm (line 370-401)
- VoucherResponse (line 384-387)

### 6. Thêm getters/setters vào CartRestController DTOs:
- CartResponse (line 207-210)
- AddItemRequest (line 220-223)
- UpdateQuantityRequest

### 7. Fix imports trong các controller:
Thêm:
- import java.util.Map;
- import javax.validation.Valid;
- import org.slf4j.Logger;
- import org.slf4j.LoggerFactory;

### 8. Thêm logger vào ChatController và WebSocketEventListener:
```java
private static final Logger log = LoggerFactory.getLogger(ChatController.class);
private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
```

## ALTERNATIVE: Tắt Lombok và dùng code thủ công

Nếu Lombok vẫn không chạy, cần chuyển tất cả @Data thành getters/setters thủ công.

## SAU KHI FIX:
1. ./gradlew clean compileJava
2. Nếu còn lỗi → tiếp tục bổ sung getters/setters cho các class còn lại
