package com.example.thexuong.job;

import com.example.thexuong.service.PointService;
import com.example.thexuong.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Batch 5 - Task 5.4: Cron expire điểm cũ (>12 tháng) hàng ngày 00:00.
 * Batch 5 - Task 5.5: Cron expire voucher UNUSED quá hạn hàng ngày 00:30.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointExpireJob {

    private final PointService pointService;
    private final VoucherService voucherService;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void expireOldPoints() {
        try {
            int total = pointService.expireOldPoints(LocalDateTime.now());
            log.info("[CRON] PointExpireJob done. Expired: {} points", total);
        } catch (Exception e) {
            log.error("[CRON] PointExpireJob failed", e);
        }
    }

    @Scheduled(cron = "0 30 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void expireOldVouchers() {
        try {
            int count = voucherService.expireOldVouchers(LocalDateTime.now());
            log.info("[CRON] VoucherExpireJob done. Expired: {} vouchers", count);
        } catch (Exception e) {
            log.error("[CRON] VoucherExpireJob failed", e);
        }
    }
}
