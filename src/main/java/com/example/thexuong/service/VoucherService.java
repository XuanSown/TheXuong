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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class VoucherService {

    /** Bảng chữ cái cho mã UserVoucher (loại 0/O/1/I/L dễ nhầm). */
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    @Autowired
    private final VoucherRepository voucherRepository;
    @Autowired
    private final UserVoucherRepository userVoucherRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserPointsRepository userPointsRepository;
    @Autowired
    private final PointTransactionRepository pointTransactionRepository;

    // ============================================================
    // Task 2.8: Generate unique code TX-XXXXXX
    // ============================================================

    /**
     * Sinh mã UserVoucher DUY NHẤT dạng TX-XXXXXX.
     * Thử tối đa 10 lần nếu trùng (collision rate rất thấp với 32^6 = ~1 tỷ combos).
     */
    public String generateUniqueCode() {
        Random random = new Random();
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("TX-");
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (userVoucherRepository.findByCode(code).isEmpty()
                    && voucherRepository.findByCode(code).isEmpty()) {
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
        Voucher catalog = voucherRepository.findById(voucherCatalogId)
                .orElseThrow(() -> new VoucherInvalidException("Voucher catalog không tồn tại."));

        if (catalog.getStatus() != Voucher.Status.ACTIVE) {
            throw new VoucherInvalidException("Voucher này hiện không khả dụng.");
        }

        // Check VIP-only (dựa trên roles của user)
        if (Boolean.TRUE.equals(catalog.getVipOnly())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new VoucherInvalidException("User không tồn tại."));
            // TODO Batch 4: chuyển sang check tier_code thay vì check trực tiếp roles.
            // Hiện tại: VIP/BOTH = role.name() = "VIP" hoặc "BOTH".
            boolean isVip = user.getRoles() != null && user.getRoles().stream()
                    .anyMatch(r -> "VIP".equals(r.getName()) || "BOTH".equals(r.getName()));
            if (!isVip) {
                throw new VoucherInvalidException("Voucher này chỉ dành cho khách hàng VIP.");
            }
        }

        // Trừ điểm (PointService.spendPoints sẽ check balance + throw PointBalanceException nếu thiếu)
        int remaining = spendPointsForVoucher(userId, catalog.getRequiredPoints(),
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
        return userVoucherRepository.save(userVoucher);
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

        UserVoucher uv = userVoucherRepository.findByCode(code)
                .orElseThrow(() -> new VoucherInvalidException("Mã voucher không tồn tại: " + code));

        if (uv.getStatus() != UserVoucher.Status.UNUSED) {
            throw new VoucherInvalidException("Voucher không ở trạng thái UNUSED: " + uv.getStatus());
        }

        uv.setStatus(UserVoucher.Status.USED);
        uv.setUsedAt(LocalDateTime.now());
        uv.setUsedInOrderId(orderId);
        userVoucherRepository.save(uv);
    }

    // ============================================================
    // Task 2.11 (continued): Cron expire vouchers
    // ============================================================

    /**
     * Expire voucher UNUSED quá hạn. Được gọi bởi VoucherExpireJob (Batch 5).
     */
    @Transactional
    public int expireOldVouchers(LocalDateTime now) {
        List<UserVoucher> expired = userVoucherRepository.findExpiredUnusedVouchers(now);
        for (UserVoucher uv : expired) {
            uv.setStatus(UserVoucher.Status.EXPIRED);
            userVoucherRepository.save(uv);
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
}
