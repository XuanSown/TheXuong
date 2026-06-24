package com.example.thexuong.service;

import com.example.thexuong.dto.VoucherCreateRequest;
import com.example.thexuong.dto.VoucherUpdateRequest;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Server-side validation cho Voucher CRUD.
 * Theo ADMIN_VOUCHER_REQUIREMENTS.md → Validation Rules.
 *
 * Rule chính:
 * - Code format: TX-[A-HJ-NP-RT-Z0-9]{6} (exclude 0,O,1,I,L)
 * - Discount: phải thuộc 1 trong 6 mệnh giá [10k,20k,50k,100k,200k,500k]
 * - Points: 1..50, và phải = discount/10000 (1:1 economics — Option A theo requirement)
 * - Min order >= discount
 * - Status hợp lệ: ACTIVE / LOCKED / EXPIRED
 * - Status LOCKED/EXPIRED trong update phải có adminNote
 */
@Component
@RequiredArgsConstructor
public class VoucherValidator {

    private static final Set<BigDecimal> VALID_DISCOUNTS = Set.of(
            new BigDecimal("10000"),
            new BigDecimal("20000"),
            new BigDecimal("50000"),
            new BigDecimal("100000"),
            new BigDecimal("200000"),
            new BigDecimal("500000")
    );

    private static final List<String> VALID_STATUSES = List.of("ACTIVE", "LOCKED", "EXPIRED");

    private final VoucherRepository voucherRepository;

    /**
     * Validate request CREATE.
     * Throw IllegalArgumentException với message thân thiện nếu fail.
     * GlobalExceptionHandler sẽ bắt → trả 400 Bad Request.
     */
    public void validateCreate(VoucherCreateRequest req) {
        // 1. Discount amount
        if (!VALID_DISCOUNTS.contains(req.getDiscountAmount())) {
            throw new IllegalArgumentException(
                    "Mệnh giá phải là một trong: 10k, 20k, 50k, 100k, 200k, 500k (VNĐ)");
        }

        // 2. Points consistency: requiredPoints = discount / 10000 (Option A: strict)
        int expectedPoints = req.getDiscountAmount().intValue() / 10000;
        if (req.getRequiredPoints() != expectedPoints) {
            throw new IllegalArgumentException(
                    "Điểm cần phải là " + expectedPoints + " cho mệnh giá " + req.getDiscountAmount().intValue() + "đ");
        }

        // 3. Min order >= discount (mặc định 0 nếu null)
        BigDecimal minOrder = req.getMinOrderAmount() != null ? req.getMinOrderAmount() : BigDecimal.ZERO;
        if (minOrder.compareTo(req.getDiscountAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Đơn tối thiểu phải >= mệnh giá giảm (" + req.getDiscountAmount() + "đ)");
        }

        // 4. Code uniqueness (nếu admin nhập code)
        if (req.getCode() != null && !req.getCode().isBlank()) {
            if (voucherRepository.existsByCode(req.getCode())) {
                throw new IllegalArgumentException(
                        "Mã voucher '" + req.getCode() + "' đã tồn tại");
            }
        }

        // 5. Status (default ACTIVE nếu null/blank)
        if (req.getStatus() != null && !req.getStatus().isBlank()
                && !VALID_STATUSES.contains(req.getStatus())) {
            throw new IllegalArgumentException(
                    "Status phải là một trong: " + String.join(", ", VALID_STATUSES));
        }
    }

    /**
     * Validate request UPDATE (partial).
     * Chỉ validate field != null.
     */
    public void validateUpdate(Long voucherId, VoucherUpdateRequest req) {
        Voucher existing = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + voucherId));

        // 1. Discount amount (nếu update)
        if (req.getDiscountAmount() != null) {
            if (!VALID_DISCOUNTS.contains(req.getDiscountAmount())) {
                throw new IllegalArgumentException(
                        "Mệnh giá phải là một trong: 10k, 20k, 50k, 100k, 200k, 500k (VNĐ)");
            }

            // Points consistency (chỉ check nếu cả 2 field được update)
            if (req.getRequiredPoints() != null) {
                int expectedPoints = req.getDiscountAmount().intValue() / 10000;
                if (req.getRequiredPoints() != expectedPoints) {
                    throw new IllegalArgumentException(
                            "Điểm cần phải là " + expectedPoints + " cho mệnh giá " + req.getDiscountAmount().intValue() + "đ");
                }
            }
        }

        // 2. Min order >= discount (dùng discount mới nếu update, ngược lại dùng discount cũ)
        if (req.getMinOrderAmount() != null) {
            BigDecimal effectiveDiscount = req.getDiscountAmount() != null
                    ? req.getDiscountAmount()
                    : existing.getDiscountAmount();
            if (req.getMinOrderAmount().compareTo(effectiveDiscount) < 0) {
                throw new IllegalArgumentException(
                        "Đơn tối thiểu phải >= mệnh giá giảm (" + effectiveDiscount + "đ)");
            }
        }

        // 3. Status validation + require adminNote cho LOCKED/EXPIRED
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            if (!VALID_STATUSES.contains(req.getStatus())) {
                throw new IllegalArgumentException(
                        "Status phải là một trong: " + String.join(", ", VALID_STATUSES));
            }
            if (("LOCKED".equals(req.getStatus()) || "EXPIRED".equals(req.getStatus()))
                    && (req.getAdminNote() == null || req.getAdminNote().isBlank())) {
                throw new IllegalArgumentException(
                        "Bắt buộc nhập ghi chú khi chuyển status sang " + req.getStatus());
            }
        }
    }

    /**
     * Generate mã voucher duy nhất: TX- + 6 random từ 29 chars (exclude 0,O,1,I,L).
     * Retry tối đa 5 lần nếu trùng.
     */
    public String generateUniqueCode() {
        final String allowed = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        final java.security.SecureRandom rand = new java.security.SecureRandom();
        for (int attempt = 0; attempt < 5; attempt++) {
            StringBuilder sb = new StringBuilder("TX-");
            for (int i = 0; i < 6; i++) {
                sb.append(allowed.charAt(rand.nextInt(allowed.length())));
            }
            String code = sb.toString();
            if (!voucherRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Không thể generate mã voucher duy nhất sau 5 lần thử. Vui lòng thử lại.");
    }
}
