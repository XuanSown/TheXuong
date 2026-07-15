package com.example.thexuong.service;

import com.example.thexuong.entity.PointTier;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.PointTierRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.thexuong.dto.UserLoyaltyDto;
import com.example.thexuong.entity.TierHistory;
import com.example.thexuong.repository.TierHistoryRepository;
import com.example.thexuong.service.VoucherService;
import com.example.thexuong.entity.UserPoints;
import com.example.thexuong.repository.UserPointsRepository;

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
@Slf4j
public class PointTierService {

    private final PointTierRepository pointTierRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final TierHistoryRepository tierHistoryRepository;
    private final VoucherService voucherService;
    private final UserPointsRepository userPointsRepository;

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

    public UserLoyaltyDto getLoyaltyProgress(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        LocalDateTime windowStart = LocalDateTime.now().minusDays(365);
        BigDecimal totalSpent = orderRepository.sumTotalForPointCalcByUserSince(userId, windowStart);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        Long totalPointsEarnedLong = pointTransactionRepository.sumPointsByUserAndTypeSince(
                userId, com.example.thexuong.entity.PointTransaction.Type.EARN, windowStart);
        Integer totalPointsEarned = totalPointsEarnedLong != null ? totalPointsEarnedLong.intValue() : 0;

        int currentPoints = userPointsRepository.findByUserId(userId)
                .map(UserPoints::getCurrentPoints)
                .orElse(0);

        String currentTierCode = user.getTierCode() != null ? user.getTierCode() : "THUONG";
        PointTier currentTier = pointTierRepository.findByCode(currentTierCode).orElse(null);
        String currentTierName = currentTier != null ? currentTier.getName() : "Thường";
        int currentPriority = currentTier != null ? currentTier.getMinTotalSpent().intValue() : 0;

        UserLoyaltyDto dto = UserLoyaltyDto.builder()
                .userId(userId)
                .currentTierCode(currentTierCode)
                .currentTierName(currentTierName)
                .currentPoints(currentPoints)
                .totalSpent365Days(totalSpent)
                .totalPointsEarned365Days(totalPointsEarned)
                .build();

        // Tìm tier kế tiếp (dựa trên order by minTotalSpent asc)
        List<PointTier> tiers = pointTierRepository.findAllByOrderByMinTotalSpentAsc();
        for (PointTier tier : tiers) {
            int tierPriority = tier.getMinTotalSpent().intValue();
            if (tierPriority > currentPriority) {
                dto.setNextTierCode(tier.getCode());
                dto.setNextTierName(tier.getName());
                dto.setMinSpentNextTier(tier.getMinTotalSpent());
                dto.setMinPointsNextTier(tier.getMinTotalPoints());

                BigDecimal remainingSpent = tier.getMinTotalSpent().subtract(totalSpent);
                dto.setSpentRemainingToNextTier(remainingSpent.compareTo(BigDecimal.ZERO) > 0 ? remainingSpent : BigDecimal.ZERO);

                int remainingPoints = tier.getMinTotalPoints() - totalPointsEarned;
                dto.setPointsRemainingToNextTier(Math.max(remainingPoints, 0));
                
                break; // Chỉ lấy tier kế tiếp ngay lập tức
            }
        }
        return dto;
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

        String currentTierCode = user.getTierCode() != null ? user.getTierCode() : "THUONG";
        String newTierCode = getTierForUser(userId);

        PointTier currentTier = pointTierRepository.findByCode(currentTierCode).orElse(null);
        PointTier nextTier = pointTierRepository.findByCode(newTierCode).orElse(null);

        // So sánh priority (dựa trên min_total_spent)
        int currentPriority = currentTier != null ? currentTier.getMinTotalSpent().intValue() : 0;
        int newPriority = nextTier != null ? nextTier.getMinTotalSpent().intValue() : 0;

        if (newPriority > currentPriority) {
            user.setTierCode(newTierCode);
            user.setTierPromotedAt(LocalDateTime.now());
            userRepository.save(user);

            // Ghi nhận lịch sử thăng hạng
            tierHistoryRepository.save(TierHistory.builder()
                    .userId(userId)
                    .oldTierCode(currentTierCode)
                    .newTierCode(newTierCode)
                    .reason("Thăng hạng do đạt đủ điểm/chi tiêu")
                    .createdAt(LocalDateTime.now())
                    .build());

            // Tặng voucher tự động nếu hạng mới có rewardVoucherId
            if (nextTier != null && nextTier.getRewardVoucherId() != null) {
                try {
                    voucherService.issueVoucherToUser(nextTier.getRewardVoucherId(), userId);
                } catch (Exception e) {
                    log.error("Lỗi khi phát hành voucher tự động cho user {} lên hạng {}: {}", userId, newTierCode, e.getMessage());
                }
            }

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

            tierHistoryRepository.save(TierHistory.builder()
                    .userId(userId)
                    .oldTierCode(null)
                    .newTierCode("THUONG")
                    .reason("Hạng ban đầu khi đặt đơn đầu tiên")
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * Thăng hạng hoặc hạ bậc thủ công bởi Admin.
     */
    @Transactional
    public void updateTierManually(Long userId, String newTierCode, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra xem tier có tồn tại trong hệ thống không
        PointTier newTier = pointTierRepository.findByCode(newTierCode)
                .orElseThrow(() -> new RuntimeException("Mã hạng không hợp lệ: " + newTierCode));

        String oldTierCode = user.getTierCode() != null ? user.getTierCode() : "THUONG";
        PointTier oldTier = pointTierRepository.findByCode(oldTierCode).orElse(null);

        if (oldTierCode.equals(newTierCode)) {
            throw new RuntimeException("Người dùng đã ở hạng này rồi.");
        }

        int currentPriority = oldTier != null ? oldTier.getMinTotalSpent().intValue() : 0;
        int newPriority = newTier.getMinTotalSpent().intValue();

        user.setTierCode(newTierCode);
        user.setTierPromotedAt(LocalDateTime.now());
        userRepository.save(user);

        tierHistoryRepository.save(TierHistory.builder()
                .userId(userId)
                .oldTierCode(oldTierCode)
                .newTierCode(newTierCode)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build());

        // Nếu là thăng hạng, tặng voucher (nếu có)
        if (newPriority > currentPriority && newTier.getRewardVoucherId() != null) {
            try {
                voucherService.issueVoucherToUser(newTier.getRewardVoucherId(), userId);
            } catch (Exception e) {
                log.error("Lỗi khi phát hành voucher tự động cho user {} lên hạng {}: {}", userId, newTierCode, e.getMessage());
            }
        }
    }
}
