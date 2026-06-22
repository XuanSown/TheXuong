package com.example.thexuong.job;

import com.example.thexuong.entity.TierEvaluationLog;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.TierEvaluationLogRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TierWarningJob {

    @Autowired
    private final TierEvaluationLogRepository tierEvaluationLogRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Ho_Chi_Minh")
    public void warnVipNearReevaluation() {
        try {
            // Tìm log gần nhất có newTierCode=VIP và evaluatedAt <= now - 335 ngày
            LocalDateTime threshold = LocalDateTime.now().minusDays(335);
            List<TierEvaluationLog> logs = tierEvaluationLogRepository.findUsersNearReevaluation(threshold);
            int warned = 0;
            for (TierEvaluationLog log : logs) {
                User user = userRepository.findById(log.getUserId()).orElse(null);
                if (user == null) continue;
                LocalDateTime nextEvaluation = log.getEvaluatedAt().plusDays(365);
                try {
                    emailService.sendVipExpiryWarning(user.getEmail(), user.getFullName(), nextEvaluation);
                    warned++;
                } catch (Exception e) {
                    System.err.println("[EMAIL] sendVipExpiryWarning failed for user " + user.getId() + ": " + e.getMessage());
                }
            }
            System.out.println("[CRON] TierWarningJob done. Warned: " + warned);
        } catch (Exception e) {
            System.err.println("[CRON] TierWarningJob failed: " + e.getMessage());
        }
    }
}
