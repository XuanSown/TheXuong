package com.example.thexuong.service;

import com.example.thexuong.entity.PointTransaction;
import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserVoucher;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.exception.PointBalanceException;
import com.example.thexuong.exception.VoucherInvalidException;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.UserPointsRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.repository.UserVoucherRepository;
import com.example.thexuong.repository.VoucherRepository;
import com.example.thexuong.dto.VoucherCreateRequest;
import com.example.thexuong.dto.VoucherListResponse;
import com.example.thexuong.dto.VoucherResponse;
import com.example.thexuong.dto.VoucherUpdateRequest;
import com.example.thexuong.dto.BulkVoucherRequest;
import com.example.thexuong.dto.BulkVoucherResponse;
import com.example.thexuong.dto.VoucherStats;
import com.example.thexuong.enums.BulkAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service quản lý voucher catalog + redemption + apply.
 *
 * Lifecycle:
 * 1. Admin tạo Vouchers catalog (status=ACTIVE).
 * 2. User redeem → tạo UserVoucher (status=UNUSED, code=TX-XXXXXX unique, expires_at=+30 days).
 * 3. User dùng UserVoucher tại checkout → set status=USED, used_at, used_in_order_id.
 * 4. Cron expire daily → UNUSED + expires_at < now → set EXPIRED.
 *
 * Mã code:
 * - Voucher.code: "TX-CAT-100K" (catalog, admin nhìn).
 * - UserVoucher.code: "TX-ABCDEF" (DUY NHẤT user nhận, dùng khi checkout).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    /** Bảng chữ cái cho mã UserVoucher (loại 0/O/1/I/L dễ nhầm). */
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final UserPointsRepository userPointsRepository;
    private final PointTransactionRepository pointTransactionRepository;

    // ============================================================
    // Task 2.8: Generate unique code TX-XXXXXX
    // ============================================================

    /**
     * Sinh mã UserVoucher DUY NHẤT dạng TX-XXXXXX.
     * Thử tối đa 10 lần nếu trùng (collision rate rất thấp với 32^6 = ~1 tỷ combos).
     */
    public String generateUniqueCode() {
        log.debug("Generating unique voucher code");
        Random random = new Random();
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("TX-");
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (userVoucherRepository.findByCode(code).isEmpty()
                    && voucherRepository.findByCode(code).isEmpty()) {
                log.debug("Generated unique code: {} (attempt {})", code, attempt + 1);
                return code;
            }
        }
        throw new RuntimeException("Không thể sinh mã voucher unique sau 10 lần thử. Thử lại sau.");
    }

    // ============================================================
    // Task 2.9: Redeem voucher (user đổi điểm lấy voucher)
    // ============================================================

    /**
     * User đổi điểm lấy voucher từ catalog.
     * Flow:
     * 1. Check catalog ACTIVE
     * 2. Check user đủ điểm (pointService.spendPoints sẽ throw nếu thiếu)
     * 3. Check VIP-only (nếu có)
     * 4. Tạo UserVoucher với mã unique
     *
     * @return UserVoucher mới tạo
     */
    @Transactional
    public UserVoucher redeemVoucher(Long userId, Long voucherCatalogId) {
        log.debug("Redeeming voucher {} for user {}", voucherCatalogId, userId);
        Voucher catalog = voucherRepository.findById(voucherCatalogId)
                .orElseThrow(() -> new VoucherInvalidException("Voucher catalog không tồn tại."));

        if (catalog.getStatus() != Voucher.Status.ACTIVE) {
            throw new VoucherInvalidException("Voucher này hiện không khả dụng.");
        }

        // Check VIP-only (dựa trên role string của user)
        if (Boolean.TRUE.equals(catalog.getVipOnly())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new VoucherInvalidException("User không tồn tại."));
            // User giờ có String role (CUSTOMER/ADMIN/BOTH), không còn Set<Role>.
            boolean isVip = "VIP".equals(user.getRole()) || "BOTH".equals(user.getRole());
            if (!isVip) {
                throw new VoucherInvalidException("Voucher này chỉ dành cho khách hàng VIP.");
            }
        }

        // Trừ điểm (PointService.spendPoints sẽ check balance + throw PointBalanceException nếu thiếu)
        spendPointsForVoucher(userId, catalog.getRequiredPoints(),
                "Đổi voucher " + catalog.getCode() + " (giảm " + catalog.getDiscountAmount() + "đ)");

        // Tạo UserVoucher
        LocalDateTime now = LocalDateTime.now();
        UserVoucher userVoucher = UserVoucher.builder()
                .userId(userId)
                .voucherId(catalog.getId())
                .code(generateUniqueCode())
                .status(UserVoucher.Status.UNUSED)
                .issuedAt(now)
                .expiresAt(now.plusDays(30))
                .build();
        UserVoucher saved = userVoucherRepository.save(userVoucher);
        log.info("Voucher redeemed: user={}, voucherCatalogId={}, userVoucherCode={}",
                userId, voucherCatalogId, saved.getCode());
        return saved;
    }

    public UserVoucher issueVoucherToUser(Long voucherCatalogId, Long userId) {
        log.debug("Issuing voucher {} for user {}", voucherCatalogId, userId);
        Voucher catalog = voucherRepository.findById(voucherCatalogId)
                .orElseThrow(() -> new VoucherInvalidException("Voucher catalog không tồn tại."));

        if (catalog.getStatus() != Voucher.Status.ACTIVE) {
            throw new VoucherInvalidException("Voucher này hiện không khả dụng.");
        }

        // Tạo UserVoucher (không trừ điểm vì đây là phần thưởng)
        LocalDateTime now = LocalDateTime.now();
        UserVoucher userVoucher = UserVoucher.builder()
                .userId(userId)
                .voucherId(catalog.getId())
                .code(generateUniqueCode())
                .status(UserVoucher.Status.UNUSED)
                .issuedAt(now)
                .expiresAt(now.plusDays(30))
                .build();
        UserVoucher saved = userVoucherRepository.save(userVoucher);
        log.info("Voucher issued as reward: user={}, voucherCatalogId={}, userVoucherCode={}",
                userId, voucherCatalogId, saved.getCode());
        return saved;
    }

    /**
     * Helper: trừ điểm và ghi PointTransaction SPEND.
     * Dùng repository trực tiếp (không qua PointService) để tránh dependency cycle.
     * Logic giống PointService.spendPoints nhưng không cần check tier.
     */
    private int spendPointsForVoucher(Long userId, int points, String note) {
        if (points <= 0) {
            throw new IllegalArgumentException("Số điểm tiêu phải > 0");
        }
        var userPoints = userPointsRepository.findByUserId(userId)
                .orElseThrow(() -> new PointBalanceException("Bạn chưa có điểm thưởng."));

        if (userPoints.getCurrentPoints() < points) {
            throw new PointBalanceException(
                    "Số dư không đủ. Bạn có " + userPoints.getCurrentPoints()
                            + " điểm, cần " + points + " điểm.");
        }

        userPoints.setCurrentPoints(userPoints.getCurrentPoints() - points);
        userPoints.setTotalSpent(userPoints.getTotalSpent() + points);
        userPoints.setLastActivityAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);

        PointTransaction tx = PointTransaction.builder()
                .userId(userId)
                .type(PointTransaction.Type.SPEND)
                .points(-points)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();
        pointTransactionRepository.save(tx);

        return userPoints.getCurrentPoints();
    }

    // ============================================================
    // Task 2.10: Validate + apply voucher tại checkout
    // ============================================================

    /**
     * Validate mã UserVoucher trước khi áp vào đơn.
     * Trả về discount amount nếu hợp lệ, throw VoucherInvalidException nếu không.
     *
     * @param code         mã UserVoucher (TX-XXXXXX)
     * @param userId       user hiện tại
     * @param orderAmount  tổng tiền đơn hàng (chưa áp voucher)
     * @return discount amount (VND)
     */
    @Transactional(readOnly = true)
    public BigDecimal validateAndGetDiscount(String code, Long userId, BigDecimal orderAmount) {
        UserVoucher uv = userVoucherRepository.findByCode(code)
                .orElseThrow(() -> new VoucherInvalidException("Mã voucher không tồn tại."));

        if (!uv.getUserId().equals(userId)) {
            throw new VoucherInvalidException("Voucher này không thuộc tài khoản của anh/chị.");
        }

        if (uv.getStatus() == UserVoucher.Status.USED) {
            throw new VoucherInvalidException("Voucher này đã được sử dụng.");
        }
        if (uv.getStatus() == UserVoucher.Status.EXPIRED) {
            throw new VoucherInvalidException("Voucher này đã hết hạn.");
        }
        if (uv.getExpiresAt() != null && uv.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VoucherInvalidException("Voucher này đã quá hạn sử dụng.");
        }

        Voucher catalog = voucherRepository.findById(uv.getVoucherId())
                .orElseThrow(() -> new VoucherInvalidException("Voucher catalog không tồn tại."));

        // Check min_order_amount
        if (catalog.getMinOrderAmount() != null && orderAmount.compareTo(catalog.getMinOrderAmount()) < 0) {
            throw new VoucherInvalidException(
                    "Đơn hàng phải tối thiểu " + catalog.getMinOrderAmount() + "đ để dùng voucher này.");
        }

        return catalog.getDiscountAmount();
    }

    // ============================================================
    // Task 2.11: Mark voucher as USED sau khi đơn CONFIRMED
    // ============================================================

    /**
     * Đánh dấu UserVoucher đã dùng. Gọi từ OrderService.vnpayReturn (khi đơn CONFIRMED).
     *
     * @param code    mã UserVoucher
     * @param orderId order vừa thanh toán
     */
    @Transactional
    public void markAsUsed(String code, Long orderId) {
        if (code == null || code.isBlank()) return;

        log.debug("Marking voucher {} as used for order {}", code, orderId);
        UserVoucher uv = userVoucherRepository.findByCode(code)
                .orElseThrow(() -> new VoucherInvalidException("Mã voucher không tồn tại: " + code));

        if (uv.getStatus() != UserVoucher.Status.UNUSED) {
            throw new VoucherInvalidException("Voucher này không hợp lệ hoặc đã được sử dụng.");
        }

        uv.setStatus(UserVoucher.Status.USED);
        uv.setUsedAt(LocalDateTime.now());
        uv.setUsedInOrderId(orderId);
        userVoucherRepository.save(uv);
        log.info("Voucher {} marked as used for order {}", code, orderId);
    }

    /**
     * Phục hồi voucher đã dùng (do đơn bị hủy/hoàn)
     */
    @Transactional
    public void restoreVoucher(String code) {
        if (code == null || code.isBlank()) return;
        userVoucherRepository.findByCode(code).ifPresent(uv -> {
            if (uv.getStatus() == UserVoucher.Status.USED) {
                uv.setStatus(UserVoucher.Status.UNUSED);
                uv.setUsedAt(null);
                uv.setUsedInOrderId(null);
                userVoucherRepository.save(uv);
                log.info("Voucher {} restored", code);
            }
        });
    }

    // ============================================================
    // Task 2.11 (continued): Cron expire vouchers
    // ============================================================

    /**
     * Expire voucher UNUSED quá hạn. Được gọi bởi VoucherExpireJob (Batch 5).
     */
    @Transactional
    public int expireOldVouchers(LocalDateTime now) {
        log.debug("Expiring old unused vouchers as of {}", now);
        List<UserVoucher> expired = userVoucherRepository.findExpiredUnusedVouchers(now);
        for (UserVoucher uv : expired) {
            uv.setStatus(UserVoucher.Status.EXPIRED);
            userVoucherRepository.save(uv);
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} vouchers", expired.size());
        }
        return expired.size();
    }

    // ============================================================
    // Query helpers
    // ============================================================

    public List<Voucher> getActiveCatalog() {
        return voucherRepository.findAllByStatus(Voucher.Status.ACTIVE);
    }

    public List<UserVoucher> getUserVouchers(Long userId) {
        return userVoucherRepository.findByUserIdOrderByIssuedAtDesc(userId);
    }

    public List<UserVoucher> getUserVouchersByStatus(Long userId, UserVoucher.Status status) {
        return userVoucherRepository.findByUserIdAndStatus(userId, status);
    }

    // ============================================================
    // Task 2.14-2.17: Admin REST API endpoints
    // ============================================================

    /**
     * GET /api/admin/loyalty/vouchers (paginated + filter)
     */
    public VoucherListResponse getVouchers(Integer page, Integer size, String search,
                                           String status, Boolean vipOnly, Integer minPoints,
                                           Integer maxPoints, String sortBy, String sortDir) {
        // TODO: Implement full pagination + filtering (simplified for now)
        List<Voucher> all = voucherRepository.findAll();
        long total = all.size();

        // Compute claimed count for each voucher
        List<VoucherResponse> responses = all.stream()
                .map(v -> VoucherResponse.from(v,
                        (int) userVoucherRepository.countByVoucherId(v.getId())))
                .toList();

        return VoucherListResponse.builder()
                .vouchers(responses)
                .total(total)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    /**
     * GET /api/admin/loyalty/vouchers/{id}
     */
    public VoucherResponse getVoucher(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + id));
        int claimed = (int) userVoucherRepository.countByVoucherId(v.getId());
        return VoucherResponse.from(v, claimed);
    }

    /**
     * POST /api/admin/loyalty/vouchers
     */
    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request, String adminUsername) {
        // Validate code unique if provided
        String code = request.getCode();
        if (code != null && !code.isBlank()) {
            if (voucherRepository.existsByCode(code)) {
                throw new RuntimeException("Mã voucher đã tồn tại: " + code);
            }
        } else {
            // Auto-generate code: TX-CAT-XXXX (simplified)
            code = "TX-CAT-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            while (voucherRepository.existsByCode(code)) {
                code = "TX-CAT-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            }
        }

        Voucher voucher = Voucher.builder()
                .code(code)
                .discountAmount(request.getDiscountAmount())
                .requiredPoints(request.getRequiredPoints())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .applicableCategoryIds(request.getApplicableCategoryIds() != null ?
                        request.getApplicableCategoryIds().toString() : null)
                .applicableProductIds(request.getApplicableProductIds() != null ?
                        request.getApplicableProductIds().toString() : null)
                .vipOnly(Boolean.TRUE.equals(request.getVipOnly()))
                .status(Voucher.Status.valueOf(request.getStatus() != null ? request.getStatus() : "ACTIVE"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        voucher = voucherRepository.save(voucher);
        return VoucherResponse.from(voucher, 0);
    }

    /**
     * PUT /api/admin/loyalty/vouchers/{id}
     */
    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherUpdateRequest request, String adminUsername) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + id));

        // Update fields if not null
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
            voucher.setApplicableCategoryIds(request.getApplicableCategoryIds().toString());
        }
        if (request.getApplicableProductIds() != null) {
            voucher.setApplicableProductIds(request.getApplicableProductIds().toString());
        }
        if (request.getVipOnly() != null) {
            voucher.setVipOnly(request.getVipOnly());
        }
        if (request.getStatus() != null) {
            voucher.setStatus(Voucher.Status.valueOf(request.getStatus()));
        }
        if (request.getExpiresAt() != null) {
            // TODO: handle expiresAt if field added
        }

        voucher.setUpdatedAt(LocalDateTime.now());
        voucher = voucherRepository.save(voucher);

        int claimed = (int) userVoucherRepository.countByVoucherId(voucher.getId());
        return VoucherResponse.from(voucher, claimed);
    }

    /**
     * DELETE /api/admin/loyalty/vouchers/{id}
     */
    @Transactional
    public void deleteVoucher(Long id, String adminUsername) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + id));

        // Check if any user vouchers exist
        long claimedCount = userVoucherRepository.countByVoucherId(id);
        if (claimedCount > 0) {
            // Soft delete: set to EXPIRED
            v.setStatus(Voucher.Status.EXPIRED);
            voucherRepository.save(v);
        } else {
            // Hard delete
            voucherRepository.delete(v);
        }
    }

    /**
     * POST /api/admin/loyalty/vouchers/bulk
     */
    @Transactional
    public BulkVoucherResponse bulkAction(BulkVoucherRequest request, String adminUsername) {
        List<Long> ids = request.getIds();
        BulkAction action = request.getAction();
        int success = 0;
        int failure = 0;

        for (Long id : ids) {
            try {
                Voucher v = voucherRepository.findById(id).orElse(null);
                if (v == null) {
                    failure++;
                    continue;
                }

                switch (action) {
                    case LOCK -> v.setStatus(Voucher.Status.LOCKED);
                    case UNLOCK -> v.setStatus(Voucher.Status.ACTIVE);
                    case DELETE -> {
                        if (userVoucherRepository.countByVoucherId(id) > 0) {
                            v.setStatus(Voucher.Status.EXPIRED);
                        } else {
                            voucherRepository.delete(v);
                            success++;
                            continue;
                        }
                    }
                    case SET_VIP -> v.setVipOnly(Boolean.TRUE.equals(request.getValue()));
                    default -> throw new RuntimeException("Unknown action: " + action);
                }
                v.setUpdatedAt(LocalDateTime.now());
                voucherRepository.save(v);
                success++;
            } catch (Exception e) {
                failure++;
            }
        }

        return BulkVoucherResponse.builder()
                .successCount(success)
                .failureCount(failure)
                .build();
    }

    /**
     * GET /api/admin/loyalty/vouchers/stats
     */
    public VoucherStats getStats() {
        long total = voucherRepository.count();
        long active = voucherRepository.countByStatus(Voucher.Status.ACTIVE);
        long locked = voucherRepository.countByStatus(Voucher.Status.LOCKED);
        long expired = voucherRepository.countByStatus(Voucher.Status.EXPIRED);
        long vip = voucherRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getVipOnly()))
                .count();
        long totalClaimed = userVoucherRepository.count(); // all user vouchers

        return VoucherStats.builder()
                .totalVouchers((int) total)
                .activeVouchers((int) active)
                .lockedVouchers((int) locked)
                .expiredVouchers((int) expired)
                .vipVouchers((int) vip)
                .totalClaimed((int) totalClaimed)
                .byStatus(Map.of(
                        "ACTIVE", (int) active,
                        "LOCKED", (int) locked,
                        "EXPIRED", (int) expired
                ))
                .build();
    }
}
