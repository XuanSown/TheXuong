package com.example.thexuong.controller;

import com.example.thexuong.config.VNPayConfig;
import com.example.thexuong.service.OrderService;
import com.example.thexuong.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Callback VNPay: VNPay redirect trình duyệt về đây sau khi khách thanh toán xong.
 * Xác minh chữ ký SHA-512, cập nhật trạng thái đơn hàng rồi redirect về trang /orders của frontend.
 */
@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayCallbackController {

    private final VNPayService vnPayService;
    private final OrderService orderService;
    private final VNPayConfig vnPayConfig;

    @GetMapping("/return")
    public void handleReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0 && v[0] != null) {
                params.put(k, v[0]);
            }
        });

        VNPayService.VNPayVerifyResult result = vnPayService.verifyReturn(params);

        if (!result.hashValid()) {
            log.warn("VNPay callback: invalid signature, redirect to error page");
            redirect(response, "error");
            return;
        }

        if (!result.success()) {
            log.info("VNPay callback: payment failed/cancelled for order {}", result.orderId());
            redirect(response, "fail");
            return;
        }

        if (result.orderId() == null) {
            log.warn("VNPay callback: success but missing orderId, redirect to error page");
            redirect(response, "error");
            return;
        }

        try {
            orderService.confirmVnpayPayment(result.orderId(), result.vnpAmount());
            redirect(response, "success");
        } catch (Exception e) {
            log.error("VNPay callback: failed to confirm order #{}: {}", result.orderId(), e.getMessage(), e);
            redirect(response, "error");
        }
    }

    private void redirect(HttpServletResponse response, String status) throws IOException {
        String frontendUrl = vnPayConfig.getFrontendReturnUrl();
        if (frontendUrl == null || frontendUrl.isBlank()) {
            frontendUrl = "http://localhost:5173/orders";
        }
        response.sendRedirect(frontendUrl + "?payment=" + status);
    }
}
