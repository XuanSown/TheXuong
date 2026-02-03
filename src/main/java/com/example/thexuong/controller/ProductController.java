package com.example.thexuong.controller;

import com.example.thexuong.entity.Product;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;

    //1. api ds sp trang chu
    // GET: http://localhost:8080/api/products
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (minPrice != null && maxPrice != null) {
            return ResponseEntity.ok(productRepository.findByPriceBetween(minPrice, maxPrice, pageable));
        }

        if (minPrice != null) {
            return ResponseEntity.ok(productRepository.findByPriceGreaterThanEqual(minPrice, pageable));
        }
        return ResponseEntity.ok(productRepository.findAll(pageable));
    }

    //2. chi tiet' sp
    // GET: http://localhost:8080/api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    //3. api  them sp
    // POST: http://localhost:8080/api/products
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productRepository.save(product));
    }
}