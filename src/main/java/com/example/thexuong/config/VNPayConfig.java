package com.example.thexuong.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VNPayConfig.class);

    private String tmnCode;
    private String secretKey;
    private String payUrl;
    private String returnUrl;
    private String frontendReturnUrl;

    public String getTmnCode() {
        return tmnCode;
    }

    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getFrontendReturnUrl() {
        return frontendReturnUrl;
    }

    public void setFrontendReturnUrl(String frontendReturnUrl) {
        this.frontendReturnUrl = frontendReturnUrl;
    }

    // Aliases tương thích ngược (theo convention VNPAY cũ)
    public String getVnp_TmnCode() {
        return tmnCode;
    }

    public void setVnp_TmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public String getVnp_PayUrl() {
        return payUrl;
    }

    public void setVnp_PayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getVnp_ReturnUrl() {
        return returnUrl;
    }

    public void setVnp_ReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    // Hàm mã hóa SHA512 theo chuẩn VNPAY
    public String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || key.isBlank()) {
                log.error("VNPay secret key is missing. Check VNPAY_HASH_SECRET in .env");
                throw new IllegalStateException("VNPay secret key is not configured");
            }
            if (data == null) {
                throw new NullPointerException("Data must not be null");
            }
            final javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            final javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(keySpec);
            byte[] dataBytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate HMAC SHA512", ex);
        }
    }

    // Hàm lấy IP thực của khách hàng
    public static String getIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        try {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                int comma = forwarded.indexOf(',');
                return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
            }
            String remoteAddr = request.getRemoteAddr();
            return (remoteAddr == null || remoteAddr.isBlank()) ? "127.0.0.1" : remoteAddr;
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    // Hàm random mã giao dịch
    public static String getRandomNumber(int len) {
        java.util.Random rnd = new java.util.Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
