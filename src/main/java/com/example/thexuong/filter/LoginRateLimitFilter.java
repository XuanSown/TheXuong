package com.example.thexuong.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter cho endpoint /api/v1/auth/login.
 *
 * - Theo dõi số lần login thất bại theo IP address.
 * - Sau 5 lần thất bại trong vòng 15 phút → khóa IP trong 15 phút.
 * - Trả về HTTP 429 (Too Many Requests) khi IP bị khóa.
 *
 * Mục đích: Ngăn brute-force attack và credential stuffing.
 */
@Component
@Slf4j
public class LoginRateLimitFilter extends OncePerRequestFilter {

    // Số lần thất bại tối đa trước khi khóa
    private static final int MAX_FAILED_ATTEMPTS = 5;

    // Cửa sổ thời gian đếm lỗi (phút)
    private static final int WINDOW_MINUTES = 15;

    // Thời gian khóa IP (phút)
    private static final int LOCKOUT_MINUTES = 15;

    // Lưu trữ: IP → (số lần lỗi, thời gian lỗi đầu tiên trong cửa sổ)
    private final Map<String, AttemptInfo> failedAttempts = new ConcurrentHashMap<>();

    // Lưu trữ: IP → thời gian mở khóa
    private final Map<String, LocalDateTime> lockedIps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        // Chỉ áp dụng rate limit cho endpoint login
        if ("/api/v1/auth/login".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            // Kiểm tra IP có bị khóa không
            if (isLocked(clientIp)) {
                LocalDateTime unlockTime = lockedIps.get(clientIp);
                long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
                log.warn("[RATE_LIMIT] Blocked login attempt from locked IP: {} (unlock in {}s)", clientIp, remainingSeconds);
                response.setStatus(429); // HTTP 429 Too Many Requests
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Too many failed login attempts. " +
                        "Please try again in " + remainingSeconds + " seconds.\"}");
                return;
            }
        }

        // Cho request đi tiếp
        filterChain.doFilter(request, response);

        // Sau khi xử lý xong, kiểm tra response để biết login thành công hay thất bại
        if ("/api/v1/auth/login".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            if (response.getStatus() == HttpServletResponse.SC_OK) {
                // Login thành công → reset counter cho IP này
                resetAttempts(clientIp);
            }
        }
    }

    /**
     * Gọi từ AuthenticationFailureHandler khi login thất bại.
     */
    public void recordFailedAttempt(String ip) {
        AttemptInfo info = failedAttempts.get(ip);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(WINDOW_MINUTES);

        if (info == null || info.firstAttemptTime.isBefore(windowStart)) {
            // Cửa sổ cũ đã hết → bắt đầu cửa sổ mới
            failedAttempts.put(ip, new AttemptInfo(now, 1));
        } else {
            // Tăng counter trong cùng cửa sổ
            info.failedCount++;
            if (info.failedCount >= MAX_FAILED_ATTEMPTS) {
                // Khóa IP
                LocalDateTime unlockTime = now.plusMinutes(LOCKOUT_MINUTES);
                lockedIps.put(ip, unlockTime);
                log.warn("[RATE_LIMIT] IP {} locked until {} after {} failed attempts",
                        ip, unlockTime, info.failedCount);
            }
        }

        // Dọn dẹp entries cũ
        cleanupExpiredEntries();
    }

    private boolean isLocked(String ip) {
        LocalDateTime unlockTime = lockedIps.get(ip);
        if (unlockTime == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(unlockTime)) {
            // Hết hạn khóa → xóa và cho qua
            lockedIps.remove(ip);
            failedAttempts.remove(ip);
            return false;
        }
        return true;
    }

    public void resetAttempts(String ip) {
        failedAttempts.remove(ip);
        lockedIps.remove(ip);
    }

    private void cleanupExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(WINDOW_MINUTES);
        LocalDateTime lockStart = now.minusMinutes(LOCKOUT_MINUTES);

        // Xóa failed attempts cũ
        failedAttempts.entrySet().removeIf(entry ->
                entry.getValue().firstAttemptTime.isBefore(windowStart)
        );

        // Xóa IP đã hết hạn khóa
        lockedIps.entrySet().removeIf(entry ->
                entry.getValue().isBefore(lockStart)
        );
    }

    public String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For có thể chứa nhiều IP, lấy cái đầu tiên
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Lưu thông tin lần thất bại.
     */
    private static class AttemptInfo {
        LocalDateTime firstAttemptTime;
        int failedCount;

        AttemptInfo(LocalDateTime firstAttemptTime, int failedCount) {
            this.firstAttemptTime = firstAttemptTime;
            this.failedCount = failedCount;
        }
    }
}
