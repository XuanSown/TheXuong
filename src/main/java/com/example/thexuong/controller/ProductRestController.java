package com.example.thexuong.controller;

import com.example.thexuong.dto.ProductDto;
import com.example.thexuong.dto.SizeDto;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductImage;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.ProductImageRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for Products (Vue frontend consumption).
 * Base path: /api/v1/products
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final ProductVariantRepository productVariantRepository;

	/**
	 * GET /api/v1/products
	 * Query params: page, size, keyword, sport, brand, sort
	 */
	@Transactional(readOnly = true)
	@GetMapping
	public ResponseEntity<?> getProducts(
			@RequestParam(defaultValue = "0") @Min(0) Integer page,
			@RequestParam(defaultValue = "12") @Min(1) @Max(100) Integer size,
			@RequestParam(required = false) @Pattern(regexp = "^.{0,100}$") String keyword,
			@RequestParam(required = false) @Pattern(regexp = "^.{0,50}$") String sport,
			@RequestParam(required = false) @Pattern(regexp = "^.{0,50}$") String brand,
			@RequestParam(defaultValue = "newest") @Pattern(regexp = "^(newest|price_asc|price_desc)$") String sort) {

		Sort sorting = Sort.by("id").descending();
		if ("price_asc".equals(sort)) {
			sorting = Sort.by("price").ascending();
		} else if ("price_desc".equals(sort)) {
			sorting = Sort.by("price").descending();
		}

		Pageable pageable = PageRequest.of(page, size, sorting);
		Page<Product> productsPage;

		// Chuẩn hóa empty -> null vì ":param IS NULL" chỉ khớp Java null, không khớp "".
		String kw = (keyword != null && !keyword.isEmpty()) ? keyword : null;
		String sp = (sport    != null && !sport.isEmpty())    ? sport    : null;
		String br = (brand    != null && !brand.isEmpty())    ? brand    : null;
		productsPage = productRepository.findByFilters(kw, sp, br, pageable);

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
	 * GET /api/v1/products/{id}
	 * Returns product detail with variants and images.
	 */
	@Transactional
	@GetMapping("/{id}")
	public ResponseEntity<?> getProduct(@PathVariable Long id) {
		Product product = productRepository.findByIdWithVariants(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

		product.setViewCount(product.getViewCount() + 1);
		productRepository.save(product);

		ProductDto productDto = toProductDto(product);
		productDto.setStockQuantity(calculateTotalStock(product.getVariants()));

		// Load images from ProductImage table
		List<ProductImage> productImages = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
		List<String> imageUrls = productImages.stream()
				.map(ProductImage::getImageUrl)
				.collect(Collectors.toList());
		productDto.setImages(imageUrls);

		return ResponseEntity.ok(productDto);
	}

	/**
	 * GET /api/v1/products/{id}/variants
	 * Returns size/stock variants for a product
	 */
	@GetMapping("/{id}/variants")
	public ResponseEntity<?> getProductVariants(@PathVariable Long id) {
		Product product = productRepository.findByIdWithVariants(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

		List<SizeDto> sizeDtos = product.getVariants().stream()
				.filter(v -> v != null && v.getSize() != null)
				.map(v -> SizeDto.builder()
						.id(v.getId())
						.name(v.getSize().getName())
						.quantity(v.getQuantity())
						.sku(v.getSku())
						.build())
				.collect(Collectors.toList());

		return ResponseEntity.ok(sizeDtos);
	}

	/**
	 * GET /api/v1/products/new?limit={n}
	 * Returns newest products
	 */
	@Transactional(readOnly = true)
	@GetMapping("/new")
	public ResponseEntity<?> getNewProducts(@RequestParam(defaultValue = "8") @Min(1) @Max(50) Integer limit) {
		Pageable pageable = PageRequest.of(0, limit);
		List<Product> newProducts = productRepository.findAllByOrderByIdDesc(pageable);
		List<ProductDto> productDtos = newProducts.stream()
				.map(this::toProductDto)
				.collect(Collectors.toList());

		return ResponseEntity.ok(productDtos);
	}

	/**
	 * GET /api/v1/categories/sports
	 * Returns distinct sports list
	 */
	@GetMapping("/categories/sports")
	public ResponseEntity<?> getSports() {
		List<String> sports = productRepository.findAllDistinctSports();
		return ResponseEntity.ok(sports);
	}

	/**
	 * GET /api/v1/categories/brands
	 * Returns distinct brands list
	 */
	@GetMapping("/categories/brands")
	public ResponseEntity<?> getBrands() {
		List<String> brands = productRepository.findAllDistinctBrands();
		return ResponseEntity.ok(brands);
	}

	// ========== Helper Methods ==========

	private ProductDto toProductDto(Product product) {
		// Load images from ProductImage table
		List<String> imageUrls = productImageRepository
				.findByProductIdOrderBySortOrderAsc(product.getId())
				.stream()
				.map(ProductImage::getImageUrl)
				.collect(Collectors.toList());

		// Load variants explicitly to avoid LazyInitializationException
		// (product.getVariants() is LAZY and session may be closed)
		List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

		// Load sizes from variants
		List<SizeDto> sizeDtos = variants != null
				? variants.stream()
						.filter(v -> v != null && v.getSize() != null)
						.map(v -> SizeDto.builder()
								.id(v.getId())
								.name(v.getSize().getName())
								.quantity(v.getQuantity())
								.sku(v.getSku())
								.build())
						.collect(Collectors.toList())
				: List.of();

		ProductDto dto = ProductDto.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.price(product.getPrice() != null ? product.getPrice().doubleValue() : null)
				.imageUrl(product.getImageUrl())
				.images(imageUrls.isEmpty() ? null : imageUrls)
				.sport(product.getSport() != null ? product.getSport().getName() : null)
				.brand(product.getBrand() != null ? product.getBrand().getName() : null)
				.category(product.getCategory() != null ? product.getCategory().getName() : null)
				.viewCount(product.getViewCount())
				.sizes(sizeDtos)
				.build();

		dto.setStockQuantity(calculateTotalStock(variants));
		return dto;
	}

	private Integer calculateTotalStock(List<ProductVariant> variants) {
		if (variants == null) return 0;
		return variants.stream()
				.filter(v -> v != null && v.getQuantity() != null)
				.mapToInt(ProductVariant::getQuantity)
				.sum();
	}
}
