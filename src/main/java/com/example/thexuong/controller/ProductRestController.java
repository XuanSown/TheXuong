package com.example.thexuong.controller;

import com.example.thexuong.dto.ProductDto;
import com.example.thexuong.dto.SizeDto;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for Products (Vue frontend consumption).
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductRepository productRepository;

    /**
     * GET /api/products
     * Query params: page, size, keyword, sport, brand, sort
     */
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "newest") String sort) {

        Sort sorting = Sort.by("id").descending();
        if ("price_asc".equals(sort)) {
            sorting = Sort.by("price").ascending();
        } else if ("price_desc".equals(sort)) {
            sorting = Sort.by("price").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Product> productsPage;

        if (keyword != null && !keyword.isEmpty()) {
            productsPage = productRepository.findByNameContaining(keyword, pageable);
        } else if (sport != null && !sport.isEmpty()) {
            productsPage = productRepository.findBySport(sport, pageable);
        } else if (brand != null && !brand.isEmpty()) {
            productsPage = productRepository.findByBrand(brand, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        // Convert to DTOs
        List<ProductDto> productDtos = productsPage.getContent().stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", productDtos);
        response.put("totalElements", productsPage.getTotalElements());
        response.put("totalPages", productsPage.getTotalPages());
        response.put("size", productsPage.getSize());
        response.put("number", productsPage.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/products/{id}
     * Returns product detail with variants
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        // Increment view count
        if (product.getViewCount() == null) {
            product.setViewCount(0);
        }
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        ProductDto productDto = toProductDto(product);
        productDto.setStockQuantity(calculateTotalStock(product.getVariants()));

        return ResponseEntity.ok(productDto);
    }

    /**
     * GET /api/products/{id}/variants
     * Returns size/stock variants for a product
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<?> getProductVariants(@PathVariable Long id) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        List<SizeDto> sizeDtos = product.getVariants().stream()
                .filter(v -> v != null && v.getSize() != null)
                .map(v -> SizeDto.builder()
                        .id(v.getSize().getId())
                        .name(v.getSize().getName())
                        .quantity(v.getQuantity())
                        .sku(v.getSku())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(sizeDtos);
    }

    /**
     * GET /api/products/new?limit={n}
     * Returns newest products
     */
    @GetMapping("/new")
    public ResponseEntity<?> getNewProducts(@RequestParam(defaultValue = "8") int limit) {
        List<Product> newProducts = productRepository.findTopNByOrderByIdDesc(limit);
        List<ProductDto> productDtos = newProducts.stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(productDtos);
    }

    /**
     * GET /api/categories/sports
     * Returns distinct sports list
     */
    @GetMapping("/categories/sports")
    public ResponseEntity<?> getSports() {
        List<String> sports = productRepository.findAllDistinctSports();
        return ResponseEntity.ok(sports);
    }

    /**
     * GET /api/categories/brands
     * Returns distinct brands list
     */
    @GetMapping("/categories/brands")
    public ResponseEntity<?> getBrands() {
        List<String> brands = productRepository.findAllDistinctBrands();
        return ResponseEntity.ok(brands);
    }

    // ========== Helper Methods ==========

    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice() != null ? product.getPrice().doubleValue() : null)
                .image(product.getImageUrl())
                .sport(product.getSport())
                .brand(product.getBrand())
                .category(product.getCategory())
                .viewCount(product.getViewCount())
                .build();
    }

    private Integer calculateTotalStock(List<ProductVariant> variants) {
        if (variants == null) return 0;
        return variants.stream()
                .filter(v -> v != null && v.getQuantity() != null)
                .mapToInt(ProductVariant::getQuantity)
                .sum();
    }
}
