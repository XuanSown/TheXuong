package com.example.thexuong.security.ratelimit;

import com.example.thexuong.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.io.IOException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitService rateLimitService;

    @Autowired
    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            return true;
        }

        RateLimitPlan plan = determinePlan(uri);
        String clientIp = ClientIpUtil.getClientIp(request);
        String key = determineKey(clientIp, plan);

        // 1. Check Specific Limit (if not global fallback)
        // ponytail: double-count có chủ ý — global là per-IP cap (không phải app-wide circuit breaker), tránh 1 IP choke app
        if (plan != RateLimitPlan.GLOBAL) {
            Bucket specificBucket = rateLimitService.resolveBucket(key, plan);
            ConsumptionProbe specificProbe = specificBucket.tryConsumeAndReturnRemaining(1);
            if (!specificProbe.isConsumed()) {
                reject429(response);
                return false;
            }
        }

        // 2. Check Global Limit (always check for all /api/**)
        String globalKey = "global_" + clientIp;
        Bucket globalBucket = rateLimitService.resolveBucket(globalKey, RateLimitPlan.GLOBAL);
        ConsumptionProbe globalProbe = globalBucket.tryConsumeAndReturnRemaining(1);
        if (!globalProbe.isConsumed()) {
            reject429(response);
            return false;
        }

        return true;
    }

    private void reject429(HttpServletResponse response) throws IOException {
        // ponytail: tên khác với HttpServletResponse.sendError để tránh nhầm
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\": 429, \"message\": \"Quá nhiều yêu cầu, vui lòng thử lại sau.\"}");
    }

    private RateLimitPlan determinePlan(String uri) {
        if (uri.startsWith("/api/v1/auth/login")) return RateLimitPlan.AUTH_LOGIN;
        if (uri.startsWith("/api/v1/auth/register")) return RateLimitPlan.AUTH_REGISTER;
        if (uri.startsWith("/api/v1/auth/forgot-password")) return RateLimitPlan.AUTH_FORGOT_PASSWORD;
        if (uri.startsWith("/api/v1/auth/reset-password")) return RateLimitPlan.AUTH_RESET_PASSWORD;
        if (uri.startsWith("/api/v1/auth/refresh")) return RateLimitPlan.AUTH_REFRESH_TOKEN;
        if (uri.startsWith("/api/v1/orders")) return RateLimitPlan.USER_ORDER;
        if (uri.startsWith("/api/v1/payments")) return RateLimitPlan.USER_PAYMENT;
        if (uri.startsWith("/api/v1/reviews")) return RateLimitPlan.USER_REVIEW;
        if (uri.startsWith("/api/v1/comments")) return RateLimitPlan.USER_COMMENT;
        if (uri.startsWith("/api/v1/auth/profile")) return RateLimitPlan.USER_PROFILE;
        if (uri.startsWith("/api/v1/products")) return RateLimitPlan.PUBLIC_PRODUCT;
        return RateLimitPlan.GLOBAL;
    }

    private String determineKey(String clientIp, RateLimitPlan plan) {
        // USER_* plans: key theo principal name (email trong codebase này) nếu đã auth,
        // fallback IP nếu anonymous. Các plan khác: key theo IP.
        boolean requiresUser = plan.name().startsWith("USER_");
        if (requiresUser) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return plan.name() + "_" + auth.getName();
            }
        }
        return plan.name() + "_" + clientIp;
    }
}
