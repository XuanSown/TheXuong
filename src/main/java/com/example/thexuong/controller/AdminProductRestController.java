package com.example.thexuong.controller;

import com.example.thexuong.dto.admin.AdminProductDto;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.SizeRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin REST API for Product Management (Vue frontend consumption).
 * Base path: /api/v1/admin/products
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductRestController {

    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * GET /api/admin/products
     * Query params: page, size, keyword
     */
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productsPage;

        if (keyword != null && !keyword.isEmpty()) {
            productsPage = productRepository.findByNameContaining(keyword, pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        List<AdminProductDto> productDtos = productsPage.getContent().stream()
                .map(this::toAdminProductDto)
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
     * POST /api/admin/products
     * Create new product with variants
     * Body: { name, description, price, imageUrl, sport, brand, category, sizeQuantities: {sizeName: quantity} }
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody AdminProductDto dto) {
        try {
            Product product = new Product();
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setImageUrl(dto.getImageUrl());
            product.setSport(dto.getSport());
            product.setBrand(dto.getBrand());
            product.setCategory(dto.getCategory());
            product.setViewCount(0);

            Product savedProduct = productRepository.save(product);

            // Save variants if provided
            if (dto.getSizeQuantities() != null) {
                for (Map.Entry<String, Integer> entry : dto.getSizeQuantities().entrySet()) {
                    String sizeName = entry.getKey();
                    Integer quantity = entry.getValue();

                    if (quantity == null || quantity <= 0) continue;

                    // Find or create Size
                    Size size = sizeRepository.findByName(sizeName)
                            .orElseGet(() -> {
                                Size s = new Size();
                                s.setName(sizeName);
                                return sizeRepository.save(s);
                            });

                    ProductVariant variant = ProductVariant.builder()
                            .product(savedProduct)
                            .size(size)
                            .quantity(quantity)
                            .sku("SKU-" + savedProduct.getId() + "-" + size.getId() + "-" + System.currentTimeMillis())
                            .build();
                    productVariantRepository.save(variant);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Tạo sản phẩm thành công", "product", toAdminProductDto(savedProduct)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/products/{id}
     * Update product and variants
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody AdminProductDto dto) {

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setImageUrl(dto.getImageUrl());
            product.setSport(dto.getSport());
            product.setBrand(dto.getBrand());
            product.setCategory(dto.getCategory());

            Product savedProduct = productRepository.save(product);

            // Clear existing variants and create new ones from sizeQuantities
            if (dto.getSizeQuantities() != null) {
                // Delete existing variants
                productVariantRepository.deleteByProductId(id);

                // Create new variants
                for (Map.Entry<String, Integer> entry : dto.getSizeQuantities().entrySet()) {
                    String sizeName = entry.getKey();
                    Integer quantity = entry.getValue();

                    if (quantity == null || quantity <= 0) continue;

                    Size size = sizeRepository.findByName(sizeName)
                            .orElseGet(() -> {
                                Size s = new Size();
                                s.setName(sizeName);
                                return sizeRepository.save(s);
                            });

                    ProductVariant variant = ProductVariant.builder()
                            .product(savedProduct)
                            .size(size)
                            .quantity(quantity)
                            .sku("SKU-" + savedProduct.getId() + "-" + size.getId() + "-" + System.currentTimeMillis())
                            .build();
                    productVariantRepository.save(variant);
                }
            }

            return ResponseEntity.ok(Map.of("message", "Cập nhật sản phẩm thành công", "product", toAdminProductDto(savedProduct)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

            // Delete variants first
            productVariantRepository.deleteByProductId(id);

            // Delete product
            productRepository.delete(product);

            return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== Helper Methods ==========

    private AdminProductDto toAdminProductDto(Product product) {
        return AdminProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .sport(product.getSport())
                .brand(product.getBrand())
                .category(product.getCategory())
                .viewCount(product.getViewCount())
                .sizeQuantities(null) // Not included in list view
                .build();
    }
}
