package com.example.thexuong.controller.api;

import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryRestController {

    private final ProductRepository productRepository;

    // GET /api/v1/categories?all=true - List all categories
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<String> categories = productRepository.findAllDistinctCategories();
        AtomicLong counter = new AtomicLong(1);
        List<CategoryDto> result = categories.stream()
                .map(name -> new CategoryDto(counter.getAndIncrement(), name))
                .toList();
        return ResponseEntity.ok(result);
    }

    // GET /api/v1/categories/sports - List all sports
    @GetMapping("/sports")
    public ResponseEntity<String[]> getSports() {
        List<String> sports = productRepository.findAllDistinctSports();
        return ResponseEntity.ok(sports.toArray(new String[0]));
    }

    // GET /api/v1/categories/brands - List all brands
    @GetMapping("/brands")
    public ResponseEntity<String[]> getBrands() {
        List<String> brands = productRepository.findAllDistinctBrands();
        return ResponseEntity.ok(brands.toArray(new String[0]));
    }

    public record CategoryDto(Long id, String name) {}
}
