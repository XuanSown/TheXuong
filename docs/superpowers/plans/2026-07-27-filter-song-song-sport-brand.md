# Bộ lọc song hành Sport + Brand Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Khi user chọn cả sport + brand trên `/products`, backend trả về product thỏa cả 2 điều kiện (hiện tại chỉ áp dụng sport do `else if` short-circuit).

**Architecture:** Thêm 1 method `findByFilters` JPQL với optional params dùng `IS NULL` check tại Repository; Controller thay if/else if bằng 1 call duy nhất, gán `null` cho param rỗng. Sort by id/price inject qua `Pageable` (giữ nguyên).

**Tech Stack:** Java 21, Spring Boot 3.5.9, Spring Data JPA, JUnit 5, H2 (in-memory test), Gradle.

## Global Constraints

- Không sửa frontend (`Products.vue` đã đúng sau fix #1).
- Không hardcode `ORDER BY` trong JPQL `findByFilters` — sort đến từ `Pageable`.
- Giữ pattern validation `@Pattern` ở controller params (lines 51-54).
- Empty string param phải được chuẩn hóa thành `null` trước khi bind vào JPQL (vì `:keyword IS NULL` chỉ khớp Java null).
- Schema entity dùng field tên `sport` / `brand` (ManyToOne) — đã verify `Product.java:42,46`.
- Repo hiện đã extends `JpaRepository<Product, Long>`.
- Repositories không xóa method derived cũ trừ khi grep xác nhận không còn consumer.

---

## File Structure

| File | Action | Responsibility |
|---|---|---|
| `build.gradle` | Modify (line 87 area) | Thêm H2 testRuntimeOnly dep |
| `src/test/resources/application-test.properties` | Create | Cấu hình H2 cho @DataJpaTest |
| `src/test/java/com/example/thexuong/repository/ProductRepositoryTest.java` | Create | Unit test `findByFilters` với các combo filter |
| `src/main/java/com/example/thexuong/repository/ProductRepository.java` | Modify (~line 76) | Thêm method `findByFilters` |
| `src/main/java/com/example/thexuong/controller/ProductRestController.java` | Modify (lines 66-74) | Thay if/else if bằng 1 call `findByFilters` |

---

## Task 1: Setup test infrastructure (H2)

**Files:**
- Modify: `build.gradle` (test deps block)
- Create: `src/test/resources/application-test.properties`

**Interfaces:**
- Produces: H2 trên test classpath + test properties cho @DataJpaTest picks up.

- [ ] **Step 1: Add H2 testRuntimeOnly dependency**

Modify `build.gradle` sau dòng 86 (`testImplementation 'org.springframework.security:spring-security-test'`):

```gradle
    // H2 for @DataJpaTest in-memory DB (test only)
    testRuntimeOnly 'com.h2database:h2'
```

- [ ] **Step 2: Create test application.properties**

Create `src/test/resources/application-test.properties`:

```properties
# H2 in-memory cho @DataJpaTest; tách biệt SQL Server prod
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MSSQLServer
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

`MODE=MSSQLServer` để H2 bắt chước syntax SQL Server (cho.native queries khác trong repo).

- [ ] **Step 3: Verify build still works**

Run: `./gradlew compileTestJava --console=plain -q`
Expected: SUCCESS, no output (or only deprecation warnings).

- [ ] **Step 4: Commit**

```bash
git add build.gradle src/test/resources/application-test.properties
git commit -m "test: them H2 in-memory cho @DataJpaTest"
```

---

## Task 2: Write failing test for `findByFilters`

**Files:**
- Create: `src/test/java/com/example/thexuong/repository/ProductRepositoryTest.java`

**Interfaces:**
- Consumes: `Product`, `Sport`, `Brand` entities (existing). Cần 1 `SportRepository` + `BrandRepository` để setup data — kiểm tra tồn tại:

Run: `grep -l "SportRepository\|BrandRepository" src/main/java/com/example/thexuong/repository/`
- Nếu tồn tại: dùng chúng.
- Nếu không: tạo inline `JpaRepository<Sport, Long>` + `JpaRepository<Brand, Long>` bên trong test as `@TestConfiguration` static class (test-only, không chạm production).

- [ ] **Step 1: Locate (or define) Sport/Brand repositories**

```bash
ls src/main/java/com/example/thexuong/repository/
```

Expected output: list including (or not) `SportRepository.java` / `BrandRepository.java`.

- [ ] **Step 2: Write failing test**

Create `src/test/java/com/example/thexuong/repository/ProductRepositoryTest.java`:

```java
package com.example.thexuong.repository;

import com.example.thexuong.entity.Brand;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.Sport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductRepository productRepository;

    private Sport sportFootball;
    private Sport sportBadminton;
    private Brand brandNike;
    private Brand brandYonex;

    private void seed() {
        sportFootball = new Sport();
        sportFootball.setName("Bóng đá");
        em.persist(sportFootball);

        sportBadminton = new Sport();
        sportBadminton.setName("Cầu lông");
        em.persist(sportBadminton);

        brandNike = new Brand();
        brandNike.setName("Nike");
        em.persist(brandNike);

        brandYonex = new Brand();
        brandYonex.setName("Yonex");
        em.persist(brandYonex);

        // Sản phẩm: sport football + Nike
        Product p1 = new Product();
        p1.setName("Giày Nike Football");
        p1.setSport(sportFootball);
        p1.setBrand(brandNike);
        em.persist(p1);

        // Sản phẩm: sport badminton + Yonex
        Product p2 = new Product();
        p2.setName("Vợt Yonex Cầu lông");
        p2.setSport(sportBadminton);
        p2.setBrand(brandYonex);
        em.persist(p2);

        // Cross-product: sport football + Yonex (chỉ thỏa sport-only)
        Product p3 = new Product();
        p3.setName("Giày Yonex Football");
        p3.setSport(sportFootball);
        p3.setBrand(brandYonex);
        em.persist(p3);

        em.flush();
        em.clear();
    }

    @Test
    void findByFilters_noFilter_returnsAll() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void findByFilters_sportOnly() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, "Bóng đá", null, PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(p -> assertThat(p.getSport().getName()).isEqualTo("Bóng đá"));
    }

    @Test
    void findByFilters_brandOnly() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, null, "Yonex", PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(p -> assertThat(p.getBrand().getName()).isEqualTo("Yonex"));
    }

    @Test
    void findByFilters_sportAndBrand_returnsOnlyMatchingBoth() {
        seed();
        Page<Product> page = productRepository.findByFilters(null, "Bóng đá", "Yonex", PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Giày Yonex Football");
    }

    @Test
    void findByFilters_keyword_sport_and_brand() {
        seed();
        Page<Product> page = productRepository.findByFilters("vợt", "Cầu lông", "Yonex", PageRequest.of(0, 10));
        List<Product> result = page.getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Vợt Yonex Cầu lông");
    }
}
```

**Lưu ý setting fields:** Kiểm `Sport`/`Brand` entity có field `name` + setter không (đa số có). Nếu entity có `@NotNull` cho các field khác (như `Product.price`), persistence test sẽ fail → cần thêm giá trị mặc định trong `seed()`. Step 3 sẽ phát hiện.

- [ ] **Step 3: Run test to verify failure (method chưa tồn tại)**

Run: `./gradlew test --tests "ProductRepositoryTest" --console=plain`
Expected: COMPILE FAIL với `cannot find symbol: method findByFilters`.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/example/thexuong/repository/ProductRepositoryTest.java
git commit -m "test: viet failing test cho findByFilters"
```

---

## Task 3: Add `findByFilters` method to Repository

**Files:**
- Modify: `src/main/java/com/example/thexuong/repository/ProductRepository.java` (~line 76, sau `findAllByOrderByIdDesc`)

**Interfaces:**
- Produces: `Page<Product> findByFilters(String keyword, String sport, String brand, Pageable pageable)`. Signature dùng cho Task 4.

- [ ] **Step 1: Add the @Query method**

Insert sau dòng 74 (`List<Product> findAllByOrderByIdDesc(Pageable pageable);`) trong `ProductRepository.java`:

```java
// Bộ lọc song song: keyword + sport + brand. Sort inject qua Pageable.
// ponytail: null-binding thay vì Optional, tránh bùng nổ method derived.
@Query("""
    SELECT p FROM Product p
    WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:sport IS NULL OR p.sport.name = :sport)
      AND (:brand IS NULL OR p.brand.name = :brand)
""")
Page<Product> findByFilters(
    @Param("keyword") String keyword,
    @Param("sport") String sport,
    @Param("brand") String brand,
    Pageable pageable
);
```

`@Param` đã có import (line 10). `Pageable`, `Page` đã có import (lines 6-7).

Không có `ORDER BY` cố định — sort đến từ `Pageable` trong controller (đã có `Sort.by("id").descending()` / `Sort.by("price")`).

- [ ] **Step 2: Run test to verify passes**

Run: `./gradlew test --tests "ProductRepositoryTest" --console=plain`
Expected: PASS, 5 tests.

Nếu fail vì entity `@NotNull` constraint → fix bằng cách thêm setter trong `seed()` (Ví dụ `p.setPrice(...)`), KHÔNG thay đổi entity production semantics.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/repository/ProductRepository.java
git commit -m "feat: them findByFilters JPQL cho bo loc song song"
```

---

## Task 4: Update Controller to use `findByFilters`

**Files:**
- Modify: `src/main/java/com/example/thexuong/controller/ProductRestController.java` (lines 66-74)

- [ ] **Step 1: Replace if/else if block**

Replace lines 66-74 trong `ProductRestController.java`:

Từ:
```java
        if (keyword != null && !keyword.isEmpty()) {
            productsPage = productRepository.findByNameContaining(keyword, pageable);
        } else if (sport != null && !sport.isEmpty()) {
            productsPage = productRepository.findBySport_Name(sport, pageable);
        } else if (brand != null && !brand.isEmpty()) {
            productsPage = productRepository.findByBrand_Name(brand, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }
```

Thành:
```java
        // Chuẩn hóa empty -> null vì ":param IS NULL" chỉ khớp Java null, không khớp "".
        String kw = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        String sp = (sport    != null && !sport.isEmpty())    ? sport    : null;
        String br = (brand    != null && !brand.isEmpty())    ? brand    : null;
        productsPage = productRepository.findByFilters(kw, sp, br, pageable);
```

Giữ nguyên `Pageable pageable` construção ở dòng 63 (`PageRequest.of(page, size, sorting)`) — sort vẫn injection qua đây.

- [ ] **Step 2: Verify full build + all tests**

Run: `./gradlew build --console=plain -x npmBuild -x copyFrontend -x processResources`
Expected: BUILD SUCCESSFUL, `ProductRepositoryTest` 5 tests pass + existing `RateLimit*Test` pass.

(Bỏ `npmBuild`/`copyFrontend` vì không cần cho backend test — tiết kiệm vài phút build.)

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/ProductRestController.java
git commit -m "fix: controller dung findByFilters -> bo loc sport+brand song song"
```

---

## Task 5: Manual end-to-end verification

**Files:** None modified. Smoke test against running backend.

- [ ] **Step 1: Start backend dev server**

Run: `./gradlew bootRun --console=plain`
(nên làm trong terminal riêng). Đợi: `Started TheXuongApplication ... port 8080`.

- [ ] **Step 2: Curl 5 scenarios**

Trong terminal khác:

```bash
# 1. No filter
curl -s "http://localhost:8080/api/v1/products?page=0&size=20" | jq '.totalElements'

# 2. Sport only: Bóng đá (slug đa đã map thành tiếng Việt ở FE; test thẳng giá trị DB)
curl -s "http://localhost:8080/api/v1/products?sport=B%C3%B3ng%20%C4%91%C3%A1" | jq '.totalElements'

# 3. Brand only: Nike
curl -s "http://localhost:8080/api/v1/products?brand=Nike" | jq '.totalElements'

# 4. Song song: Bóng đá + Yonex
curl -s "http://localhost:8080/api/v1/products?sport=B%C3%B3ng%20%C4%91%C3%A1&brand=Yonex" | jq '.content[].name'

# 5. Sort by price ascending vẫn hoạt động:
curl -s "http://localhost:8080/api/v1/products?sort=price_asc&brand=Nike" | jq '.content[].price'
```

Expected:
- #1: > 0
- #2: ít hơn #1 (đã filter)
- #3: ít hơn #1
- #4: list product có brand Yonex + sport Bóng đá, không có Nike / Cầu lông
- #5: mảng price sorted tăng dần

- [ ] **Step 3: Frontend sanity check (optional, qua Cloudflare tunnel)**

Mở: `https://thexuong.xuansown.id.vn/products?sport=badminton&brand=yonex`
Expected: grid product chỉ Yonex + Cầu lông (không lẫn Nike / Bóng đá).

---

## Self-Review

**1. Spec coverage:**
- Repository `findByFilters` with optional params + `IS NULL` → Task 3 ✓
- Controller null normalization + single call → Task 4 ✓
- 5 manual verification scenarios (no/sport/brand/both/keyword+sport+brand) → Task 5 + tests in Task 2 ✓
- Sort via Pageable preserved → Task 4 Step 1 keeps `Pageable` from line 63 ✓

**2. Placeholder scan:** No TBD / "add appropriate" / "similar to". All code concrete.

**3. Type consistency:**
- `findByFilters(String, String, String, Pageable) → Page<Product>` — same throughout Tasks 2, 3, 4 ✓
- Field names `sport` / `brand` (singular) match `Product.java:42,46` ✓
- `@Param` import line 10 ✓; `Page`/`Pageable` import lines 6-7 ✓

No issues found.