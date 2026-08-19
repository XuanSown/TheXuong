# Review Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Nike-style product reviews (1-5 stars, verified-buyer only) on the product detail page, with 2-reviews-collapsed display, 150-char truncation, admin edit/delete inline, and full VI/EN i18n.

**Architecture:** New `ReviewRepository`/`ReviewService`/`ReviewRestController` behind the existing `Reviews` table (no DB migration). New Vue components `StarRating.vue` (ui) + `ProductReviews.vue` (review section) embedded into `ProductDetail.vue`. Service-level TDD with `@DataJpaTest` (H2 MSSQL mode), frontend TDD with vitest.

**Tech Stack:** Java 21, Spring Boot 3.5.9, Spring Data JPA, SQL Server (prod) / H2 MSSQL mode (test), Vue 3 + TypeScript strict, Tailwind CSS 3, Pinia, vue-i18n, vitest.

## Global Constraints

- **No DB migration.** `Reviews` table stays as-is; keep `UNIQUE (user_id, product_id)`.
- **Roles:** `Users.role` is `CUSTOMER`/`ADMIN`/`BOTH`. Authority = raw role value (no `ROLE_` prefix) → use `hasAnyAuthority('ADMIN','BOTH')` / string checks `"ADMIN".equals(role) || "BOTH".equals(role)`.
- **Auth:** `authentication.getName()` returns email. Anonymous requests on permitAll endpoints get `AnonymousAuthenticationToken`.
- **Backend errors:** throw custom exceptions handled by `GlobalExceptionHandler` returning `ApiResponse.error(message)` JSON. Never return raw 500.
- **i18n parity:** every new key must exist in BOTH `vi.json` and `en.json` with identical `{placeholder}` names — `parity.test.ts` enforces this.
- **Theme:** monochrome black only — text `#1A1C1C`/`#4C4546`, secondary `#5E5F5C`, tracks/borders `#E5E7EB`, filled stars/avatars/badges black. NO yellow/green/red.
- **Numbers:** review comment max 1000 chars (server `@Size(max = 1000)` + client `maxlength`), display truncation at 150 chars, default 2 visible reviews, rating 1-5.
- **Rate limit:** `/api/v1/reviews` already mapped to `USER_REVIEW` plan — do not touch `RateLimitInterceptor`.
- **Backend tests:** `@DataJpaTest` + `@Import(Service.class)` style (see `RecommendationServiceTest`), H2 via `src/test/resources/application.properties`.
- **Commands:** backend `./gradlew test` (repo root); frontend `npm run test` / `npm run type-check` / `npm run lint` / `npm run build` (workdir `frontend`).
- **Commits:** conventional format `feat: ...` per task; commit only files of that task.

---

### Task 1: ReviewRepository + OrderDetailRepository purchase check (TDD)

**Files:**
- Create: `src/main/java/com/example/thexuong/repository/ReviewRepository.java`
- Modify: `src/main/java/com/example/thexuong/repository/OrderDetailRepository.java`
- Test: `src/test/java/com/example/thexuong/repository/ReviewRepositoryTest.java`

**Interfaces:**
- Consumes: `Review`, `User`, `Product`, `Order`, `OrderDetail`, `OrderStatus` entities (existing)
- Produces (used by Task 3):
  - `ReviewRepository.findByProductIdOrderByCreatedAtDesc(Long productId): List<Review>`
  - `ReviewRepository.existsByUserIdAndProductId(Long userId, Long productId): boolean`
  - `ReviewRepository.findByIdAndUserId(Long id, Long userId): Optional<Review>`
  - `ReviewRepository.countByProductId(Long productId): long`
  - `ReviewRepository.countByRating(Long productId): List<Object[]>` (rows: `[rating, count]`)
  - `OrderDetailRepository.existsPurchaseWithStatus(Long productId, Long userId, OrderStatus status): boolean`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/example/thexuong/repository/ReviewRepositoryTest.java`:

```java
package com.example.thexuong.repository;

import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Review;
import com.example.thexuong.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired ReviewRepository reviewRepository;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderDetailRepository orderDetailRepository;

    private User user(String email) {
        return userRepository.save(User.builder()
                .email(email).username(email).fullName("User " + email)
                .role("CUSTOMER").active(true).build());
    }

    private Product product(String name) {
        return productRepository.save(Product.builder()
                .name(name).price(BigDecimal.valueOf(100)).viewCount(0).active(true).build());
    }

    private Review review(User u, Product p, int rating) {
        return reviewRepository.save(Review.builder().user(u).product(p).rating(rating).build());
    }

    private Order order(User u, OrderStatus status) {
        return orderRepository.save(Order.builder().user(u).status(status).build());
    }

    private void detail(Order o, Product p) {
        orderDetailRepository.save(OrderDetail.builder()
                .order(o).productId(p.getId()).productName(p.getName()).quantity(1).build());
    }

    private void pause() {
        try { Thread.sleep(15); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    @Test
    void findByProductIdOrderByCreatedAtDesc_returnsNewestFirst() {
        User u = user("a@test.com");
        Product p = product("P1");
        Review r1 = review(u, p, 5);
        pause();
        Review r2 = review(u, p, 4);

        List<Review> result = reviewRepository.findByProductIdOrderByCreatedAtDesc(p.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(r2.getId());
        assertThat(result.get(1).getId()).isEqualTo(r1.getId());
    }

    @Test
    void existsByUserIdAndProductId_detectsExistingReview() {
        User u = user("b@test.com");
        Product p = product("P2");

        assertThat(reviewRepository.existsByUserIdAndProductId(u.getId(), p.getId())).isFalse();
        review(u, p, 3);
        assertThat(reviewRepository.existsByUserIdAndProductId(u.getId(), p.getId())).isTrue();
    }

    @Test
    void findByIdAndUserId_matchesOnlyOwnedReview() {
        User u1 = user("c@test.com");
        User u2 = user("d@test.com");
        Product p = product("P3");
        Review own = review(u1, p, 5);
        review(u2, p, 1);

        assertThat(reviewRepository.findByIdAndUserId(own.getId(), u1.getId())).isPresent();
        assertThat(reviewRepository.findByIdAndUserId(own.getId(), u2.getId())).isEmpty();
    }

    @Test
    void countByProductId_countsOnlyThatProduct() {
        User u = user("e@test.com");
        Product p1 = product("P4");
        Product p2 = product("P5");
        review(u, p1, 5);
        review(u, p1, 4);
        review(u, p2, 3);

        assertThat(reviewRepository.countByProductId(p1.getId())).isEqualTo(2);
        assertThat(reviewRepository.countByProductId(p2.getId())).isEqualTo(1);
    }

    @Test
    void countByRating_groupsByRating() {
        User u = user("f@test.com");
        Product p = product("P6");
        review(u, p, 5);
        review(u, p, 5);
        review(u, p, 2);

        List<Object[]> rows = reviewRepository.countByRating(p.getId());

        assertThat(rows).hasSize(2);
        for (Object[] row : rows) {
            if (((Number) row[0]).intValue() == 5) assertThat(((Number) row[1]).longValue()).isEqualTo(2);
            if (((Number) row[0]).intValue() == 2) assertThat(((Number) row[1]).longValue()).isEqualTo(1);
        }
    }

    @Test
    void existsPurchaseWithStatus_trueOnlyForCompletedOrders() {
        User u = user("g@test.com");
        Product p = product("P7");
        detail(order(u, OrderStatus.COMPLETED), p);
        detail(order(u, OrderStatus.PENDING), p);

        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId(), OrderStatus.COMPLETED)).isTrue();
        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId(), OrderStatus.PENDING)).isTrue();
        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId(), OrderStatus.CANCELLED)).isFalse();
        assertThat(orderDetailRepository.existsPurchaseWithStatus(p.getId(), u.getId() + 999L, OrderStatus.COMPLETED)).isFalse();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run (repo root): `./gradlew test --tests "*ReviewRepositoryTest*"`
Expected: FAIL — compile error: `ReviewRepository` class not found.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/com/example/thexuong/repository/ReviewRepository.java`:

```java
package com.example.thexuong.repository;

import com.example.thexuong.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<Review> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> countByRating(@Param("productId") Long productId);
}
```

Modify `src/main/java/com/example/thexuong/repository/OrderDetailRepository.java` — add imports and method at the end of the interface:

```java
import com.example.thexuong.entity.OrderStatus;
import org.springframework.data.repository.query.Param;
```

```java
    // 3. Kiểm tra user đã mua sản phẩm với trạng thái đơn cụ thể (dùng cho Review).
    @Query("""
            SELECT COUNT(d) > 0 FROM OrderDetail d
            WHERE d.productId = :productId
              AND d.order.user.id = :userId
              AND d.order.status = :status
            """)
    boolean existsPurchaseWithStatus(@Param("productId") Long productId,
                                     @Param("userId") Long userId,
                                     @Param("status") OrderStatus status);
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "*ReviewRepositoryTest*"`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/thexuong/repository/ReviewRepository.java src/main/java/com/example/thexuong/repository/OrderDetailRepository.java src/test/java/com/example/thexuong/repository/ReviewRepositoryTest.java
git commit -m "feat: ReviewRepository + OrderDetailRepository purchase check"
```

---

### Task 2: Review exceptions + DTOs

**Files:**
- Create: `src/main/java/com/example/thexuong/exception/ReviewNotFoundException.java`
- Create: `src/main/java/com/example/thexuong/exception/ReviewNotAllowedException.java`
- Create: `src/main/java/com/example/thexuong/exception/ReviewAlreadyExistsException.java`
- Create: `src/main/java/com/example/thexuong/dto/ReviewDto.java`
- Create: `src/main/java/com/example/thexuong/dto/ReviewSummaryDto.java`
- Create: `src/main/java/com/example/thexuong/dto/ReviewListResponse.java`
- Create: `src/main/java/com/example/thexuong/dto/ReviewRequest.java`

**Interfaces:**
- Produces (used by Tasks 3, 4):
  - `ReviewNotFoundException(String message)` → 404
  - `ReviewNotAllowedException(String message)` → 403
  - `ReviewAlreadyExistsException(String message)` → 409
  - `ReviewDto` fields: `Long id, Integer rating, String comment, LocalDateTime createdAt, String authorName, boolean verifiedBuyer, boolean isMine, boolean canModerate` (Lombok `@Getter @Builder @NoArgsConstructor @AllArgsConstructor`)
  - `ReviewSummaryDto` fields: `double averageRating, long totalCount, Map<Integer, Long> distribution`
  - `ReviewListResponse` fields: `ReviewSummaryDto summary, List<ReviewDto> reviews`
  - `ReviewRequest` fields: `Long productId` (required on POST only), `@NotNull @Min(1) @Max(5) Integer rating`, `@Size(max=1000) String comment`

- [ ] **Step 1: Create the 3 exceptions** (same style as `UserNotFoundException`)

`ReviewNotFoundException.java`:

```java
package com.example.thexuong.exception;

/**
 * Ném ra khi không tìm thấy Review (hoặc Product khi tạo review).
 */
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
```

`ReviewNotAllowedException.java`:

```java
package com.example.thexuong.exception;

/**
 * 403 — User không đủ điều kiện: chưa mua sản phẩm, không phải chủ review, không phải admin.
 */
public class ReviewNotAllowedException extends RuntimeException {
    public ReviewNotAllowedException(String message) {
        super(message);
    }
}
```

`ReviewAlreadyExistsException.java`:

```java
package com.example.thexuong.exception;

/**
 * 409 — User đã review sản phẩm này rồi (UNIQUE user_id + product_id).
 */
public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String message) {
        super(message);
    }
}
```

- [ ] **Step 2: Create the 4 DTOs**

`src/main/java/com/example/thexuong/dto/ReviewDto.java`:

```java
package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String authorName;
    private boolean verifiedBuyer;
    private boolean isMine;
    private boolean canModerate;
}
```

`src/main/java/com/example/thexuong/dto/ReviewSummaryDto.java`:

```java
package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    private double averageRating;
    private long totalCount;
    private Map<Integer, Long> distribution; // key 5 -> 1
}
```

`src/main/java/com/example/thexuong/dto/ReviewListResponse.java`:

```java
package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListResponse {
    private ReviewSummaryDto summary;
    private List<ReviewDto> reviews;
}
```

`src/main/java/com/example/thexuong/dto/ReviewRequest.java`:

```java
package com.example.thexuong.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {

    private Long productId; // chỉ bắt buộc khi POST (tạo mới)

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải từ 1 đến 5")
    @Max(value = 5, message = "Rating phải từ 1 đến 5")
    private Integer rating;

    @Size(max = 1000, message = "Nội dung đánh giá tối đa 1000 ký tự")
    private String comment;
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/thexuong/exception/ReviewNotFoundException.java src/main/java/com/example/thexuong/exception/ReviewNotAllowedException.java src/main/java/com/example/thexuong/exception/ReviewAlreadyExistsException.java src/main/java/com/example/thexuong/dto/ReviewDto.java src/main/java/com/example/thexuong/dto/ReviewSummaryDto.java src/main/java/com/example/thexuong/dto/ReviewListResponse.java src/main/java/com/example/thexuong/dto/ReviewRequest.java
git commit -m "feat: review exceptions and DTOs"
```

---

### Task 3: ReviewService (TDD)

**Files:**
- Create: `src/main/java/com/example/thexuong/service/ReviewService.java`
- Test: `src/test/java/com/example/thexuong/service/ReviewServiceTest.java`

**Interfaces:**
- Consumes: Task 1 repositories, Task 2 DTOs/exceptions, `UserNotFoundException(String)`, `ProductRepository.findById(Long)` (note: `@SQLRestriction("active = 1")` means inactive products return empty → 404)
- Produces (used by Task 4):
  - `ReviewListResponse getProductReviews(Long productId, String viewerEmail)` — viewerEmail nullable (anonymous)
  - `ReviewDto createReview(String email, ReviewRequest request)`
  - `ReviewDto updateReview(String email, Long reviewId, ReviewRequest request)`
  - `void deleteReview(String email, Long reviewId)`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/example/thexuong/service/ReviewServiceTest.java`:

```java
package com.example.thexuong.service;

import com.example.thexuong.dto.ReviewDto;
import com.example.thexuong.dto.ReviewListResponse;
import com.example.thexuong.dto.ReviewRequest;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Review;
import com.example.thexuong.entity.User;
import com.example.thexuong.exception.ReviewAlreadyExistsException;
import com.example.thexuong.exception.ReviewNotAllowedException;
import com.example.thexuong.exception.ReviewNotFoundException;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ReviewRepository;
import com.example.thexuong.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ReviewService.class)
class ReviewServiceTest {

    @Autowired ReviewService reviewService;
    @Autowired ReviewRepository reviewRepository;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderDetailRepository orderDetailRepository;

    private User customer(String email) {
        return userRepository.save(User.builder()
                .email(email).username(email).fullName("Khách " + email)
                .role("CUSTOMER").active(true).build());
    }

    private User admin(String email) {
        return userRepository.save(User.builder()
                .email(email).username(email).fullName("Admin")
                .role("ADMIN").active(true).build());
    }

    private Product product(String name) {
        return productRepository.save(Product.builder()
                .name(name).price(BigDecimal.valueOf(100)).viewCount(0).active(true).build());
    }

    private Order order(User u, OrderStatus status) {
        return orderRepository.save(Order.builder().user(u).status(status).build());
    }

    private void purchased(User u, Product p, OrderStatus status) {
        Order o = order(u, status);
        orderDetailRepository.save(OrderDetail.builder()
                .order(o).productId(p.getId()).productName(p.getName()).quantity(1).build());
    }

    private Review existing(User u, Product p, int rating, String comment) {
        return reviewRepository.save(Review.builder().user(u).product(p).rating(rating).comment(comment).build());
    }

    private ReviewRequest request(Long productId, int rating, String comment) {
        ReviewRequest r = new ReviewRequest();
        r.setProductId(productId);
        r.setRating(rating);
        r.setComment(comment);
        return r;
    }

    private void pause() {
        try { Thread.sleep(15); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    @Test
    void createReview_notPurchased_throwsNotAllowed() {
        User u = customer("u1@test.com");
        Product p = product("P1");

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(p.getId(), 5, "Tốt")))
                .isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void createReview_purchasedCompleted_savesReview() {
        User u = customer("u2@test.com");
        Product p = product("P2");
        purchased(u, p, OrderStatus.COMPLETED);

        ReviewDto dto = reviewService.createReview(u.getEmail(), request(p.getId(), 4, "  Ổn áp  "));

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getRating()).isEqualTo(4);
        assertThat(dto.getComment()).isEqualTo("Ổn áp"); // trimmed
        assertThat(dto.getAuthorName()).isEqualTo("Khách u2@test.com");
        assertThat(dto.isMine()).isTrue();
        assertThat(dto.isVerifiedBuyer()).isTrue();
        assertThat(dto.isCanModerate()).isFalse();
    }

    @Test
    void createReview_pendingOrderOnly_throwsNotAllowed() {
        User u = customer("u3@test.com");
        Product p = product("P3");
        purchased(u, p, OrderStatus.PENDING);

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(p.getId(), 5, null)))
                .isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void createReview_duplicate_throwsAlreadyExists() {
        User u = customer("u4@test.com");
        Product p = product("P4");
        purchased(u, p, OrderStatus.COMPLETED);
        reviewService.createReview(u.getEmail(), request(p.getId(), 5, "Lần 1"));

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(p.getId(), 3, "Lần 2")))
                .isInstanceOf(ReviewAlreadyExistsException.class);
    }

    @Test
    void createReview_productNotFound_throwsNotFound() {
        User u = customer("u5@test.com");

        assertThatThrownBy(() -> reviewService.createReview(u.getEmail(), request(99999L, 5, null)))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void updateReview_owner_succeeds() {
        User u = customer("u6@test.com");
        Product p = product("P6");
        purchased(u, p, OrderStatus.COMPLETED);
        Review saved = existing(u, p, 5, "Cũ");

        ReviewDto dto = reviewService.updateReview(u.getEmail(), saved.getId(), request(p.getId(), 2, "Mới"));

        assertThat(dto.getRating()).isEqualTo(2);
        assertThat(dto.getComment()).isEqualTo("Mới");
    }

    @Test
    void updateReview_otherCustomer_throwsNotAllowed() {
        User owner = customer("u7@test.com");
        User other = customer("u8@test.com");
        Product p = product("P7");
        Review saved = existing(owner, p, 5, "Cũ");

        assertThatThrownBy(() -> reviewService.updateReview(other.getEmail(), saved.getId(), request(p.getId(), 1, "Hack")))
                .isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void updateReview_admin_updatesAnyReview() {
        User owner = customer("u9@test.com");
        User boss = admin("boss@test.com");
        Product p = product("P9");
        Review saved = existing(owner, p, 5, "Cũ");

        ReviewDto dto = reviewService.updateReview(boss.getEmail(), saved.getId(), request(p.getId(), 1, "Admin sửa"));

        assertThat(dto.getRating()).isEqualTo(1);
        assertThat(dto.isCanModerate()).isTrue();
        assertThat(dto.isMine()).isFalse();
    }

    @Test
    void updateReview_notFound_throwsNotFound() {
        User u = customer("u10@test.com");

        assertThatThrownBy(() -> reviewService.updateReview(u.getEmail(), 99999L, request(1L, 5, null)))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void deleteReview_customer_throwsNotAllowed() {
        User u = customer("u11@test.com");
        Product p = product("P11");
        Review saved = existing(u, p, 5, "Tôi xóa");

        assertThatThrownBy(() -> reviewService.deleteReview(u.getEmail(), saved.getId()))
                .isInstanceOf(ReviewNotAllowedException.class);
        assertThat(reviewRepository.countByProductId(p.getId())).isEqualTo(1);
    }

    @Test
    void deleteReview_admin_deletes() {
        User u = customer("u12@test.com");
        User boss = admin("boss2@test.com");
        Product p = product("P12");
        Review saved = existing(u, p, 5, "Xóa đi");

        reviewService.deleteReview(boss.getEmail(), saved.getId());

        assertThat(reviewRepository.countByProductId(p.getId())).isZero();
    }

    @Test
    void deleteReview_notFound_throwsNotFound() {
        User boss = admin("boss3@test.com");

        assertThatThrownBy(() -> reviewService.deleteReview(boss.getEmail(), 99999L))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void getProductReviews_emptyProduct_returnsZeroSummary() {
        Product p = product("P13");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getSummary().getTotalCount()).isZero();
        assertThat(res.getSummary().getAverageRating()).isZero();
        assertThat(res.getReviews()).isEmpty();
    }

    @Test
    void getProductReviews_computesSummaryAndDistribution() {
        User u1 = customer("u14@test.com");
        User u2 = customer("u15@test.com");
        User u3 = customer("u16@test.com");
        Product p = product("P14");
        existing(u1, p, 5, "R1");
        existing(u2, p, 4, "R2");
        existing(u3, p, 3, "R3");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getSummary().getTotalCount()).isEqualTo(3);
        assertThat(res.getSummary().getAverageRating()).isEqualTo(4.0);
        assertThat(res.getSummary().getDistribution())
                .containsEntry(5, 1L).containsEntry(4, 1L).containsEntry(3, 1L)
                .containsEntry(2, 0L).containsEntry(1, 0L);
        assertThat(res.getReviews()).hasSize(3);
    }

    @Test
    void getProductReviews_roundsAverageToOneDecimal() {
        User u1 = customer("u17@test.com");
        User u2 = customer("u18@test.com");
        Product p = product("P15");
        existing(u1, p, 5, "R1");
        existing(u2, p, 4, "R2");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getSummary().getAverageRating()).isEqualTo(4.5);
    }

    @Test
    void getProductReviews_returnsNewestFirst() {
        User u1 = customer("u19@test.com");
        User u2 = customer("u20@test.com");
        Product p = product("P16");
        Review first = existing(u1, p, 5, "Cũ hơn");
        pause();
        Review second = existing(u2, p, 2, "Mới hơn");

        ReviewListResponse res = reviewService.getProductReviews(p.getId(), null);

        assertThat(res.getReviews().get(0).getId()).isEqualTo(second.getId());
        assertThat(res.getReviews().get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void getProductReviews_setsViewerFlags() {
        User owner = customer("u21@test.com");
        User other = customer("u22@test.com");
        User boss = admin("boss4@test.com");
        Product p = product("P17");
        Review mine = existing(owner, p, 5, "Của tôi");
        existing(other, p, 3, "Của người khác");

        ReviewListResponse asOwner = reviewService.getProductReviews(p.getId(), owner.getEmail());
        assertThat(asOwner.getReviews().stream().filter(r -> r.getId().equals(mine.getId())).findFirst().get().isMine()).isTrue();
        assertThat(asOwner.getReviews().stream().allMatch(r -> !r.isCanModerate())).isTrue();

        ReviewListResponse asAdmin = reviewService.getProductReviews(p.getId(), boss.getEmail());
        assertThat(asAdmin.getReviews().stream().allMatch(r -> r.isCanModerate())).isTrue();

        ReviewListResponse asAnonymous = reviewService.getProductReviews(p.getId(), null);
        assertThat(asAnonymous.getReviews().stream().allMatch(r -> !r.isMine() && !r.isCanModerate())).isTrue();
    }

    @Test
    void getProductReviews_productNotFound_throwsNotFound() {
        assertThatThrownBy(() -> reviewService.getProductReviews(99999L, null))
                .isInstanceOf(ReviewNotFoundException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "*ReviewServiceTest*"`
Expected: FAIL — compile error: `ReviewService` not found.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/com/example/thexuong/service/ReviewService.java`:

```java
package com.example.thexuong.service;

import com.example.thexuong.dto.ReviewDto;
import com.example.thexuong.dto.ReviewListResponse;
import com.example.thexuong.dto.ReviewRequest;
import com.example.thexuong.dto.ReviewSummaryDto;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Review;
import com.example.thexuong.entity.User;
import com.example.thexuong.exception.ReviewAlreadyExistsException;
import com.example.thexuong.exception.ReviewNotAllowedException;
import com.example.thexuong.exception.ReviewNotFoundException;
import com.example.thexuong.exception.UserNotFoundException;
import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ReviewRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional(readOnly = true)
    public ReviewListResponse getProductReviews(Long productId, String viewerEmail) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy sản phẩm: " + productId));

        User viewer = viewerEmail == null ? null : userRepository.findByEmail(viewerEmail).orElse(null);
        boolean viewerIsAdmin = viewer != null && isAdmin(viewer);

        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int stars = 5; stars >= 1; stars--) distribution.put(stars, 0L);
        long total = 0;
        double weighted = 0;
        for (Object[] row : reviewRepository.countByRating(productId)) {
            int stars = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            distribution.put(stars, count);
            total += count;
            weighted += (double) stars * count;
        }
        double average = total == 0 ? 0 : Math.round((weighted / total) * 10.0) / 10.0;

        ReviewSummaryDto summary = ReviewSummaryDto.builder()
                .averageRating(average)
                .totalCount(total)
                .distribution(distribution)
                .build();

        List<ReviewDto> dtos = reviews.stream()
                .map(r -> toDto(r, viewerEmail, viewerIsAdmin))
                .toList();

        return ReviewListResponse.builder().summary(summary).reviews(dtos).build();
    }

    @Transactional
    public ReviewDto createReview(String email, ReviewRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Thiếu productId.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy sản phẩm: " + request.getProductId()));

        boolean purchased = orderDetailRepository.existsPurchaseWithStatus(
                request.getProductId(), user.getId(), OrderStatus.COMPLETED);
        if (!purchased) {
            throw new ReviewNotAllowedException("Bạn cần mua sản phẩm để đánh giá.");
        }
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), request.getProductId())) {
            throw new ReviewAlreadyExistsException("Bạn đã đánh giá sản phẩm này rồi.");
        }

        Review review = reviewRepository.save(Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(normalizeComment(request.getComment()))
                .build());
        return toDto(review, email, isAdmin(user));
    }

    @Transactional
    public ReviewDto updateReview(String email, Long reviewId, ReviewRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy đánh giá: " + reviewId));

        boolean owner = review.getUser().getId().equals(user.getId());
        boolean admin = isAdmin(user);
        if (!owner && !admin) {
            throw new ReviewNotAllowedException("Bạn chỉ có thể sửa đánh giá của chính mình.");
        }

        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));
        Review saved = reviewRepository.save(review);
        return toDto(saved, email, admin);
    }

    @Transactional
    public void deleteReview(String email, Long reviewId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Không tìm thấy đánh giá: " + reviewId));
        if (!isAdmin(user)) {
            throw new ReviewNotAllowedException("Bạn không có quyền xóa đánh giá.");
        }
        reviewRepository.delete(review);
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole()) || "BOTH".equals(user.getRole());
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) return null;
        return comment.trim();
    }

    private ReviewDto toDto(Review review, String viewerEmail, boolean viewerIsAdmin) {
        User author = review.getUser();
        String authorName = (author.getFullName() != null && !author.getFullName().isBlank())
                ? author.getFullName() : author.getUsername();
        return ReviewDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .authorName(authorName)
                .verifiedBuyer(true)
                .isMine(viewerEmail != null && viewerEmail.equalsIgnoreCase(author.getEmail()))
                .canModerate(viewerIsAdmin)
                .build();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "*ReviewServiceTest*"`
Expected: PASS (18 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/thexuong/service/ReviewService.java src/test/java/com/example/thexuong/service/ReviewServiceTest.java
git commit -m "feat: ReviewService with verified-purchase check and admin moderation"
```

---

### Task 4: ReviewRestController + exception handlers + security permit

**Files:**
- Create: `src/main/java/com/example/thexuong/controller/ReviewRestController.java`
- Modify: `src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java`
- Modify: `src/main/java/com/example/thexuong/config/SecurityConfig.java`

**Interfaces:**
- Consumes: Task 2 exceptions, Task 3 `ReviewService`
- Produces (used by Task 7 frontend service):
  - `GET /api/v1/reviews/product/{productId}` → `200 ReviewListResponse` (public)
  - `POST /api/v1/reviews` body `{productId, rating, comment}` → `201 ReviewDto` (authenticated)
  - `PUT /api/v1/reviews/{id}` body `{rating, comment}` → `200 ReviewDto` (authenticated; owner or ADMIN/BOTH)
  - `DELETE /api/v1/reviews/{id}` → `200 ApiResponse<Void>` (authenticated; ADMIN/BOTH)
  - Errors: `404` ReviewNotFound, `403` ReviewNotAllowed, `409` ReviewAlreadyExists, `400` validation/IllegalArgument

- [ ] **Step 1: Create the controller**

Create `src/main/java/com/example/thexuong/controller/ReviewRestController.java`:

```java
package com.example.thexuong.controller;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.ReviewDto;
import com.example.thexuong.dto.ReviewListResponse;
import com.example.thexuong.dto.ReviewRequest;
import com.example.thexuong.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewRestController {

    private final ReviewService reviewService;

    /**
     * GET /api/v1/reviews/product/{productId} — Public.
     * Nếu request có đăng nhập thì trả kèm cờ isMine / canModerate theo user đang xem.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ReviewListResponse> getProductReviews(
            @PathVariable Long productId,
            Authentication authentication) {
        String viewerEmail = (authentication == null || authentication instanceof AnonymousAuthenticationToken)
                ? null : authentication.getName();
        return ResponseEntity.ok(reviewService.getProductReviews(productId, viewerEmail));
    }

    /**
     * POST /api/v1/reviews — Authenticated. Body: { productId, rating, comment }
     */
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(Authentication authentication,
                                                  @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(authentication.getName(), request));
    }

    /**
     * PUT /api/v1/reviews/{id} — Chủ review hoặc ADMIN/BOTH. Body: { rating, comment }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(authentication.getName(), id, request));
    }

    /**
     * DELETE /api/v1/reviews/{id} — Chỉ ADMIN/BOTH.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(Authentication authentication,
                                                          @PathVariable Long id) {
        reviewService.deleteReview(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đánh giá thành công."));
    }
}
```

- [ ] **Step 2: Register the 3 exception handlers**

Modify `src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java` — insert BEFORE the `handleGeneral` (500) handler:

```java
  /**
   * 404 — Review (hoặc Product khi tạo review) không tồn tại.
   */
  @ExceptionHandler(ReviewNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleReviewNotFound(ReviewNotFoundException ex) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 403 — Review: chưa mua sản phẩm / không phải chủ review / không phải admin.
   */
  @ExceptionHandler(ReviewNotAllowedException.class)
  public ResponseEntity<ApiResponse<Void>> handleReviewNotAllowed(ReviewNotAllowedException ex) {
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 409 — User đã review sản phẩm này rồi.
   */
  @ExceptionHandler(ReviewAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<Void>> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ApiResponse.error(ex.getMessage()));
  }
```

- [ ] **Step 3: Permit public GET in SecurityConfig**

Modify `src/main/java/com/example/thexuong/config/SecurityConfig.java`:
- Add import: `import org.springframework.http.HttpMethod;`
- In `.authorizeHttpRequests(...)`, insert BEFORE `.requestMatchers("/api/v1/addresses", ...)`:

```java
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/product/**").permitAll()
```

(Security check: POST/PUT/DELETE `/api/v1/reviews` fall through to `.anyRequest().authenticated()` — no extra config needed.)

- [ ] **Step 4: Verify full backend test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL — all tests including `ReviewRepositoryTest` + `ReviewServiceTest` pass. If `RecommendationServiceTest` regresses, fix before continuing.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/ReviewRestController.java src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java src/main/java/com/example/thexuong/config/SecurityConfig.java
git commit -m "feat: ReviewRestController + exception handlers + public GET reviews"
```

---

### Task 5: i18n review keys (VI + EN)

**Files:**
- Modify: `frontend/src/i18n/locales/vi.json`
- Modify: `frontend/src/i18n/locales/en.json`

**Interfaces:**
- Produces (used by Tasks 6, 8, 9): `review.*` i18n namespace with keys: `title, count, noReviews, starLabel, star, verifiedBuyer, seeAll, showLess, seeMore, yourReview, writeTitle, ratingLabel, commentPlaceholder, charactersLeft, submit, edit, delete, editTitle, deleteTitle, deleteConfirm, update, cancel, needToLogin, needToPurchase, alreadyReviewed, submitted, updated, deleted, loadFailed, timeJustNow, timeMinutesAgo, timeHoursAgo, timeDaysAgo, timeMonthsAgo, timeYearsAgo`

- [ ] **Step 1: Add `review` namespace to vi.json**

In `frontend/src/i18n/locales/vi.json`, insert after the `"product": { ... }` block (after line 210, before `"toast"`):

```json
  "review": {
    "title": "ĐÁNH GIÁ SẢN PHẨM",
    "count": "{count} đánh giá",
    "noReviews": "Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá!",
    "starLabel": "{stars} sao",
    "star": "sao",
    "verifiedBuyer": "Đã mua hàng",
    "seeAll": "Xem tất cả {count} đánh giá",
    "showLess": "Thu gọn",
    "seeMore": "Xem thêm",
    "yourReview": "Đánh giá của bạn",
    "writeTitle": "Viết đánh giá của bạn",
    "ratingLabel": "Chọn mức đánh giá:",
    "commentPlaceholder": "Chia sẻ trải nghiệm của bạn về sản phẩm này...",
    "charactersLeft": "Còn {count} ký tự",
    "submit": "GỬI ĐÁNH GIÁ",
    "edit": "Sửa",
    "delete": "Xóa",
    "editTitle": "Sửa đánh giá",
    "deleteTitle": "Xóa đánh giá",
    "deleteConfirm": "Bạn có chắc muốn xóa đánh giá này? Hành động này không thể hoàn tác.",
    "update": "CẬP NHẬT",
    "cancel": "HỦY",
    "needToLogin": "Bạn cần đăng nhập để viết đánh giá.",
    "needToPurchase": "Bạn cần mua sản phẩm này để viết đánh giá.",
    "alreadyReviewed": "Bạn đã đánh giá sản phẩm này rồi.",
    "submitted": "Đã gửi đánh giá của bạn!",
    "updated": "Đã cập nhật đánh giá!",
    "deleted": "Đã xóa đánh giá!",
    "loadFailed": "Không thể tải đánh giá. Vui lòng thử lại.",
    "timeJustNow": "Vừa xong",
    "timeMinutesAgo": "{n} phút trước",
    "timeHoursAgo": "{n} giờ trước",
    "timeDaysAgo": "{n} ngày trước",
    "timeMonthsAgo": "{n} tháng trước",
    "timeYearsAgo": "{n} năm trước"
  },
```

- [ ] **Step 2: Add `review` namespace to en.json**

In `frontend/src/i18n/locales/en.json`, insert at the same position (after `"product"`, before `"toast"`):

```json
  "review": {
    "title": "PRODUCT REVIEWS",
    "count": "{count} reviews",
    "noReviews": "No reviews yet. Be the first to review!",
    "starLabel": "{stars} stars",
    "star": "stars",
    "verifiedBuyer": "Verified buyer",
    "seeAll": "See all {count} reviews",
    "showLess": "Show less",
    "seeMore": "See more",
    "yourReview": "Your review",
    "writeTitle": "Write your review",
    "ratingLabel": "Select rating:",
    "commentPlaceholder": "Share your experience with this product...",
    "charactersLeft": "{count} characters left",
    "submit": "SUBMIT REVIEW",
    "edit": "Edit",
    "delete": "Delete",
    "editTitle": "Edit review",
    "deleteTitle": "Delete review",
    "deleteConfirm": "Are you sure you want to delete this review? This action cannot be undone.",
    "update": "UPDATE",
    "cancel": "CANCEL",
    "needToLogin": "You need to log in to write a review.",
    "needToPurchase": "You need to purchase this product to write a review.",
    "alreadyReviewed": "You have already reviewed this product.",
    "submitted": "Your review has been submitted!",
    "updated": "Review updated!",
    "deleted": "Review deleted!",
    "loadFailed": "Could not load reviews. Please try again.",
    "timeJustNow": "Just now",
    "timeMinutesAgo": "{n} minutes ago",
    "timeHoursAgo": "{n} hours ago",
    "timeDaysAgo": "{n} days ago",
    "timeMonthsAgo": "{n} months ago",
    "timeYearsAgo": "{n} years ago"
  },
```

- [ ] **Step 3: Run i18n parity + full frontend tests**

Run (workdir `frontend`): `npm run test`
Expected: PASS — `parity.test.ts` (same key set, non-empty values, matching placeholders) and all existing tests green.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/i18n/locales/vi.json frontend/src/i18n/locales/en.json
git commit -m "feat: i18n review namespace (VI/EN)"
```

---

### Task 6: truncateText + formatRelativeTime utils (TDD)

**Files:**
- Modify: `frontend/src/utils/formatters.ts`
- Test: `frontend/src/utils/__tests__/formatters.test.ts` (append new describe blocks)

**Interfaces:**
- Consumes: `review.*` i18n keys (Task 5)
- Produces (used by Task 9):
  - `truncateText(text: string | null | undefined, max = 150): string` — empty for null; unchanged if ≤ max; else `slice(0, max).trimEnd() + '…'`
  - `formatRelativeTime(value: string | Date, now: Date = new Date()): string` — localized: `<1min` "Vừa xong"/"Just now"; minutes/hours/days/months/years with `{n}`

- [ ] **Step 1: Write the failing tests**

Append to `frontend/src/utils/__tests__/formatters.test.ts` (update the import line to `import { formatCurrency, formatDate, truncateText, formatRelativeTime } from '@/utils/formatters'`):

```ts
describe('truncateText', () => {
  it('returns empty string for null/undefined/empty', () => {
    expect(truncateText(null)).toBe('')
    expect(truncateText(undefined)).toBe('')
    expect(truncateText('')).toBe('')
  })

  it('returns text unchanged when at or under limit', () => {
    expect(truncateText('abc', 150)).toBe('abc')
    expect(truncateText('x'.repeat(150))).toBe('x'.repeat(150))
  })

  it('truncates to max chars plus ellipsis when over limit', () => {
    expect(truncateText('x'.repeat(151))).toBe('x'.repeat(150) + '…')
    expect(truncateText('hello world', 5)).toBe('hello…')
  })
})

describe('formatRelativeTime', () => {
  const now = new Date('2026-08-20T12:00:00')

  it('returns "Vừa xong" for less than a minute in vi', () => {
    i18n.global.locale.value = 'vi'
    expect(formatRelativeTime(new Date('2026-08-20T11:59:30'), now)).toBe('Vừa xong')
  })

  it('returns "Just now" in en', () => {
    i18n.global.locale.value = 'en'
    expect(formatRelativeTime(new Date('2026-08-20T11:59:30'), now)).toBe('Just now')
  })

  it('formats minutes/hours/days/months/years in vi', () => {
    i18n.global.locale.value = 'vi'
    expect(formatRelativeTime(new Date('2026-08-20T11:55:00'), now)).toBe('5 phút trước')
    expect(formatRelativeTime(new Date('2026-08-20T10:00:00'), now)).toBe('2 giờ trước')
    expect(formatRelativeTime(new Date('2026-08-18T12:00:00'), now)).toBe('2 ngày trước')
    expect(formatRelativeTime(new Date('2026-07-20T12:00:00'), now)).toBe('1 tháng trước')
    expect(formatRelativeTime(new Date('2025-08-20T12:00:00'), now)).toBe('1 năm trước')
  })

  it('formats minutes/hours/days in en', () => {
    i18n.global.locale.value = 'en'
    expect(formatRelativeTime(new Date('2026-08-20T11:55:00'), now)).toBe('5 minutes ago')
    expect(formatRelativeTime(new Date('2026-08-20T10:00:00'), now)).toBe('2 hours ago')
    expect(formatRelativeTime(new Date('2026-08-18T12:00:00'), now)).toBe('2 days ago')
  })

  it('accepts string input', () => {
    i18n.global.locale.value = 'vi'
    expect(formatRelativeTime('2026-08-20T11:55:00', now)).toBe('5 phút trước')
  })

  it('returns empty string for invalid input', () => {
    expect(formatRelativeTime('not-a-date', now)).toBe('')
  })
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run (workdir `frontend`): `npm run test -- formatters`
Expected: FAIL — `truncateText`/`formatRelativeTime` are not exported.

- [ ] **Step 3: Implement in formatters.ts**

Append to `frontend/src/utils/formatters.ts`:

```ts
// Cắt nội dung review ở mức max ký tự, thêm dấu "…" nếu bị cắt (plan review).
export const truncateText = (text: string | null | undefined, max = 150): string => {
  if (!text) return ''
  if (text.length <= max) return text
  return text.slice(0, max).trimEnd() + '…'
}

// Thời gian tương đối theo locale hiện tại ("3 ngày trước" / "3 days ago").
export const formatRelativeTime = (value: string | Date, now: Date = new Date()): string => {
  const date = typeof value === 'string' ? new Date(value) : value
  if (!value || isNaN(date.getTime())) return ''
  const t = i18n.global.t
  const diffMinutes = Math.floor((now.getTime() - date.getTime()) / 60000)
  if (diffMinutes < 1) return t('review.timeJustNow')
  if (diffMinutes < 60) return t('review.timeMinutesAgo', { n: diffMinutes })
  const hours = Math.floor(diffMinutes / 60)
  if (hours < 24) return t('review.timeHoursAgo', { n: hours })
  const days = Math.floor(hours / 24)
  if (days < 30) return t('review.timeDaysAgo', { n: days })
  const months = Math.floor(days / 30)
  if (months < 12) return t('review.timeMonthsAgo', { n: months })
  return t('review.timeYearsAgo', { n: Math.floor(months / 12) })
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run (workdir `frontend`): `npm run test -- formatters`
Expected: PASS — all `formatCurrency`/`formatDate`/`truncateText`/`formatRelativeTime` tests green.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/utils/formatters.ts frontend/src/utils/__tests__/formatters.test.ts
git commit -m "feat: truncateText + formatRelativeTime utils"
```

---

### Task 7: Review types + review.service.ts + 403 interceptor fix

**Files:**
- Create: `frontend/src/types/review.types.ts`
- Modify: `frontend/src/types/index.ts`
- Create: `frontend/src/services/review.service.ts`
- Modify: `frontend/src/services/http.ts`

**Interfaces:**
- Consumes: backend API shapes from Task 4
- Produces (used by Tasks 8, 9):
  - Types: `Review { id, rating, comment: string|null, createdAt, authorName, verifiedBuyer, isMine, canModerate }`, `ReviewSummary { averageRating, totalCount, distribution: Record<number, number> }`, `ReviewListResponse { summary, reviews }`
  - `reviewService.getProductReviews(productId): Promise<ReviewListResponse>`
  - `reviewService.createReview(productId, {rating, comment?}): Promise<Review>`
  - `reviewService.updateReview(id, {rating, comment?}): Promise<Review>`
  - `reviewService.deleteReview(id): Promise<void>`
  - `http.ts`: 403 redirect to `/` skipped for URLs starting with `/reviews`

- [ ] **Step 1: Create review types**

Create `frontend/src/types/review.types.ts`:

```ts
export interface Review {
  id: number
  rating: number
  comment: string | null
  createdAt: string
  authorName: string
  verifiedBuyer: boolean
  isMine: boolean
  canModerate: boolean
}

export interface ReviewSummary {
  averageRating: number
  totalCount: number
  distribution: Record<number, number>
}

export interface ReviewListResponse {
  summary: ReviewSummary
  reviews: Review[]
}
```

Modify `frontend/src/types/index.ts` — add export line after the `order.types` export:

```ts
export type { Review, ReviewSummary, ReviewListResponse } from './review.types'
```

- [ ] **Step 2: Create review service**

Create `frontend/src/services/review.service.ts`:

```ts
import http from './http'
import type { Review, ReviewListResponse } from '@/types/review.types'

export const reviewService = {
  async getProductReviews(productId: number): Promise<ReviewListResponse> {
    return (await http.get(`/reviews/product/${productId}`)).data
  },

  async createReview(productId: number, payload: { rating: number; comment?: string }): Promise<Review> {
    return (await http.post('/reviews', { productId, ...payload })).data
  },

  async updateReview(id: number, payload: { rating: number; comment?: string }): Promise<Review> {
    return (await http.put(`/reviews/${id}`, payload)).data
  },

  async deleteReview(id: number): Promise<void> {
    await http.delete(`/reviews/${id}`)
  }
}
```

- [ ] **Step 3: Fix 403 interceptor for review endpoints**

Modify `frontend/src/services/http.ts` — replace:

```ts
    if (error.response?.status === 403) window.location.href = '/'
```

with:

```ts
    if (error.response?.status === 403) {
      // Review dùng 403 cho lỗi nghiệp vụ (chưa mua, không phải chủ review) → không redirect, để component hiện toast.
      const url = error.config?.url || ''
      if (!url.startsWith('/reviews')) window.location.href = '/'
    }
```

- [ ] **Step 4: Verify type-check**

Run (workdir `frontend`): `npm run type-check`
Expected: exit 0, no errors.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/types/review.types.ts frontend/src/types/index.ts frontend/src/services/review.service.ts frontend/src/services/http.ts
git commit -m "feat: review types + service + 403 interceptor exception"
```

---

### Task 8: StarRating.vue component

**Files:**
- Create: `frontend/src/components/ui/StarRating.vue`

**Interfaces:**
- Consumes: `review.star` i18n key (Task 5)
- Produces (used by Task 9):
  - Props: `modelValue?: number` (default 0, fractional supported), `interactive?: boolean` (default false), `size?: 'sm' | 'md'` (default 'md')
  - Emits: `update:modelValue` (number 1-5) on click when interactive

- [ ] **Step 1: Create the component**

Create `frontend/src/components/ui/StarRating.vue`:

```vue
<template>
  <div
    class="relative inline-block shrink-0"
    :class="sizeClass"
    :role="interactive ? 'group' : 'img'"
    :aria-label="interactive ? undefined : `${modelValue} / 5`"
  >
    <div class="flex h-full w-full">
      <svg
        v-for="i in 5"
        :key="i"
        viewBox="0 0 24 24"
        class="h-full w-full text-[#E5E7EB]"
        fill="currentColor"
      >
        <path :d="STAR_PATH" />
      </svg>
    </div>
    <div
      class="absolute inset-y-0 left-0 overflow-hidden"
      :style="{ width: fillPercent }"
    >
      <div
        class="flex h-full"
        :class="sizeClass"
      >
        <svg
          v-for="i in 5"
          :key="i"
          viewBox="0 0 24 24"
          class="h-full w-full text-black"
          fill="currentColor"
        >
          <path :d="STAR_PATH" />
        </svg>
      </div>
    </div>
    <template v-if="interactive">
      <button
        v-for="i in 5"
        :key="'hit-' + i"
        type="button"
        class="absolute inset-y-0 cursor-pointer bg-transparent"
        :style="{ left: ((i - 1) * 20) + '%', width: '20%' }"
        :aria-label="`${i} ${t('review.star')}`"
        @mouseenter="hover = i"
        @mouseleave="hover = 0"
        @click="emit('update:modelValue', i)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const STAR_PATH =
  'M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    modelValue?: number
    interactive?: boolean
    size?: 'sm' | 'md'
  }>(),
  { modelValue: 0, interactive: false, size: 'md' }
)

const emit = defineEmits<{ (e: 'update:modelValue', value: number): void }>()

const hover = ref(0)
const shown = computed(() =>
  props.interactive && hover.value > 0 ? hover.value : props.modelValue
)
const fillPercent = computed(
  () => `${(Math.min(Math.max(shown.value, 0), 5) / 5) * 100}%`
)
const sizeClass = computed(() =>
  props.size === 'sm' ? 'h-4 w-[80px]' : 'h-6 w-[120px]'
)
</script>
```

- [ ] **Step 2: Verify type-check**

Run (workdir `frontend`): `npm run type-check`
Expected: exit 0.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/ui/StarRating.vue
git commit -m "feat: StarRating display + interactive component"
```

---

### Task 9: ProductReviews.vue component

**Files:**
- Create: `frontend/src/components/review/ProductReviews.vue`

**Interfaces:**
- Consumes: `reviewService` (Task 7), `StarRating` (Task 8), `truncateText`/`formatRelativeTime`/`formatDate` (Task 6 + existing), `review.*` + `common.*` + `errors.*` i18n (Task 5 + existing), `useAuthStore` (`isAuthenticated`), `BaseModal` (`v-model`, `title`, `#footer` slot), `BaseSkeleton`, `useToast`
- Produces (used by Task 10): prop `productId: number`; renders self-contained review section

- [ ] **Step 1: Create the component**

Create `frontend/src/components/review/ProductReviews.vue`:

```vue
<template>
  <section
    v-if="productId"
    class="w-[1152px] mx-auto bg-white px-8 py-10 mb-8"
  >
    <h2 class="font-geist text-xl font-semibold text-black mb-8">
      {{ t('review.title') }}
    </h2>

    <!-- Loading -->
    <div
      v-if="loading"
      class="space-y-4"
    >
      <BaseSkeleton
        v-for="i in 3"
        :key="i"
        type="text"
        class="w-full h-16"
      />
    </div>

    <!-- Load error -->
    <p
      v-else-if="error"
      class="text-[#5E5F5C]"
    >
      {{ t('review.loadFailed') }}
    </p>

    <!-- Empty -->
    <p
      v-else-if="!summary || summary.totalCount === 0"
      class="text-[#5E5F5C]"
    >
      {{ t('review.noReviews') }}
    </p>

    <template v-else>
      <!-- Summary header -->
      <div class="flex items-start gap-16 mb-10">
        <div class="flex flex-col items-center w-[180px]">
          <span class="font-geist text-[56px] font-semibold leading-none text-black">
            {{ formatAverage(summary.averageRating) }}
          </span>
          <StarRating
            :model-value="summary.averageRating"
            class="mt-2"
          />
          <span class="text-sm text-[#5E5F5C] mt-2">
            {{ t('review.count', { count: summary.totalCount }) }}
          </span>
        </div>
        <div class="flex-1 max-w-[420px] space-y-1">
          <div
            v-for="stars in [5, 4, 3, 2, 1]"
            :key="stars"
            class="flex items-center gap-3"
          >
            <span class="w-6 text-right text-sm font-medium text-black">{{ stars }}</span>
            <div class="flex-1 h-[6px] bg-[#E5E7EB]">
              <div
                class="h-full bg-black"
                :style="{ width: distributionPercent(stars) }"
              />
            </div>
            <span class="w-8 text-sm text-[#5E5F5C]">{{ summary.distribution[stars] || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Review list (2 mới nhất, có thể mở rộng) -->
      <div class="border-t border-[#E5E7EB]">
        <article
          v-for="review in visibleReviews"
          :key="review.id"
          class="py-8 border-b border-[#E5E7EB]"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-black text-white flex items-center justify-center font-geist text-sm font-semibold uppercase">
                {{ initials(review.authorName) }}
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-geist font-medium text-black">{{ review.authorName }}</span>
                  <span class="inline-flex items-center gap-1 border border-black px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-black">
                    <svg
                      viewBox="0 0 20 20"
                      fill="currentColor"
                      class="w-3 h-3"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M16.403 12.652a3 3 0 000-5.304 3 3 0 00-3.75-3.751 3 3 0 00-5.305 0 3 3 0 00-3.751 3.75 3 3 0 000 5.305 3 3 0 003.75 3.751 3 3 0 005.305 0 3 3 0 003.751-3.75zm-2.546-4.46a.75.75 0 00-1.214-.883l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z"
                        clip-rule="evenodd"
                      />
                    </svg>
                    {{ t('review.verifiedBuyer') }}
                  </span>
                </div>
                <div
                  class="text-xs text-[#5E5F5C] mt-1"
                  :title="formatDate(review.createdAt)"
                >
                  {{ formatRelativeTime(review.createdAt) }}
                </div>
              </div>
            </div>

            <div
              v-if="review.canModerate || review.isMine"
              class="flex items-center gap-3"
            >
              <button
                class="text-xs font-semibold uppercase tracking-wide text-black underline hover:text-[#5E5F5C]"
                @click="openEdit(review)"
              >
                {{ t('review.edit') }}
              </button>
              <button
                v-if="review.canModerate"
                class="text-xs font-semibold uppercase tracking-wide text-black underline hover:text-[#5E5F5C]"
                @click="askDelete(review)"
              >
                {{ t('review.delete') }}
              </button>
            </div>
          </div>

          <StarRating
            :model-value="review.rating"
            size="sm"
            class="mt-3"
          />

          <p
            v-if="review.comment"
            class="mt-3 font-gelasio text-base text-[#4C4546] leading-[26px] whitespace-pre-line"
          >
            {{ expanded.has(review.id) ? review.comment : truncateText(review.comment, 150) }}
            <button
              v-if="review.comment.length > 150"
              class="ml-1 text-sm font-medium text-black underline"
              @click="toggleExpand(review.id)"
            >
              {{ expanded.has(review.id) ? t('review.showLess') : t('review.seeMore') }}
            </button>
          </p>
        </article>
      </div>

      <button
        v-if="summary.totalCount > 2"
        class="mt-6 w-full h-12 border border-black text-black font-geist text-sm font-semibold uppercase tracking-wider hover:bg-black hover:text-white transition-colors"
        @click="showAll = !showAll"
      >
        {{ showAll ? t('review.showLess') : t('review.seeAll', { count: summary.totalCount }) }}
      </button>
    </template>

    <!-- Form viết đánh giá -->
    <div class="border-t border-[#E5E7EB] mt-10 pt-8">
      <p
        v-if="!authStore.isAuthenticated"
        class="text-[#5E5F5C]"
      >
        {{ t('review.needToLogin') }}
        <router-link
          to="/login"
          class="text-black underline font-medium"
        >
          {{ t('common.login') }}
        </router-link>
      </p>

      <template v-else-if="!myReview">
        <h3 class="font-geist text-base font-semibold text-black mb-4">
          {{ t('review.writeTitle') }}
        </h3>
        <p
          v-if="formError"
          class="text-sm mb-3 text-[#4C4546]"
        >
          {{ formError }}
        </p>
        <div class="flex items-center gap-3 mb-4">
          <span class="text-sm text-[#5E5F5C]">{{ t('review.ratingLabel') }}</span>
          <StarRating
            v-model="formRating"
            interactive
          />
        </div>
        <textarea
          v-model="formComment"
          rows="4"
          maxlength="1000"
          class="w-full max-w-[600px] border border-[#7E7576] p-3 font-gelasio text-base text-black focus:border-black focus:outline-none resize-none"
          :placeholder="t('review.commentPlaceholder')"
        />
        <div class="flex items-center gap-4 mt-3">
          <button
            class="h-12 px-8 bg-black text-white font-geist text-sm font-semibold uppercase tracking-wider hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="submitting || formRating === 0"
            @click="submitReview"
          >
            {{ submitting ? t('common.loading') : t('review.submit') }}
          </button>
          <span class="text-xs text-[#5E5F5C]">
            {{ t('review.charactersLeft', { count: 1000 - formComment.length }) }}
          </span>
        </div>
      </template>
    </div>

    <!-- Modal sửa đánh giá (dùng chung cho chủ review và admin) -->
    <BaseModal
      v-model="editModalOpen"
      :title="t('review.editTitle')"
    >
      <div class="flex items-center gap-3 mb-4">
        <span class="text-sm text-[#5E5F5C]">{{ t('review.ratingLabel') }}</span>
        <StarRating
          v-model="editRating"
          interactive
        />
      </div>
      <textarea
        v-model="editComment"
        rows="4"
        maxlength="1000"
        class="w-full border border-[#7E7576] p-3 text-base text-black focus:border-black focus:outline-none resize-none"
        :placeholder="t('review.commentPlaceholder')"
      />
      <div class="mt-2 text-xs text-[#5E5F5C]">
        {{ t('review.charactersLeft', { count: 1000 - editComment.length }) }}
      </div>
      <template #footer>
        <button
          class="h-10 px-6 border border-black text-black text-sm font-semibold hover:bg-black hover:text-white transition-colors"
          @click="editModalOpen = false"
        >
          {{ t('review.cancel') }}
        </button>
        <button
          class="h-10 px-6 bg-black text-white text-sm font-semibold hover:bg-gray-900 transition-colors disabled:opacity-50"
          :disabled="editSubmitting || editRating === 0"
          @click="submitEdit"
        >
          {{ editSubmitting ? t('common.loading') : t('review.update') }}
        </button>
      </template>
    </BaseModal>

    <!-- Modal xác nhận xóa -->
    <BaseModal
      v-model="deleteModalOpen"
      :title="t('review.deleteTitle')"
    >
      <p class="text-[#4C4546]">{{ t('review.deleteConfirm') }}</p>
      <template #footer>
        <button
          class="h-10 px-6 border border-black text-black text-sm font-semibold hover:bg-black hover:text-white transition-colors"
          @click="deleteModalOpen = false"
        >
          {{ t('review.cancel') }}
        </button>
        <button
          class="h-10 px-6 bg-black text-white text-sm font-semibold hover:bg-gray-900 transition-colors"
          @click="confirmDelete"
        >
          {{ t('review.delete') }}
        </button>
      </template>
    </BaseModal>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vue-toastification'
import StarRating from '@/components/ui/StarRating.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'
import { reviewService } from '@/services/review.service'
import { useAuthStore } from '@/stores/auth.store'
import { formatDate, formatRelativeTime, truncateText } from '@/utils/formatters'
import type { Review, ReviewListResponse } from '@/types/review.types'

const props = defineProps<{ productId: number }>()

const { t, locale } = useI18n()
const toast = useToast()
const authStore = useAuthStore()

const data = ref<ReviewListResponse | null>(null)
const loading = ref(true)
const error = ref(false)
const showAll = ref(false)
const expanded = ref<Set<number>>(new Set())

const formRating = ref(0)
const formComment = ref('')
const formError = ref('')
const submitting = ref(false)

const editModalOpen = ref(false)
const editReviewId = ref<number | null>(null)
const editRating = ref(0)
const editComment = ref('')
const editSubmitting = ref(false)

const deleteModalOpen = ref(false)
const deleteReviewId = ref<number | null>(null)

const summary = computed(() => data.value?.summary ?? null)
const reviews = computed<Review[]>(() => data.value?.reviews ?? [])
const visibleReviews = computed(() =>
  showAll.value ? reviews.value : reviews.value.slice(0, 2)
)
const myReview = computed(() => reviews.value.find((r) => r.isMine) ?? null)

const formatAverage = (avg: number): string => {
  const fixed = avg.toFixed(1)
  return locale.value === 'vi' ? fixed.replace('.', ',') : fixed
}

const distributionPercent = (stars: number): string => {
  const total = summary.value?.totalCount ?? 0
  if (total === 0) return '0%'
  return `${(((summary.value?.distribution[stars] ?? 0) / total) * 100).toFixed(0)}%`
}

const initials = (name: string): string => {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  return (parts.length > 1 ? parts[0][0] + parts[parts.length - 1][0] : parts[0].slice(0, 2)).toUpperCase()
}

const toggleExpand = (id: number) => {
  const next = new Set(expanded.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  expanded.value = next
}

const load = async () => {
  loading.value = true
  error.value = false
  try {
    data.value = await reviewService.getProductReviews(props.productId)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const submitReview = async () => {
  if (formRating.value === 0) return
  submitting.value = true
  formError.value = ''
  try {
    await reviewService.createReview(props.productId, {
      rating: formRating.value,
      comment: formComment.value.trim() || undefined
    })
    toast.success(t('review.submitted'))
    formRating.value = 0
    formComment.value = ''
    await load()
  } catch (e: any) {
    if (e?.response?.status === 403) formError.value = t('review.needToPurchase')
    else if (e?.response?.status === 409) formError.value = t('review.alreadyReviewed')
    else toast.error(t('review.loadFailed'))
  } finally {
    submitting.value = false
  }
}

const openEdit = (review: Review) => {
  editReviewId.value = review.id
  editRating.value = review.rating
  editComment.value = review.comment ?? ''
  editModalOpen.value = true
}

const submitEdit = async () => {
  if (editReviewId.value === null || editRating.value === 0) return
  editSubmitting.value = true
  try {
    await reviewService.updateReview(editReviewId.value, {
      rating: editRating.value,
      comment: editComment.value.trim() || undefined
    })
    toast.success(t('review.updated'))
    editModalOpen.value = false
    await load()
  } catch (e: any) {
    if (e?.response?.status === 403) toast.error(t('errors.accessDenied'))
    else toast.error(t('review.loadFailed'))
  } finally {
    editSubmitting.value = false
  }
}

const askDelete = (review: Review) => {
  deleteReviewId.value = review.id
  deleteModalOpen.value = true
}

const confirmDelete = async () => {
  if (deleteReviewId.value === null) return
  try {
    await reviewService.deleteReview(deleteReviewId.value)
    toast.success(t('review.deleted'))
    deleteModalOpen.value = false
    await load()
  } catch (e: any) {
    if (e?.response?.status === 403) toast.error(t('errors.accessDenied'))
    else toast.error(t('review.loadFailed'))
  }
}

onMounted(load)
</script>
```

- [ ] **Step 2: Verify type-check**

Run (workdir `frontend`): `npm run type-check`
Expected: exit 0.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/review/ProductReviews.vue
git commit -m "feat: ProductReviews section (summary, list, form, admin actions)"
```

---

### Task 10: Integrate into ProductDetail.vue + full verification

**Files:**
- Modify: `frontend/src/views/ProductDetail.vue`

- [ ] **Step 1: Embed the review section**

Modify `frontend/src/views/ProductDetail.vue`:
- Add import in `<script setup>` after `BaseSkeleton` import:

```ts
import ProductReviews from '@/components/review/ProductReviews.vue'
```

- Insert after the product detail `</section>` (the block ending `      </section>` right before `      <!-- Loading State -->`):

```html
      <!-- Product Reviews -->
      <ProductReviews
        v-if="product"
        :product-id="product.id"
      />
```

- [ ] **Step 2: Full frontend verification**

Run (workdir `frontend`):
1. `npm run type-check` — Expected: exit 0
2. `npm run lint` — Expected: no errors (auto-fix may modify files; re-run `git diff` to confirm only expected files changed)
3. `npm run test` — Expected: all vitest suites pass (formatters, parity, locale, auth store, validators, apiError)
4. `npm run build` — Expected: build completes, no TS errors

- [ ] **Step 3: Full backend verification**

Run (repo root): `./gradlew test`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Manual smoke test**

1. Backend: `./gradlew bootRun` (SQL Server `localhost:1444/dbTheXuong` must be running)
2. Frontend (workdir `frontend`): `npm run dev` → http://localhost:5173
3. **Guest:** mở 1 sản phẩm → thấy header "ĐÁNH GIÁ SẢN PHẨM" + điểm TB + phân bố sao + tối đa 2 review, nút "Xem tất cả N đánh giá" nếu >2, nút "Xem thêm" trên review dài. Cuối section hiện "Bạn cần đăng nhập để viết đánh giá." + link login.
4. **Customer đã mua (đơn COMPLETED):** thấy form sao + textarea; chọn 5 sao, gõ review >150 ký tự, gửi → toast thành công, review hiện đầu danh sách với badge "Đã mua hàng" + nút Sửa. Gửi lần 2 → hiện "Bạn đã đánh giá sản phẩm này rồi."
5. **Customer chưa mua:** gửi review → hiện "Bạn cần mua sản phẩm này để viết đánh giá."
6. **Admin (ADMIN hoặc BOTH):** mỗi review có nút Sửa + Xóa. Sửa → modal đổi sao/nội dung → toast cập nhật. Xóa → modal xác nhận → review biến mất, summary cập nhật.
7. **Đổi ngôn ngữ EN:** toàn bộ section chuyển tiếng Anh ("PRODUCT REVIEWS", "Verified buyer", "See all N reviews", "3 days ago"...).
8. Nếu review ≤2: không có nút "Xem tất cả". Nếu 0 review: hiện "Chưa có đánh giá nào...".

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/ProductDetail.vue
git commit -m "feat: embed ProductReviews in product detail page"
```

---

## Self-Review Notes

- Spec coverage: verified-purchase-only (Task 3 tests), 1 review/user/product (Task 3 duplicate test + UNIQUE constraint untouched), user edit own/not delete (Task 3 update/delete permission tests), admin edit+delete (Tasks 3, 9), 2 latest + expand (Task 9 `visibleReviews`/`showAll`), 150-char truncation + More (Tasks 6, 9), VI/EN i18n (Task 5 + parity test), black monochrome theme (Tasks 8, 9), timestamp relative + tooltip (Tasks 6, 9), star summary + distribution (Tasks 3, 9), no DB migration (Global Constraints), rate limit untouched.
- Type consistency: `ReviewDto` field names (`isMine`, `canModerate`, `authorName`, `verifiedBuyer`) match `Review` TS interface; `ReviewListResponse` matches `ReviewListResponse` TS; `review.*` i18n keys used in Tasks 6/8/9 all defined in Task 5; service method signatures used in controller (Task 4) match Task 3.
- No placeholders: every step has complete code and exact commands.
