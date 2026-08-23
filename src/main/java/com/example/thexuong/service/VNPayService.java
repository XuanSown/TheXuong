package com.example.thexuong.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.thexuong.config.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {
    private final VNPayConfig vnPayConfig;

    /**
     * Kết quả xác minh dữ liệu trả về từ VNPay.
     *
     * @param hashValid chữ ký vnp_SecureHash hợp lệ
     * @param orderId   mã đơn hàng parse từ vnp_TxnRef
     * @param vnpAmount số tiền VNPay gửi về (đơn vị: VND x100)
     * @param success   giao dịch thành công (ResponseCode = 00 và TransactionStatus = 00)
     */
    public record VNPayVerifyResult(boolean hashValid, Long orderId, long vnpAmount, boolean success) {
        public static VNPayVerifyResult invalid() {
            return new VNPayVerifyResult(false, null, 0L, false);
        }
    }

    public String createOrder(Long orderId, int total, String orderInfor, HttpServletRequest request) {
        log.debug("Creating VNPay order: orderId={}, total={}, orderInfo={}", orderId, total, orderInfor);
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = orderId + "_" + VNPayConfig.getRandomNumber(6);
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.getTmnCode();

        // Số tiền nhân 100 theo chuẩn VNPAY (vd: 10000 vnđ -> 1000000)
        long amount = total * 100L;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        // "ncb" là mã ngân hàng test của VNPAY
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build dữ liệu để tạo mã băm (Hash)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        log.info("VNPay order created: txnRef={}, amount={}", vnp_TxnRef, amount);
        return vnPayConfig.getPayUrl() + "?" + queryUrl;
    }

    /**
     * Xác minh dữ liệu VNPay gửi về qua vnp_ReturnUrl.
     * Kiểm tra chữ ký SHA-512 (loại trừ vnp_SecureHash / vnp_SecureHashType),
     * parse orderId từ vnp_TxnRef và kiểm tra mã kết quả giao dịch.
     */
    public VNPayVerifyResult verifyReturn(Map<String, String> vnpParams) {
        if (vnpParams == null || vnpParams.isEmpty()) {
            log.warn("VNPay return: empty params");
            return VNPayVerifyResult.invalid();
        }

        String secureHash = vnpParams.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            log.warn("VNPay return: missing vnp_SecureHash");
            return VNPayVerifyResult.invalid();
        }

        Map<String, String> hashParams = new HashMap<>(vnpParams);
        hashParams.remove("vnp_SecureHash");
        hashParams.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(hashParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = hashParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        if (!calculatedHash.equalsIgnoreCase(secureHash)) {
            log.warn("VNPay return: invalid secure hash. Received={}, calculated={}", secureHash, calculatedHash);
            return VNPayVerifyResult.invalid();
        }

        Long orderId = parseOrderId(vnpParams.get("vnp_TxnRef"));
        if (orderId == null) {
            log.warn("VNPay return: cannot parse orderId from vnp_TxnRef={}", vnpParams.get("vnp_TxnRef"));
            return new VNPayVerifyResult(true, null, 0L, false);
        }

        long vnpAmount = 0L;
        try {
            vnpAmount = Long.parseLong(vnpParams.getOrDefault("vnp_Amount", "0"));
        } catch (NumberFormatException e) {
            log.warn("VNPay return: invalid vnp_Amount={}", vnpParams.get("vnp_Amount"));
        }

        boolean success = "00".equals(vnpParams.get("vnp_ResponseCode"))
                && "00".equals(vnpParams.get("vnp_TransactionStatus"));

        log.info("VNPay return verified: orderId={}, amount={}, success={}, responseCode={}, transactionStatus={}",
                orderId, vnpAmount, success, vnpParams.get("vnp_ResponseCode"), vnpParams.get("vnp_TransactionStatus"));
        return new VNPayVerifyResult(true, orderId, vnpAmount, success);
    }

    private Long parseOrderId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }
        int underscore = txnRef.indexOf('_');
        String prefix = underscore >= 0 ? txnRef.substring(0, underscore) : txnRef;
        try {
            return Long.parseLong(prefix);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
