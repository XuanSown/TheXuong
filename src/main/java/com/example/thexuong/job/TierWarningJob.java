package com.example.thexuong.job;

import com.example.thexuong.entity.TierEvaluationLog;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.TierEvaluationLogRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Batch 4 - Task 4.20: Cron cảnh báo user VIP sắp đến hạn re-evaluate (trong 30 ngày tới).
 * Chạy mỗi ngày 09:00.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TierWarningJob {

    private final TierEvaluationLogRepository tierEvaluationLogRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final com.example.thexuong.service.PointTierService pointTierService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Ho_Chi_Minh")
    public void warnVipNearReevaluation() {
        try {
            // Tìm log gần nhất có newTierCode khác BaseTier và evaluatedAt <= now - 335 ngày
            LocalDateTime threshold = LocalDateTime.now().minusDays(335);
            String baseTier = pointTierService.getBaseTierCode();
            List<TierEvaluationLog> evaluationLogs = tierEvaluationLogRepository.findUsersNearReevaluation(threshold, baseTier);
            int warned = 0;
            for (TierEvaluationLog evaluationLog : evaluationLogs) {
                Long userId = evaluationLog.getUserId();
                if (userId == null) continue;
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) continue;
                LocalDateTime nextEvaluation = evaluationLog.getEvaluatedAt().plusDays(365);
                try {
                    emailService.sendVipExpiryWarning(user.getEmail(), user.getFullName(), nextEvaluation);
                    warned++;
                } catch (Exception e) {
                    log.error("[EMAIL] sendVipExpiryWarning failed for user {}: {}", user.getId(), e.getMessage());
                }
            }
            log.info("[CRON] TierWarningJob done. Warned: {} users", warned);
        } catch (Exception e) {
            log.error("[CRON] TierWarningJob failed", e);
        }
    }
}
