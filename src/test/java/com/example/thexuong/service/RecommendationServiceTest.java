package com.example.thexuong.service;

import com.example.thexuong.dto.RecommendationProductDto;
import com.example.thexuong.entity.Brand;
import com.example.thexuong.entity.Category;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.entity.SizeType;
import com.example.thexuong.entity.Sport;
import com.example.thexuong.repository.BrandRepository;
import com.example.thexuong.repository.CategoryRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.SizeRepository;
import com.example.thexuong.repository.SizeTypeRepository;
import com.example.thexuong.repository.SportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RecommendationService.class)
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private SportRepository sportRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private SizeRepository sizeRepository;
    @Autowired
    private SizeTypeRepository sizeTypeRepository;

    private SizeType shoeSizeType;

    private Sport sport(String name) {
        return sportRepository.save(Sport.builder().name(name).build());
    }

    private Category category(String name) {
        return categoryRepository.save(Category.builder().name(name).build());
    }

    private Brand brand(String name) {
        return brandRepository.save(Brand.builder().name(name).build());
    }

    private Size size(String name) {
        if (shoeSizeType == null) {
            shoeSizeType = sizeTypeRepository.save(SizeType.builder().code("SHOE").name("Shoes").active(true).build());
        }
        return sizeRepository.save(Size.builder().name(name).sizeType(shoeSizeType).active(true).build());
    }

    private Product product(String name, Sport sport, Category category, Brand brand,
                            BigDecimal price, int viewCount, boolean active) {
        return productRepository.save(Product.builder()
                .name(name).sport(sport).category(category).brand(brand)
                .price(price).viewCount(viewCount).active(active).build());
    }

    private void withStock(Product product, int qty) {
        productVariantRepository.save(ProductVariant.builder()
                .product(product).size(size("42")).quantity(qty).build());
    }

    private List<Long> idsOf(Product... products) {
        return java.util.Arrays.stream(products).map(Product::getId).toList();
    }

    private List<Long> resultIds(List<RecommendationProductDto> result) {
        return result.stream().map(RecommendationProductDto::getId).toList();
    }

    // ================= Tests =================

    @Test
    void emptyProductIds_returnsEmpty() {
        assertThat(recommendationService.recommendForCart(List.of(), 8)).isEmpty();
    }

    @Test
    void singleProductInCart_recommendsRelatedProducts() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");
        Brand adidas = brand("ADIDAS");

        Product inCart = product("P-in-cart", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(inCart, 10);

        Product c1 = product("C1-related", football, shoes, nike, BigDecimal.valueOf(90), 40, true);
        withStock(c1, 5);
        Product c2 = product("C2-related", football, shoes, nike, BigDecimal.valueOf(500), 10, true);
        withStock(c2, 5);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(inCart.getId()), 8);

        assertThat(result).isNotEmpty();
        assertThat(resultIds(result)).doesNotContain(inCart.getId());
        assertThat(resultIds(result)).contains(c1.getId(), c2.getId());
        // C1 giống giá hơn -> score cao hơn -> xếp trước
        assertThat(resultIds(result).indexOf(c1.getId())).isLessThan(resultIds(result).indexOf(c2.getId()));
    }

    @Test
    void multipleProductsInCart_recommendForBoth() {
        Sport football = sport("FOOTBALL");
        Sport running = sport("RUNNING");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");
        Brand adidas = brand("ADIDAS");

        Product p1 = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(p1, 10);
        Product p2 = product("P2", running, shoes, adidas, BigDecimal.valueOf(200), 30, true);
        withStock(p2, 10);

        Product c1 = product("C1", football, shoes, nike, BigDecimal.valueOf(90), 10, true);
        withStock(c1, 5);
        Product c2 = product("C2", running, shoes, adidas, BigDecimal.valueOf(210), 10, true);
        withStock(c2, 5);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(p1.getId(), p2.getId()), 8);

        assertThat(resultIds(result)).contains(c1.getId(), c2.getId());
        assertThat(resultIds(result)).doesNotContain(p1.getId(), p2.getId());
    }

    @Test
    void duplicateProductIds_areDeduped() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product p1 = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(p1, 10);
        Product c1 = product("C1", football, shoes, nike, BigDecimal.valueOf(90), 10, true);
        withStock(c1, 5);

        List<RecommendationProductDto> single = recommendationService.recommendForCart(List.of(p1.getId()), 8);
        List<RecommendationProductDto> duplicated = recommendationService.recommendForCart(List.of(p1.getId(), p1.getId(), p1.getId()), 8);

        assertThat(resultIds(duplicated)).isEqualTo(resultIds(single));
    }

    @Test
    void inactiveCandidate_isExcluded() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product inCart = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(inCart, 10);
        Product inactive = product("INACTIVE", football, shoes, nike, BigDecimal.valueOf(80), 100, false);
        withStock(inactive, 5);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(inCart.getId()), 8);

        assertThat(resultIds(result)).doesNotContain(inactive.getId());
    }

    @Test
    void outOfStockCandidate_isExcluded() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product inCart = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(inCart, 10);
        Product outOfStock = product("OUT_OF_STOCK", football, shoes, nike, BigDecimal.valueOf(80), 100, true);
        withStock(outOfStock, 0);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(inCart.getId()), 8);

        assertThat(resultIds(result)).doesNotContain(outOfStock.getId());
    }

    @Test
    void productsAlreadyInCart_areExcluded() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product p1 = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(p1, 10);
        Product p2 = product("P2", football, shoes, nike, BigDecimal.valueOf(120), 40, true);
        withStock(p2, 10);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(p1.getId(), p2.getId()), 8);

        assertThat(resultIds(result)).doesNotContain(p1.getId(), p2.getId());
    }

    @Test
    void limit_capsResults() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product inCart = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(inCart, 10);
        for (int i = 1; i <= 5; i++) {
            Product c = product("C" + i, football, shoes, nike, BigDecimal.valueOf(100 - i), 10, true);
            withStock(c, 5);
        }

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(inCart.getId()), 2);

        assertThat(result).hasSize(2);
    }

    @Test
    void insufficientCandidates_fallbackFillsWithPopular() {
        Sport football = sport("FOOTBALL");
        Sport other = sport("OTHER");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product inCart = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(inCart, 10);
        Product c1 = product("C1", football, shoes, nike, BigDecimal.valueOf(90), 10, true);
        withStock(c1, 5);

        // Không liên quan (khác sport/category/brand) -> chỉ vào qua fallback
        Product popular = product("POPULAR", other, shoes, nike, BigDecimal.valueOf(300), 1000, true);
        withStock(popular, 5);
        Product lowView = product("LOW_VIEW", other, shoes, nike, BigDecimal.valueOf(350), 1, true);
        withStock(lowView, 5);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(inCart.getId()), 8);

        // C1 là candidate; POPULAR (viewCount cao) được fallback thêm trước, LOW_VIEW sau đó
        assertThat(resultIds(result)).contains(c1.getId(), popular.getId(), lowView.getId());
        assertThat(resultIds(result)).doesNotContain(inCart.getId());
        assertThat(resultIds(result).indexOf(popular.getId())).isLessThan(resultIds(result).indexOf(lowView.getId()));
    }

    @Test
    void invalidProductId_isIgnored() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product p1 = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(p1, 10);
        Product c1 = product("C1", football, shoes, nike, BigDecimal.valueOf(90), 10, true);
        withStock(c1, 5);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(999999L, p1.getId()), 8);

        assertThat(resultIds(result)).contains(c1.getId());
    }

    @Test
    void equalScores_tieBreakDeterministic() {
        Sport football = sport("FOOTBALL");
        Category shoes = category("SHOES");
        Brand nike = brand("NIKE");

        Product inCart = product("P1", football, shoes, nike, BigDecimal.valueOf(100), 50, true);
        withStock(inCart, 10);

        // Hai candidate giống hệt nhau về mọi attribute + giá -> cùng score
        Product ta1 = product("TA1", football, shoes, nike, BigDecimal.valueOf(100), 0, true);
        withStock(ta1, 5);
        Product ta2 = product("TA2", football, shoes, nike, BigDecimal.valueOf(100), 0, true);
        withStock(ta2, 5);

        List<RecommendationProductDto> result = recommendationService.recommendForCart(List.of(inCart.getId()), 8);

        // Tie-break: viewCount DESC -> id ASC => TA1 (id nhỏ hơn) trước TA2
        assertThat(ta1.getId()).isLessThan(ta2.getId());
        assertThat(resultIds(result).indexOf(ta1.getId())).isLessThan(resultIds(result).indexOf(ta2.getId()));

        // Gọi lại lần 2 -> kết quả giống hệt (deterministic)
        List<RecommendationProductDto> again = recommendationService.recommendForCart(List.of(inCart.getId()), 8);
        assertThat(resultIds(again)).isEqualTo(resultIds(result));
    }
}
