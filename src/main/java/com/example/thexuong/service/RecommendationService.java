package com.example.thexuong.service;

import com.example.thexuong.dto.RecommendationProductDto;
import com.example.thexuong.entity.Product;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Rule-based cart recommendation V1 (không dùng AI/ML).
 *
 * Ranking:
 *   cùng sport     +4
 *   cùng category  +3
 *   cùng brand     +2
 *   giá tương đồng +1
 *   phổ biến       +1
 *
 * Tie-break deterministic: viewCount DESC, id ASC.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;
    private static final BigDecimal PRICE_SIMILARITY_RATIO = BigDecimal.valueOf(0.3);

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<RecommendationProductDto> recommendForCart(List<Long> productIds, Integer requestedLimit) {
        // Dedupe + loại null
        List<Long> uniqueIds = productIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        int limit = requestedLimit != null ? requestedLimit : DEFAULT_LIMIT;
        if (limit <= 0) {
            return List.of();
        }
        int cappedLimit = Math.min(limit, MAX_LIMIT);

        // 1. Load sản phẩm trong cart (unique productId)
        List<Product> cartProducts = productRepository.findAllByIdsWithAttributes(uniqueIds);
        if (cartProducts.isEmpty()) {
            return List.of();
        }
        Set<Long> cartIds = extractIds(cartProducts, Product::getId);

        // 2. Candidate pool giới hạn theo sport/category/brand của cart
        Set<Long> sportIds = extractIds(cartProducts, p -> p.getSport() != null ? p.getSport().getId() : null);
        Set<Long> categoryIds = extractIds(cartProducts, p -> p.getCategory() != null ? p.getCategory().getId() : null);
        Set<Long> brandIds = extractIds(cartProducts, p -> p.getBrand() != null ? p.getBrand().getId() : null);

        List<Product> candidates = productRepository.findRecommendationCandidates(
                cartIds,
                !sportIds.isEmpty(), sportIds.isEmpty() ? List.of(-1L) : sportIds,
                !categoryIds.isEmpty(), categoryIds.isEmpty() ? List.of(-1L) : categoryIds,
                !brandIds.isEmpty(), brandIds.isEmpty() ? List.of(-1L) : brandIds
        );

        // 3. Scoring + sort deterministic
        Map<Long, Integer> scores = new HashMap<>();
        for (Product candidate : candidates) {
            scores.put(candidate.getId(), scoreAgainstCart(candidate, cartProducts, candidates));
        }
        candidates.sort(Comparator
                .comparingInt((Product p) -> scores.get(p.getId())).reversed()
                .thenComparing(Comparator.comparingInt(Product::getViewCount).reversed())
                .thenComparingLong(Product::getId));

        List<Product> picked = new ArrayList<>(candidates.subList(0, Math.min(cappedLimit, candidates.size())));

        // 4. Fallback khi chưa đủ limit: sản phẩm phổ biến còn hàng
        if (picked.size() < cappedLimit) {
            Set<Long> excluded = new HashSet<>(cartIds);
            picked.forEach(p -> excluded.add(p.getId()));
            List<Product> fallback = productRepository.findPopularInStock(
                    excluded, PageRequest.of(0, cappedLimit - picked.size()));
            LinkedHashSet<Product> merged = new LinkedHashSet<>(picked);
            merged.addAll(fallback);
            picked = new ArrayList<>(merged.stream().limit(cappedLimit).toList());
        }

        // 5. Map sang lightweight DTO
        return picked.stream().map(this::toDto).toList();
    }

    /**
     * Điểm candidate so với từng sản phẩm unique trong cart.
     * Không cộng dồn vô hạn: mỗi attribute chỉ cộng tối đa theo số cart product unique.
     */
    private int scoreAgainstCart(Product candidate, List<Product> cartProducts, List<Product> candidates) {
        int score = 0;
        for (Product cp : cartProducts) {
            if (sameAttributeId(candidate.getSport(), cp.getSport())) score += 4;
            if (sameAttributeId(candidate.getCategory(), cp.getCategory())) score += 3;
            if (sameAttributeId(candidate.getBrand(), cp.getBrand())) score += 2;
            if (isSimilarPrice(candidate.getPrice(), cp.getPrice())) score += 1;
        }
        // Độ phổ biến: từ mức trung bình viewCount của candidate pool -> +1
        double avgViews = candidates.stream().mapToInt(Product::getViewCount).average().orElse(0);
        if (candidate.getViewCount() >= avgViews) score += 1;
        return score;
    }

    private boolean sameAttributeId(Object a, Object b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean isSimilarPrice(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        BigDecimal diff = a.subtract(b).abs();
        return diff.compareTo(b.multiply(PRICE_SIMILARITY_RATIO)) <= 0;
    }

    private Set<Long> extractIds(Collection<Product> products, Function<Product, Long> idExtractor) {
        Set<Long> ids = new HashSet<>();
        for (Product p : products) {
            Long id = idExtractor.apply(p);
            if (id != null) ids.add(id);
        }
        return ids;
    }

    private RecommendationProductDto toDto(Product p) {
        return RecommendationProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .imageUrl(p.getImageUrl())
                .sport(p.getSport() != null ? p.getSport().getName() : null)
                .brand(p.getBrand() != null ? p.getBrand().getName() : null)
                .category(p.getCategory() != null ? p.getCategory().getName() : null)
                .build();
    }
}
