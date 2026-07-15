package com.example.thexuong.job;

import com.example.thexuong.service.TierReevaluateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Batch 4 - Task 4.19: Cron re-evaluate tier theo Phương án Y.
 * Chạy 00:00 ngày 1 hàng tháng.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TierReevaluateJob {

    private final TierReevaluateService tierReevaluateService;

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void reevaluateAllVip() {
        try {
            int changed = tierReevaluateService.reevaluateAllActiveVip();
            log.info("[CRON] TierReevaluateJob done. Changed: {} tiers", changed);
        } catch (Exception e) {
            log.error("[CRON] TierReevaluateJob failed", e);
        }
    }
}
