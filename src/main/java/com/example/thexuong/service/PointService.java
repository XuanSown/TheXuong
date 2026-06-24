package com.example.thexuong.service;

import com.example.thexuong.entity.PointTransaction;
import com.example.thexuong.entity.UserPoints;
import com.example.thexuong.exception.PointBalanceException;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.UserPointsRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    public static final BigDecimal VND_PER_POINT = new BigDecimal("100000");

    @Autowired
    private final UserPointsRepository userPointsRepository;
    @Autowired
    private final PointTransactionRepository pointTransactionRepository;
    @Autowired
    private final UserRepository userRepository;

    @Transactional
    public int earnPoints(Long userId, Long orderId, BigDecimal amount, String note) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int points = amount.divide(VND_PER_POINT, 0, java.math.RoundingMode.FLOOR).intValue();
        if (points <= 0) {
            return 0;
        }

        UserPoints userPoints = getOrCreateUserPoints(userId);
        userPoints.setCurrentPoints(userPoints.getCurrentPoints() + points);
        userPoints.setTotalEarned(userPoints.getTotalEarned() + points);
        userPoints.setLastActivityAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);

        PointTransaction tx = PointTransaction.builder()
                .userId(userId)
                .orderId(orderId)
                .type(PointTransaction.Type.EARN)
                .points(points)
                .expiresAt(LocalDateTime.now().plusMonths(12))
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();
        pointTransactionRepository.save(tx);

        return points;
    }

    @Transactional
    public int spendPoints(Long userId, int points, String note) {
        if (points <= 0) {
            throw new IllegalArgumentException("Số điểm tiêu phải > 0");
        }

        UserPoints userPoints = userPointsRepository.findByUserId(userId)
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

    @Transactional
    public void reversePoints(Long orderId, String note) {
        List<PointTransaction> earnTxs = pointTransactionRepository.findEarnTransactionsByOrderId(orderId);
        if (earnTxs.isEmpty()) {
            return;
        }

        for (PointTransaction earnTx : earnTxs) {
            int pointsToReverse = earnTx.getPoints();
            UserPoints userPoints = userPointsRepository.findByUserId(earnTx.getUserId())
                    .orElseThrow(() -> new RuntimeException("UserPoints không tồn tại cho user " + earnTx.getUserId()));

            int newCurrent = Math.max(0, userPoints.getCurrentPoints() - pointsToReverse);
            int actualReverse = userPoints.getCurrentPoints() - newCurrent;

            userPoints.setCurrentPoints(newCurrent);
            userPoints.setTotalSpent(userPoints.getTotalSpent() + actualReverse);
            userPoints.setLastActivityAt(LocalDateTime.now());
            userPointsRepository.save(userPoints);

            PointTransaction reverseTx = PointTransaction.builder()
                    .userId(earnTx.getUserId())
                    .orderId(orderId)
                    .type(PointTransaction.Type.REVERSE)
                    .points(-actualReverse)
                    .note(note + " (gốc: " + pointsToReverse + " điểm, thực trừ: " + actualReverse + ")")
                    .createdAt(LocalDateTime.now())
                    .build();
            pointTransactionRepository.save(reverseTx);
        }
    }

    @Transactional
    public int adjustPoints(Long adminId, Long userId, int delta, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("ADJUST bắt buộc phải có note để audit.");
        }

        UserPoints userPoints = getOrCreateUserPoints(userId);
        int newCurrent = Math.max(0, userPoints.getCurrentPoints() + delta);
        int actualDelta = newCurrent - userPoints.getCurrentPoints();

        userPoints.setCurrentPoints(newCurrent);
        if (delta > 0) {
            userPoints.setTotalEarned(userPoints.getTotalEarned() + actualDelta);
        } else {
            userPoints.setTotalSpent(userPoints.getTotalSpent() + Math.abs(actualDelta));
        }
        userPoints.setLastActivityAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);

        PointTransaction tx = PointTransaction.builder()
                .userId(userId)
                .adminId(adminId)
                .type(PointTransaction.Type.ADJUST)
                .points(actualDelta)
                .note("[ADMIN ADJUST] " + note)
                .createdAt(LocalDateTime.now())
                .build();
        pointTransactionRepository.save(tx);

        return newCurrent;
    }

    @Transactional
    public int expireOldPoints(LocalDateTime now) {
        List<PointTransaction> expiredTxs = pointTransactionRepository.findExpiredEarnTransactions(now);
        int totalExpired = 0;

        for (PointTransaction earnTx : expiredTxs) {
            UserPoints userPoints = userPointsRepository.findByUserId(earnTx.getUserId()).orElse(null);
            if (userPoints == null) continue;

            int pointsToExpire = earnTx.getPoints();
            int newCurrent = Math.max(0, userPoints.getCurrentPoints() - pointsToExpire);
            int actualExpire = userPoints.getCurrentPoints() - newCurrent;

            if (actualExpire > 0) {
                userPoints.setCurrentPoints(newCurrent);
                userPoints.setTotalSpent(userPoints.getTotalSpent() + actualExpire);
                userPointsRepository.save(userPoints);

                PointTransaction expireTx = PointTransaction.builder()
                        .userId(earnTx.getUserId())
                        .orderId(earnTx.getOrderId())
                        .type(PointTransaction.Type.EXPIRE)
                        .points(-actualExpire)
                        .note("Hết hạn 12 tháng (EARN gốc từ đơn #" + earnTx.getOrderId() + ")")
                        .createdAt(now)
                        .build();
                pointTransactionRepository.save(expireTx);
                totalExpired += actualExpire;
            }
        }
        return totalExpired;
    }

    public UserPoints getOrCreateUserPoints(Long userId) {
        return userPointsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPoints newPoints = UserPoints.builder()
                            .userId(userId)
                            .currentPoints(0)
                            .totalEarned(0L)
                            .totalSpent(0L)
                            .lastActivityAt(LocalDateTime.now())
                            .version(0L)
                            .build();
                    return userPointsRepository.save(newPoints);
                });
    }

    public int getCurrentPoints(Long userId) {
        return userPointsRepository.findByUserId(userId)
                .map(UserPoints::getCurrentPoints)
                .orElse(0);
    }

    public List<PointTransaction> getHistory(Long userId) {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
