package com.example.thexuong.service;

import com.example.thexuong.entity.PointTier;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.PointTierRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service quản lý tier (Phương án C + Y).
 *
 * Phương án C — Lên hạng:
 *   Tổng chi tiêu 365 ngày >= 5M HOẶC tổng điểm earn 365 ngày >= 50.
 *
 * Phương án Y — Hạ hạng (re-evaluate theo năm):
 *   Cron 00:00 ngày 1 hàng tháng: nếu user VIP mà không đạt ngưỡng trong 365 ngày → hạ THUONG.
 *   KHÔNG hạ tự động khi refund (giữ rule đã chốt ở voucher.md mục 3.1).
 */
@Service
@RequiredArgsConstructor
public class PointTierService {

    @Autowired
    private final PointTierRepository pointTierRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final PointTransactionRepository pointTransactionRepository;

    /**
     * Tính tier phù hợp cho user dựa trên chi tiêu + điểm trong 365 ngày gần nhất.
     * Trả về code (THUONG / VIP).
     */
    public String getTierForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "THUONG";

        LocalDateTime windowStart = LocalDateTime.now().minusDays(365);

        // Tổng chi tiêu trong 365 ngày (dựa trên total_for_point_calc của đơn COMPLETED)
        BigDecimal totalSpent = orderRepository.sumTotalForPointCalcByUserSince(userId, windowStart);

        // Tổng điểm earn trong 365 ngày
        Long totalPointsEarnedLong = pointTransactionRepository.sumPointsByUserAndTypeSince(
                userId, com.example.thexuong.entity.PointTransaction.Type.EARN, windowStart);
        Integer totalPointsEarned = totalPointsEarnedLong != null ? totalPointsEarnedLong.intValue() : 0;

        // Match tier cao nhất đạt được
        List<PointTier> tiers = pointTierRepository.findAllByOrderByMinTotalSpentAsc();
        String bestTier = "THUONG";
        for (PointTier tier : tiers) {
            boolean matchSpent = totalSpent != null
                    && tier.getMinTotalSpent() != null
                    && totalSpent.compareTo(tier.getMinTotalSpent()) >= 0;
            boolean matchPoints = totalPointsEarned != null
                    && tier.getMinTotalPoints() != null
                    && totalPointsEarned >= tier.getMinTotalPoints();
            if (matchSpent || matchPoints) {
                bestTier = tier.getCode();
            }
        }
        return bestTier;
    }

    /**
     * Check và nâng tier nếu user đủ điều kiện.
     * Gọi từ OrderService.confirmReceived (sau khi earn points).
     *
     * Rule: KHÔNG hạ tier (chỉ hạ qua cron re-evaluate).
     * Set tier_promoted_at = NOW() khi nâng (dùng cho Phương án Y).
     */
    @Transactional
    public boolean upgradeTierIfEligible(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        String currentTier = user.getTierCode() != null ? user.getTierCode() : "THUONG";
        String newTier = getTierForUser(userId);

        // So sánh priority (VIP > THUONG)
        int currentPriority = tierPriority(currentTier);
        int newPriority = tierPriority(newTier);

        if (newPriority > currentPriority) {
            user.setTierCode(newTier);
            user.setTierPromotedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Set tier THUONG cho user lần đầu đặt đơn.
     * Gọi từ OrderService.placeOrder.
     */
    @Transactional
    public void setFirstOrderTier(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        if (user.getTierCode() == null) {
            user.setTierCode("THUONG");
            user.setTierPromotedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    private int tierPriority(String code) {
        if (code == null) return 0;
        return switch (code) {
            case "VIP" -> 2;
            case "THUONG" -> 1;
            default -> 0;
        };
    }
}
