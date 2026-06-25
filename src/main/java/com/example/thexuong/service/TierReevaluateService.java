package com.example.thexuong.service;

import com.example.thexuong.entity.TierEvaluationLog;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.TierEvaluationLogRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Re-evaluate tier định kỳ (Phương án Y).
 * Cron 00:00 ngày 1 hàng tháng: query tất cả user VIP có tier_promoted_at <= (now - 365 ngày)
 * → tính lại tier → hạ THUONG nếu không đạt ngưỡng.
 */
@Service
@RequiredArgsConstructor
public class TierReevaluateService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final TierEvaluationLogRepository tierEvaluationLogRepository;
    private final PointTierService pointTierService;
    private final EmailService emailService;

    /**
     * Re-evaluate 1 user. Trả về true nếu có thay đổi tier.
     */
    @Transactional
    public boolean reevaluateUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        String oldTier = user.getTierCode() != null ? user.getTierCode() : "THUONG";
        if (!"VIP".equals(oldTier)) return false;  // chỉ check user VIP

        // Cửa sổ 365 ngày
        LocalDateTime windowStart = LocalDateTime.now().minusDays(365);
        LocalDateTime windowEnd = LocalDateTime.now();

        // Tính tier mới
        BigDecimal totalSpent = orderRepository.sumTotalForPointCalcByUserSince(userId, windowStart);
        Long totalPointsEarnedLong = pointTransactionRepository.sumPointsByUserAndTypeSince(
                userId, com.example.thexuong.entity.PointTransaction.Type.EARN, windowStart);
        Integer totalPointsEarned = totalPointsEarnedLong != null ? totalPointsEarnedLong.intValue() : 0;

        String newTier;
        String reason;
        if (totalSpent != null && totalSpent.compareTo(new BigDecimal("5000000")) >= 0) {
            newTier = "VIP";
            reason = "Giữ VIP - chi tiêu " + totalSpent + "đ trong 365 ngày";
        } else if (totalPointsEarned != null && totalPointsEarned >= 50) {
            newTier = "VIP";
            reason = "Giữ VIP - " + totalPointsEarned + " điểm earn trong 365 ngày";
        } else {
            newTier = "THUONG";
            reason = "Hạ THUONG - chỉ chi " + totalSpent + "đ, " + totalPointsEarned + " điểm trong 365 ngày";
        }

        // Ghi log
        TierEvaluationLog log = TierEvaluationLog.builder()
                .userId(userId)
                .evaluatedAt(LocalDateTime.now())
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .totalSpent(totalSpent)
                .totalPointsEarned(totalPointsEarned)
                .oldTierCode(oldTier)
                .newTierCode(newTier)
                .reason(reason)
                .build();
        tierEvaluationLogRepository.save(log);

        // Cập nhật nếu thay đổi
        if (!newTier.equals(oldTier)) {
            user.setTierCode(newTier);
            user.setTierPromotedAt(LocalDateTime.now());
            userRepository.save(user);

            // Gửi email nếu vừa bị hạ
            if ("THUONG".equals(newTier) && "VIP".equals(oldTier)) {
                try {
                    emailService.sendVipDowngraded(user.getEmail(), user.getFullName(), reason);
                } catch (Exception e) {
                    System.err.println("[EMAIL] sendVipDowngraded failed: " + e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Cron entry point: re-evaluate tất cả user VIP có tier_promoted_at <= (now - 365 ngày).
     */
    @Transactional
    public int reevaluateAllActiveVip() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(365);
        List<User> vipUsers = userRepository.findByTierCodeAndTierPromotedAtBefore("VIP", threshold);
        int changed = 0;
        for (User user : vipUsers) {
            if (reevaluateUser(user.getId())) {
                changed++;
            }
        }
        return changed;
    }
}
