package com.example.thexuong.service;

import com.example.thexuong.entity.TierEvaluationLog;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.TierEvaluationLogRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        String baseTier = pointTierService.getBaseTierCode();
        String oldTier = user.getTierCode() != null ? user.getTierCode() : baseTier;
        if (baseTier.equals(oldTier)) return false;  // chỉ check user không ở hạng thấp nhất

        // Cửa sổ 365 ngày
        LocalDateTime windowStart = LocalDateTime.now().minusDays(365);
        LocalDateTime windowEnd = LocalDateTime.now();

        // Tính tier mới
        BigDecimal totalSpent = orderRepository.sumTotalForPointCalcByUserSince(userId, windowStart);
        Long totalPointsEarnedLong = pointTransactionRepository.sumPointsByUserAndTypeSince(
                userId, com.example.thexuong.entity.PointTransaction.Type.EARN, windowStart);
        Integer totalPointsEarned = totalPointsEarnedLong != null ? totalPointsEarnedLong.intValue() : 0;

        String newTier = pointTierService.getTierForUser(userId);
        String reason;
        if (newTier.equals(oldTier)) {
            reason = "Giữ " + oldTier + " - đủ điều kiện duy trì trong 365 ngày";
        } else {
            reason = "Giáng xuống " + newTier + " - không đạt đủ chi tiêu/điểm trong 365 ngày";
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

            // Gửi email nếu bị hạ hạng
            if (baseTier.equals(newTier) && !baseTier.equals(oldTier)) {
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
     * Cron entry point: re-evaluate tất cả user không ở hạng thấp nhất có tier_promoted_at <= (now - 365 ngày).
     */
    @Transactional
    public int reevaluateAllActiveVip() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(365);
        String baseTier = pointTierService.getBaseTierCode();
        List<User> premiumUsers = userRepository.findByTierCodeNotAndTierPromotedAtBefore(baseTier, threshold);
        int changed = 0;
        for (User user : premiumUsers) {
            if (reevaluateUser(user.getId())) {
                changed++;
            }
        }
        return changed;
    }
}
