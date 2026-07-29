# Design: Bộ lọc song song Sport + Brand

- **Ngày:** 2026-07-27
- **Loại:** Bug fix
- **Files chạm:** `ProductRepository.java`, `ProductRestController.java`

## Vấn đề

Bộ lọc "Thể thao" và "Thương hiệu" ở `/products` không hoạt động song song. Khi user chọn cả 2, chỉ "Thể thao" được áp dụng; "Thương hiệu" bị bỏ qua. Tương tự filter từ khóa (keyword) chặn mất sport/brand.

## Root cause

`ProductRestController.java:66-74` dùng chuỗi `if / else if`:

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

`else if` short-circuit: có sport → brand bị bỏ. Có keyword → cả sport + brand bị bỏ.

Frontend (`Products.vue` post-fix #1) đã gửi song song `sport` + `brand` đúng; backend là điểm hỏng duy nhất.

## Giải pháp (Approach 2 — optional @Query)

### Repository — `ProductRepository.java`

Thêm 1 method duy nhất, giữ nguyên các method derived cũ (vì `findByNameContaining`, `findBySport_Name`, `findByBrand_Name` không còn được controller gọi, nhưng không xóa để tránh RIP consumer khác — kiểm tra bằng grep trước khi xóa):

```java
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

Sort (`id DESC` / `price ASC` / `price DESC`) được inject qua `Pageable` (đã có ở controller `:56-63`), không hardcode trong JPQL → bảo toàn sort hiện tại.

### Controller — `ProductRestController.java`

Thay block `if/else if` (line 66-74) bằng:

```java
String keywordParam = (keyword == null || keyword.isEmpty()) ? null : keyword;
String sportParam   = (sport   == null || sport.isEmpty())   ? null : sport;
String brandParam   = (brand   == null || brand.isEmpty())   ? null : brand;
productsPage = productRepository.findByFilters(keywordParam, sportParam, brandParam, pageable);
```

### Lý do null thay vì empty string

JPQL `:keyword IS NULL` chỉ khớp khi bindJava param thực sự là `null`. Empty string `""` sẽ đượcHibernate coi là NOT NULL → mệnh đề OR thứ nhất luôn false → filter keyword hoạt động sai (chỉ khớp product có name rỗng).

### Bảo toàn hành vi cũ

- `Pageable` giữ nguyên → sort, page, size y hệt
- Pattern validation ở `@Pattern` trên các param (line 51-54) giữ nguyên
- Response JSON schema không đổi

## Phạm vi thay đổi

| File | Đổi |
|---|---|
| `ProductRepository.java` | +1 method `findByFilters` |
| `ProductRestController.java` | Block if/else if → gọi `findByFilters` (+5 dòng, -9 dòng) |

## Không trong phạm vi

- Không sửa frontend (đã đúng sau fix #1)
- Không xóa các method derived cũ trừ khi grep xác nhận không còn consumer
- Không thêm filter mới (chỉ keyword/sport/brand hiện có)

## Verification

1. Build: `./gradlew compileJava`
2. Manual test trên dev server (Cloudflare tunnel):
   - `/products?sport=badminton&brand=nike` → chỉ trả product Nike + Cầu lông
   - `/products?sport=badminton` → tất cả Cầu lông
   - `/products?brand=nike` → tất cả Nike
   - `/products?keyword=giày&sport=football` → giày bóng đá
   - `/products` (không filter) → tất cả
3. Không regression trên sort `newest`/`price_asc`/`price_desc`