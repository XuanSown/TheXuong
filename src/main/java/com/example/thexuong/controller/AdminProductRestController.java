package com.example.thexuong.controller;

import com.example.thexuong.dto.admin.AdminProductDto;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductImage;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.SizeCatalog;
import com.example.thexuong.entity.SizeType;
import com.example.thexuong.repository.ProductImageRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.SizeCatalogRepository;
import com.example.thexuong.repository.SizeTypeRepository;
import com.example.thexuong.repository.SportRepository;
import com.example.thexuong.repository.BrandRepository;
import com.example.thexuong.repository.CategoryRepository;
import com.example.thexuong.service.CloudflareR2Service;
import com.example.thexuong.service.SizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.example.thexuong.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/admin/products")
@Slf4j
@lombok.RequiredArgsConstructor
public class AdminProductRestController {

	private static final int MAX_IMAGES = 5;
	private static final int MIN_IMAGES = 1;

	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;
	private final ProductImageRepository productImageRepository;
	private final CloudflareR2Service r2Service;
	private final SizeService sizeService;
	private final SizeTypeRepository sizeTypeRepository;
	private final SizeCatalogRepository sizeCatalogRepository;
	private final SportRepository sportRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final AuditLogService auditLogService;
	private final ObjectMapper objectMapper;

	private String toJson(Object obj) {
		if (obj == null) return null;
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			log.error("Loi parse JSON audit log", e);
			return null;
		}
	}

	// ── List ────────────────────────────────────────────────

	@Transactional(readOnly = true)
	@GetMapping
	public ResponseEntity<?> getProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String keyword) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Product> productsPage;

		if (keyword != null && !keyword.isEmpty()) {
			productsPage = productRepository.findAllIncludingInactiveByNameContainingPageable(keyword, pageable);
		} else {
			productsPage = productRepository.findAllIncludingInactivePageable(pageable);
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

	@Transactional(readOnly = true)
	@GetMapping("/{id}")
	public ResponseEntity<?> getProduct(@PathVariable Long id) {
		Product product = productRepository.findByIdIncludingInactive(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

		AdminProductDto dto = toAdminProductDto(product);

		List<ProductVariant> variants = productVariantRepository.findByProductId(id);
		Map<String, Integer> sizeQuantities = new LinkedHashMap<>();
		for (ProductVariant v : variants) {
			if (v.getSize() != null) {
				sizeQuantities.put(v.getSize().getName(), v.getQuantity());
			}
		}
		dto.setSizeQuantities(sizeQuantities);

		List<ProductImage> productImages = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
		List<String> imageUrls = productImages.stream()
				.map(ProductImage::getImageUrl)
				.collect(Collectors.toList());
		dto.setImages(imageUrls);

		if (!imageUrls.isEmpty()) {
			dto.setImageUrl(imageUrls.get(0));
		}

		return ResponseEntity.ok(dto);
	}

	// ── Create ──────────────────────────────────────────────

	@Transactional
	@PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> createProduct(
			@RequestParam(value = "files", required = false) MultipartFile[] files,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "price", required = false) String priceStr,
			@RequestParam(value = "imageUrl", required = false, defaultValue = "") String imageUrl,
			@RequestParam(value = "sport", required = false, defaultValue = "") String sport,
			@RequestParam(value = "brand", required = false, defaultValue = "") String brand,
			@RequestParam(value = "category", required = false, defaultValue = "") String category,
			@RequestParam(value = "sizeQuantities", required = false) String sizeQuantitiesJson) {

		try {
			// Validate required fields
			if (!StringUtils.hasText(name)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Tên sản phẩm là bắt buộc"));
			}
			if (!StringUtils.hasText(description)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Mô tả sản phẩm là bắt buộc"));
			}
			if (!StringUtils.hasText(priceStr)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Giá sản phẩm là bắt buộc"));
			}

			Map<String, Integer> sizeQuantities = sizeService.parseSizeQuantities(sizeQuantitiesJson);

			List<String> uploadedUrls = new ArrayList<>();

			if (files != null && files.length > 0) {
				if (files.length > MAX_IMAGES) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Map.of("error", "Tối đa " + MAX_IMAGES + " ảnh cho mỗi sản phẩm"));
				}
				uploadedUrls.addAll(r2Service.uploadMultiple(files));
			}

			if (uploadedUrls.isEmpty() && StringUtils.hasText(imageUrl)) {
				uploadedUrls.add(imageUrl);
			}

			if (uploadedUrls.isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Sản phẩm cần có ít nhất " + MIN_IMAGES + " ảnh"));
			}
			if (uploadedUrls.size() > MAX_IMAGES) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Tối đa " + MAX_IMAGES + " ảnh cho mỗi sản phẩm"));
			}

			Product product = new Product();
			product.setName(name);
			product.setDescription(description);
			product.setPrice(new BigDecimal(priceStr));
			product.setSport(getSportEntity(sport));
			product.setBrand(getBrandEntity(brand));
			product.setCategory(getCategoryEntity(category));
			product.setViewCount(0);
			product.setImageUrl(uploadedUrls.get(0));

			Product savedProduct = productRepository.save(product);

			for (int i = 0; i < uploadedUrls.size(); i++) {
				ProductImage pi = new ProductImage(savedProduct.getId(), i, uploadedUrls.get(i));
				productImageRepository.save(pi);
			}

			if (sizeQuantities != null && !sizeQuantities.isEmpty()) {
				List<ProductVariant> variants = sizeService.createVariants(savedProduct, sizeQuantities);
				savedProduct.setVariants(variants);
			}

			auditLogService.logAction(
					"PRODUCT",
					"CREATE",
					String.valueOf(savedProduct.getId()),
					null,
					toJson(toAdminProductDto(savedProduct)),
					"Admin created product: " + savedProduct.getName()
			);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Tạo sản phẩm thành công", "product", toAdminProductDto(savedProduct)));
		} catch (Exception e) {
			log.error("Create product failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	// ── Update ──────────────────────────────────────────────

	@Transactional
	@PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> updateProduct(
			@PathVariable Long id,
			@RequestParam(value = "files", required = false) MultipartFile[] files,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "price", required = false) String priceStr,
			@RequestParam(value = "imageUrl", required = false, defaultValue = "") String imageUrl,
			@RequestParam(value = "sport", required = false, defaultValue = "") String sport,
			@RequestParam(value = "brand", required = false, defaultValue = "") String brand,
			@RequestParam(value = "category", required = false, defaultValue = "") String category,
			@RequestParam(value = "sizeQuantities", required = false) String sizeQuantitiesJson) {

		try {
			Product product = productRepository.findByIdIncludingInactive(id)
					.orElseThrow(() -> new RuntimeException("Khong tim thay san pham: " + id));

			AdminProductDto oldStateDto = toAdminProductDto(product);
			List<ProductVariant> oldVariants = productVariantRepository.findByProductId(id);
			Map<String, Integer> oldSizeQuantities = new LinkedHashMap<>();
			for (ProductVariant v : oldVariants) {
				if (v.getSize() != null) {
					oldSizeQuantities.put(v.getSize().getName(), v.getQuantity());
				}
			}
			oldStateDto.setSizeQuantities(oldSizeQuantities);

			// Validate required fields
			if (!StringUtils.hasText(name)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Tên sản phẩm là bắt buộc"));
			}
			if (!StringUtils.hasText(description)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Mô tả sản phẩm là bắt buộc"));
			}
			if (!StringUtils.hasText(priceStr)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Giá sản phẩm là bắt buộc"));
			}

			Map<String, Integer> sizeQuantities = sizeService.parseSizeQuantities(sizeQuantitiesJson);

			List<String> finalImageUrls;

			if (files != null && files.length > 0) {
				if (files.length > MAX_IMAGES) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Map.of("error", "Tối đa " + MAX_IMAGES + " ảnh cho mỗi sản phẩm"));
				}
				List<ProductImage> oldImages = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
				for (ProductImage oldImg : oldImages) {
					r2Service.deleteFile(oldImg.getImageUrl());
				}
				productImageRepository.deleteByProductId(id);
				finalImageUrls = r2Service.uploadMultiple(files);

			} else if (StringUtils.hasText(imageUrl) && !imageUrl.equals(product.getImageUrl())) {
				List<ProductImage> oldImages = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
				for (ProductImage oldImg : oldImages) {
					r2Service.deleteFile(oldImg.getImageUrl());
				}
				productImageRepository.deleteByProductId(id);
				finalImageUrls = new ArrayList<>();
				finalImageUrls.add(imageUrl);
			} else {
				List<ProductImage> existingImages = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
				finalImageUrls = existingImages.stream()
						.map(ProductImage::getImageUrl)
						.collect(Collectors.toList());
			}

			if (finalImageUrls.isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Sản phẩm cần có ít nhất " + MIN_IMAGES + " ảnh"));
			}

			product.setName(name);
			product.setDescription(description);
			product.setPrice(new BigDecimal(priceStr));
			product.setSport(getSportEntity(sport));
			product.setBrand(getBrandEntity(brand));
			product.setCategory(getCategoryEntity(category));
			product.setImageUrl(finalImageUrls.get(0));

			Product savedProduct = productRepository.save(product);

			for (int i = 0; i < finalImageUrls.size(); i++) {
				ProductImage pi = new ProductImage(savedProduct.getId(), i, finalImageUrls.get(i));
				productImageRepository.save(pi);
			}

			if (sizeQuantities != null && !sizeQuantities.isEmpty()) {
				// updateVariants da xoa het variants cu va tao moi trong 1 lan goi
				sizeService.updateVariants(id, sizeQuantities);
			}

			AdminProductDto newStateDto = toAdminProductDto(savedProduct);
			List<ProductVariant> newVariants = productVariantRepository.findByProductId(id);
			Map<String, Integer> newSizeQuantities = new LinkedHashMap<>();
			for (ProductVariant v : newVariants) {
				if (v.getSize() != null) {
					newSizeQuantities.put(v.getSize().getName(), v.getQuantity());
				}
			}
			newStateDto.setSizeQuantities(newSizeQuantities);

			auditLogService.logAction(
					"PRODUCT",
					"UPDATE",
					String.valueOf(savedProduct.getId()),
					toJson(oldStateDto),
					toJson(newStateDto),
					"Admin updated product: " + savedProduct.getName()
			);

			return ResponseEntity.ok(Map.of("message", "Cập nhật sản phẩm thành công", "product", toAdminProductDto(savedProduct)));
		} catch (Exception e) {
			log.error("Update product failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	// ── Delete ──────────────────────────────────────────────

	@Transactional
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
		try {
			Product product = productRepository.findByIdIncludingInactive(id)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

			List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
			for (ProductImage img : images) {
				r2Service.deleteFile(img.getImageUrl());
			}
			AdminProductDto oldStateDto = toAdminProductDto(product);
			List<ProductVariant> oldVariants = productVariantRepository.findByProductId(id);
			Map<String, Integer> oldSizeQuantities = new LinkedHashMap<>();
			for (ProductVariant v : oldVariants) {
				if (v.getSize() != null) {
					oldSizeQuantities.put(v.getSize().getName(), v.getQuantity());
				}
			}
			oldStateDto.setSizeQuantities(oldSizeQuantities);

			productImageRepository.deleteByProductId(id);
			productVariantRepository.deleteByProductId(id);
			productRepository.delete(product);

			auditLogService.logAction(
					"PRODUCT",
					"DELETE",
					String.valueOf(product.getId()),
					toJson(oldStateDto),
					null,
					"Admin deleted product: " + product.getName()
			);

			return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công"));
		} catch (Exception e) {
			log.error("Delete product failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	// ── Toggle Active ───────────────────────────────────────

	@Transactional
	@PatchMapping("/{id}/toggle-active")
	public ResponseEntity<?> toggleProductActive(@PathVariable Long id) {
		try {
			Product product = productRepository.findByIdIncludingInactive(id)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));
			
			AdminProductDto oldStateDto = toAdminProductDto(product);
			List<ProductVariant> oldVariants = productVariantRepository.findByProductId(id);
			Map<String, Integer> oldSizeQuantities = new LinkedHashMap<>();
			for (ProductVariant v : oldVariants) {
				if (v.getSize() != null) {
					oldSizeQuantities.put(v.getSize().getName(), v.getQuantity());
				}
			}
			oldStateDto.setSizeQuantities(oldSizeQuantities);

			product.setActive(!product.isActive());
			productRepository.save(product);

			AdminProductDto newStateDto = toAdminProductDto(product);
			newStateDto.setSizeQuantities(oldSizeQuantities);

			auditLogService.logAction(
					"PRODUCT",
					"TOGGLE_ACTIVE",
					String.valueOf(product.getId()),
					toJson(oldStateDto),
					toJson(newStateDto),
					"Admin toggled product active status to: " + product.isActive()
			);

			return ResponseEntity.ok(Map.of("message", "Đã " + (product.isActive() ? "hiện" : "ẩn") + " sản phẩm thành công"));
		} catch (Exception e) {
			log.error("Toggle product active failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	// ══════════════════════════════════════════════════════════
	// ── Size Catalog Management ──────────────────────────────
	// ══════════════════════════════════════════════════════════

	/**
	 * GET /admin/size-types
	 * Returns all active size types.
	 */
	@GetMapping("/size-types")
	public ResponseEntity<?> getAllSizeTypes() {
		try {
			List<SizeType> sizeTypes = sizeTypeRepository.findByActiveTrue();
			return ResponseEntity.ok(sizeTypes);
		} catch (Exception e) {
			log.error("Get size types failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * GET /admin/size-catalog
	 * Returns all active catalog items, optionally filtered by size type.
	 * Accepts either typeId (Long) or typeCode (String, e.g. "CLOTHING").
	 *
	 * @param typeId   optional size type ID to filter by
	 * @param typeCode optional size type code to filter by
	 */
	@GetMapping("/size-catalog")
	public ResponseEntity<?> getSizeCatalog(
			@RequestParam(required = false) Long typeId,
			@RequestParam(required = false) String typeCode) {
		try {
			List<SizeCatalog> catalogItems;
			if (typeId != null) {
				catalogItems = sizeCatalogRepository.findBySizeTypeIdAndActiveTrueOrderByDisplayOrderAsc(typeId);
			} else if (typeCode != null && !typeCode.isBlank()) {
				// Look up SizeType by code, then filter catalog
				SizeType sizeType = sizeTypeRepository.findByCode(typeCode.trim().toUpperCase())
						.orElse(null);
				if (sizeType != null) {
					catalogItems = sizeCatalogRepository
							.findBySizeTypeIdAndActiveTrueOrderByDisplayOrderAsc(sizeType.getId());
				} else {
					catalogItems = List.of();
				}
			} else {
				catalogItems = sizeCatalogRepository.findByActiveTrueOrderByDisplayOrderAsc();
			}
			return ResponseEntity.ok(catalogItems);
		} catch (Exception e) {
			log.error("Get size catalog failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * POST /admin/size-catalog
	 * Create a new size catalog item.
	 *
	 * Request body: { "sizeTypeId": Long, "name": String, "displayOrder": Integer }
	 */
	@Transactional
	@PostMapping("/size-catalog")
	public ResponseEntity<?> createSizeCatalogItem(@RequestBody Map<String, Object> body) {
		try {
			Long sizeTypeId = body.containsKey("sizeTypeId") ? Long.valueOf(body.get("sizeTypeId").toString()) : null;
			String name = (String) body.get("name");
			Integer displayOrder = body.containsKey("displayOrder") ? Integer.valueOf(body.get("displayOrder").toString()) : 0;

			if (sizeTypeId == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "sizeTypeId là bắt buộc"));
			}
			if (!StringUtils.hasText(name)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "name là bắt buộc"));
			}

			SizeType sizeType = sizeTypeRepository.findById(sizeTypeId)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy loại size: " + sizeTypeId));

			SizeCatalog catalogItem = new SizeCatalog();
			catalogItem.setSizeType(sizeType);
			catalogItem.setName(name.trim());
			catalogItem.setDisplayOrder(displayOrder != null ? displayOrder : 0);
			catalogItem.setActive(true);

			SizeCatalog saved = sizeCatalogRepository.save(catalogItem);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Tạo size catalog thành công", "item", saved));
		} catch (Exception e) {
			log.error("Create size catalog item failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * PUT /admin/size-catalog/{id}/toggle-active
	 * Toggle the active status of a size catalog item (soft delete / restore).
	 */
	@Transactional
	@PutMapping("/size-catalog/{id}/toggle-active")
	public ResponseEntity<?> toggleSizeCatalogActive(@PathVariable Long id) {
		try {
			SizeCatalog catalogItem = sizeCatalogRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy size catalog: " + id));

			catalogItem.setActive(!catalogItem.getActive());
			sizeCatalogRepository.save(catalogItem);

			String status = catalogItem.getActive() ? "hiện" : "ẩn";
			return ResponseEntity.ok(Map.of("message", "Đã " + status + " size catalog thành công", "item", catalogItem));
		} catch (Exception e) {
			log.error("Toggle size catalog active failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * DELETE /admin/size-catalog/{id}
	 * Soft delete a size catalog item by setting active = false.
	 */
	@Transactional
	@DeleteMapping("/size-catalog/{id}")
	public ResponseEntity<?> deleteSizeCatalogItem(@PathVariable Long id) {
		try {
			SizeCatalog catalogItem = sizeCatalogRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy size catalog: " + id));

			catalogItem.setActive(false);
			sizeCatalogRepository.save(catalogItem);

			return ResponseEntity.ok(Map.of("message", "Xóa size catalog thành công"));
		} catch (Exception e) {
			log.error("Delete size catalog item failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage()));
		}
	}

	// ── Helpers ─────────────────────────────────────────────

	private AdminProductDto toAdminProductDto(Product product) {
		List<String> imageUrls = productImageRepository
				.findByProductIdOrderBySortOrderAsc(product.getId())
				.stream()
				.map(ProductImage::getImageUrl)
				.collect(Collectors.toList());

		return AdminProductDto.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.price(product.getPrice())
				.imageUrl(product.getImageUrl())
				.images(imageUrls)
				.sport(product.getSport() != null ? product.getSport().getName() : null)
				.brand(product.getBrand() != null ? product.getBrand().getName() : null)
				.category(product.getCategory() != null ? product.getCategory().getName() : null)
				.viewCount(product.getViewCount())
				.active(product.isActive())
				.sizeQuantities(null)
				.build();
	}
	private com.example.thexuong.entity.Sport getSportEntity(String str) {
		if (!StringUtils.hasText(str)) return null;
		try { return sportRepository.findById(Long.valueOf(str)).orElseGet(() -> sportRepository.findByName(str).orElse(null)); }
		catch (NumberFormatException e) { return sportRepository.findByName(str).orElse(null); }
	}

	private com.example.thexuong.entity.Brand getBrandEntity(String str) {
		if (!StringUtils.hasText(str)) return null;
		try { return brandRepository.findById(Long.valueOf(str)).orElseGet(() -> brandRepository.findByName(str).orElse(null)); }
		catch (NumberFormatException e) { return brandRepository.findByName(str).orElse(null); }
	}

	private com.example.thexuong.entity.Category getCategoryEntity(String str) {
		if (!StringUtils.hasText(str)) return null;
		try { return categoryRepository.findById(Long.valueOf(str)).orElseGet(() -> categoryRepository.findByName(str).orElse(null)); }
		catch (NumberFormatException e) { return categoryRepository.findByName(str).orElse(null); }
	}
}
