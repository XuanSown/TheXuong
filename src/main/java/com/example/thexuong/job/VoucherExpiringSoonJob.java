package com.example.thexuong.job;

import com.example.thexuong.entity.UserVoucher;
import com.example.thexuong.repository.UserVoucherRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Batch 5 - Task 5.6: Cron gửi email cảnh báo voucher sắp hết hạn.
 * Chạy hàng ngày lúc 09:00 sáng.
 */
@Component
@RequiredArgsConstructor
public class VoucherExpiringSoonJob {

    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendVoucherExpiringWarnings() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysFromNow = now.plusDays(3);

            // Lấy tất cả UNUSED vouchers hết hạn trong vòng 3 ngày tới
            List<UserVoucher> expiringSoon = userVoucherRepository.findExpiringSoonVouchers(now, threeDaysFromNow);

            for (UserVoucher uv : expiringSoon) {
                // Lấy email user
                userRepository.findById(uv.getUserId())
                        .ifPresent(user -> {
                            String email = user.getEmail();
                            if (email != null && !email.isBlank()) {
                                try {
                                    emailService.sendVoucherExpiring(
                                            email,
                                            user.getFullName() != null ? user.getFullName() : user.getUsername(),
                                            uv.getCode(),
                                            uv.getVoucher() != null ? uv.getVoucher().getDiscountAmount() + "đ" : "???",
                                            uv.getExpiresAt(),
                                            3
                                    );
                                    System.out.println("[CRON] Sent voucher expiring warning for code: " + uv.getCode() + " to " + email);
                                } catch (Exception e) {
                                    System.err.println("[CRON] Failed to send voucher expiring email for " + uv.getCode() + ": " + e.getMessage());
                                }
                            }
                        });
            }

            System.out.println("[CRON] VoucherExpiringSoonJob done. Sent warnings for " + expiringSoon.size() + " vouchers.");
        } catch (Exception e) {
            System.err.println("[CRON] VoucherExpiringSoonJob failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
