package com.example.thexuong.job;

import com.example.thexuong.service.TierReevaluateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Batch 4 - Task 4.19: Cron re-evaluate tier theo Phương án Y.
 * Chạy 00:00 ngày 1 hàng tháng.
 */
@Component
@RequiredArgsConstructor
public class TierReevaluateJob {

    @Autowired
    private final TierReevaluateService tierReevaluateService;

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void reevaluateAllVip() {
        try {
            int changed = tierReevaluateService.reevaluateAllActiveVip();
            System.out.println("[CRON] TierReevaluateJob done. Changed: " + changed);
        } catch (Exception e) {
            System.err.println("[CRON] TierReevaluateJob failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
