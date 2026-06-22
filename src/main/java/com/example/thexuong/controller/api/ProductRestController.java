package com.example.thexuong.controller.api;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final SizeRepository sizeRepository;

    // DTOs
    public record ProductResponse(
            Long id,
            String name,
            Double price,
            String imageUrl,
            String sport,
            String category,
            String brand,
            Integer viewCount,
            List<VariantResponse> variants
    ) {}

    public record VariantResponse(
            Long id,
            String size,
            Integer quantity
    ) {}

    public record ProductListResponse(
            List<ProductResponse> content,
            long totalElements,
            int totalPages,
            int size,
            int number
    ) {}

    // GET /api/v1/products - List with filters
    @GetMapping
    public ProductListResponse getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
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

        List<ProductResponse> content = productsPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new ProductListResponse(
                content,
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.getSize(),
                productsPage.getNumber()
        );
    }

    // GET /api/v1/products/{id} - Detail with variants
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        Product product = productRepository.findByIdWithVariants(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        // Increment view count
        if (product.getViewCount() == null) {
            product.setViewCount(0);
        }
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        return toResponse(product);
    }

    // GET /api/v1/products/new - New arrivals
    @GetMapping("/new")
    public List<ProductResponse> getNewProducts(@RequestParam(defaultValue = "8") int limit) {
        List<Product> products = productRepository.findTopNByOrderByIdDesc(limit);
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse toResponse(Product product) {
        List<VariantResponse> variants = product.getVariants().stream()
                .filter(v -> v != null && v.getSize() != null)
                .map(v -> new VariantResponse(
                        v.getId(),
                        v.getSize().getName(),
                        v.getQuantity()
                ))
                .collect(Collectors.toList());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice() != null ? product.getPrice().doubleValue() : null,
                product.getImageUrl(),
                product.getSport(),
                product.getCategory(),
                product.getBrand(),
                product.getViewCount(),
                variants
        );
    }
}
