package com.example.thexuong.controller;

import com.example.thexuong.dto.admin.AdminVoucherDto;
import com.example.thexuong.dto.BulkVoucherRequest;
import com.example.thexuong.dto.VoucherCreateRequest;
import com.example.thexuong.dto.VoucherUpdateRequest;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.UserVoucherRepository;
import com.example.thexuong.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

/**
 * Admin REST API for Voucher Management.
 * Base path: /api/v1/admin/loyalty/vouchers
 */
@RestController
@RequestMapping("/api/v1/admin/loyalty/vouchers")
@RequiredArgsConstructor
public class AdminVoucherRestController {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    /**
     * GET /api/v1/admin/loyalty/vouchers
     * Query params: page, size, search, status, vipOnly, minPoints, maxPoints
     */
    @GetMapping
    public ResponseEntity<?> getVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean vipOnly,
            @RequestParam(required = false) Integer minPoints,
            @RequestParam(required = false) Integer maxPoints) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> voucherPage = voucherRepository.findAll((root, query, cb) -> {
            // Build dynamic predicates
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String likeSearch = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("code")), likeSearch));
            }
            if (status != null && !status.isBlank() && !"all".equals(status)) {
                try {
                    Voucher.Status s = Voucher.Status.valueOf(status);
                    predicates.add(cb.equal(root.get("status"), s));
                } catch (IllegalArgumentException e) {
                    return cb.disjunction(); // empty result for invalid status
                }
            }
            if (vipOnly != null) {
                predicates.add(cb.equal(root.get("vipOnly"), vipOnly));
            }
            if (minPoints != null) {
                predicates.add(cb.ge(root.get("requiredPoints"), minPoints));
            }
            if (maxPoints != null) {
                predicates.add(cb.le(root.get("requiredPoints"), maxPoints));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        // Build DTOs with claimed count
        List<AdminVoucherDto> voucherDtos = voucherPage.getContent().stream()
                .map(v -> {
                    long claimedCount = userVoucherRepository.countByVoucherId(v.getId());
                    return AdminVoucherDto.fromEntity(v, claimedCount);
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("vouchers", voucherDtos);
        response.put("total", voucherPage.getTotalElements());
        response.put("page", page);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/loyalty/vouchers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVoucher(@PathVariable Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher: " + id));

        long claimedCount = userVoucherRepository.countByVoucherId(voucher.getId());
        AdminVoucherDto dto = AdminVoucherDto.fromEntity(voucher, claimedCount);

        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/v1/admin/loyalty/vouchers
     * Create new voucher
     */
    @PostMapping
    public ResponseEntity<?> createVoucher(@RequestBody VoucherCreateRequest request) {
        try {
            // Validate discount amount format (must be in 10k increments: 10k, 20k, 50k, 100k, 200k, 500k)
            if (request.getDiscountAmount() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Mệnh giá không được để trống"
                ));
            }
            int discount = request.getDiscountAmount().intValue();
            if (!isValidDiscountAmount(discount)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid discount amount. Must be 10000, 20000, 50000, 100000, 200000, or 500000."
                ));
            }

            // Validate points consistency: requiredPoints = discount / 10000
            int expectedPoints = discount / 10000;
            if (request.getRequiredPoints() == null || request.getRequiredPoints() != expectedPoints) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Required points must equal discountAmount / 10000 = " + expectedPoints
                ));
            }

            // Convert category/product IDs to JSON strings
            String categoryIdsJson = toJsonArray(request.getApplicableCategoryIds());
            String productIdsJson = toJsonArray(request.getApplicableProductIds());

            Voucher voucher = Voucher.builder()
                    .code(request.getCode()) // can be null for auto-generate
                    .discountAmount(request.getDiscountAmount())
                    .requiredPoints(request.getRequiredPoints())
                    .minOrderAmount(request.getMinOrderAmount())
                    .applicableCategoryIds(categoryIdsJson)
                    .applicableProductIds(productIdsJson)
                    .vipOnly(request.getVipOnly() != null ? request.getVipOnly() : false)
                    .status(Voucher.Status.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Auto-generate code if not provided
            if (voucher.getCode() == null || voucher.getCode().isBlank()) {
                voucher.setCode(generateUniqueVoucherCode());
            }

            Voucher saved = voucherRepository.save(voucher);

            long claimedCount = 0;
            AdminVoucherDto dto = AdminVoucherDto.fromEntity(saved, claimedCount);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Tạo voucher thành công",
                    "voucher", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/admin/loyalty/vouchers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVoucher(
            @PathVariable Long id,
            @RequestBody VoucherUpdateRequest request) {

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher: " + id));

        try {
            if (request.getDiscountAmount() != null) {
                voucher.setDiscountAmount(request.getDiscountAmount());
            }
            if (request.getRequiredPoints() != null) {
                voucher.setRequiredPoints(request.getRequiredPoints());
            }
            if (request.getMinOrderAmount() != null) {
                voucher.setMinOrderAmount(request.getMinOrderAmount());
            }
            if (request.getApplicableCategoryIds() != null) {
                voucher.setApplicableCategoryIds(toJsonArray(request.getApplicableCategoryIds()));
            }
            if (request.getApplicableProductIds() != null) {
                voucher.setApplicableProductIds(toJsonArray(request.getApplicableProductIds()));
            }
            if (request.getVipOnly() != null) {
                voucher.setVipOnly(request.getVipOnly());
            }
            if (request.getStatus() != null) {
                try {
                    voucher.setStatus(Voucher.Status.valueOf(request.getStatus()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
                }
            }
            voucher.setUpdatedAt(LocalDateTime.now());

            Voucher saved = voucherRepository.save(voucher);
            long claimedCount = userVoucherRepository.countByVoucherId(saved.getId());
            AdminVoucherDto dto = AdminVoucherDto.fromEntity(saved, claimedCount);

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật voucher thành công",
                    "voucher", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/admin/loyalty/vouchers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVoucher(@PathVariable Long id) {
        try {
            Voucher voucher = voucherRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher: " + id));

            // Check if voucher has been claimed
            long claimedCount = userVoucherRepository.countByVoucherId(id);
            if (claimedCount > 0) {
                // Soft delete: mark as EXPIRED instead of deleting
                voucher.setStatus(Voucher.Status.EXPIRED);
                voucher.setUpdatedAt(LocalDateTime.now());
                voucherRepository.save(voucher);
                return ResponseEntity.ok(Map.of(
                        "message", "Voucher đã được khóa (không xóa được vì đã có người đổi)"
                ));
            } else {
                // Hard delete if never claimed
                voucherRepository.delete(voucher);
                return ResponseEntity.ok(Map.of("message", "Xóa voucher thành công"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/admin/loyalty/vouchers/bulk
     * Bulk operations: LOCK, UNLOCK, DELETE, SET_VIP
     */
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkAction(@RequestBody BulkVoucherRequest request) {
        try {
            List<Integer> successes = new ArrayList<>();
            List<Map<String, Object>> failures = new ArrayList<>();

            for (Long id : request.getIds()) {
                try {
                    Voucher voucher = voucherRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy"));

                    switch (request.getAction()) {
                        case LOCK -> voucher.setStatus(Voucher.Status.LOCKED);
                        case UNLOCK -> voucher.setStatus(Voucher.Status.ACTIVE);
                        case SET_VIP -> voucher.setVipOnly(request.getValue() != null ? request.getValue() : true);
                        case DELETE -> {
                            if (userVoucherRepository.countByVoucherId(id) > 0) {
                                throw new Exception("Voucher đã được người dùng đổi, không thể xóa");
                            }
                            voucherRepository.delete(voucher);
                            continue; // skip save
                        }
                    }
                    voucher.setUpdatedAt(LocalDateTime.now());
                    voucherRepository.save(voucher);
                    successes.add(id.intValue());
                } catch (Exception e) {
                    failures.add(Map.of(
                            "id", id,
                            "error", e.getMessage()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "totalRequested", request.getIds().size(),
                    "successCount", successes.size(),
                    "failureCount", failures.size(),
                    "successes", successes,
                    "failures", failures
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/admin/loyalty/vouchers/stats
     * Voucher statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getVoucherStats() {
        long totalVouchers = voucherRepository.count();
        long activeVouchers = voucherRepository.countByStatus(Voucher.Status.ACTIVE);
        long lockedVouchers = voucherRepository.countByStatus(Voucher.Status.LOCKED);
        long expiredVouchers = voucherRepository.countByStatus(Voucher.Status.EXPIRED);

        // Total claimed vouchers (unique user-voucher pairs)
        long totalClaimed = userVoucherRepository.count();

        // VIP-only vouchers count
        long vipOnlyCount = voucherRepository.countByVipOnlyTrue();

        Map<String, Object> response = new HashMap<>();
        response.put("totalVouchers", totalVouchers);
        response.put("activeVouchers", activeVouchers);
        response.put("lockedVouchers", lockedVouchers);
        response.put("expiredVouchers", expiredVouchers);
        response.put("totalClaimed", totalClaimed);
        response.put("vipOnlyCount", vipOnlyCount);

        return ResponseEntity.ok(response);
    }

    // ========== Helper Methods ==========

    private String toJsonArray(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return ids.toString().replace(" ", "");
    }

    private boolean isValidDiscountAmount(int amount) {
        return amount == 10000 || amount == 20000 || amount == 50000 ||
               amount == 100000 || amount == 200000 || amount == 500000;
    }

    private String generateUniqueVoucherCode() {
        String code;
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        do {
            StringBuilder sb = new StringBuilder("TX-");
            for (int i = 0; i < 6; i++) {
                int idx = (int) (Math.random() * chars.length());
                sb.append(chars.charAt(idx));
            }
            code = sb.toString();
        } while (voucherRepository.existsByCode(code));
        return code;
    }
}
