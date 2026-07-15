package com.example.thexuong.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    private String tmnCode;
    private String secretKey;
    private String payUrl;
    private String returnUrl;

    public String getVnp_TmnCode() {
        return tmnCode;
    }

    public void setVnp_TmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
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
            if (key == null || data == null) {
                throw new NullPointerException("Key and data must not be null");
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

    // Hàm lấy IP của khách hàng
    public static String getIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "Invalid IP";
        }
        return ipAddress;
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
