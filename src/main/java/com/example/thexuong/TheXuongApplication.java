package com.example.thexuong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Batch 5 - Task 5.1: @EnableScheduling cho phép @Scheduled cron job chạy.
 * (TierReevaluateJob, TierWarningJob, PointExpireJob)
 */
@SpringBootApplication
@EnableScheduling
public class TheXuongApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheXuongApplication.class, args);
    }

}
