package com.example.thexuong.security.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitInterceptorTest {
    @Test
    public void testRegisterRateLimiting() throws Exception {
        RateLimitService service = new RateLimitService();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(service);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/auth/register");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        // 3 requests allowed for AUTH_REGISTER
        assertTrue(interceptor.preHandle(req, res, null));
        assertTrue(interceptor.preHandle(req, res, null));
        assertTrue(interceptor.preHandle(req, res, null));

        // 4th request should fail
        assertFalse(interceptor.preHandle(req, res, null));
        assertEquals(429, res.getStatus());
    }

    @Test
    public void testLoginRateLimiting() throws Exception {
        RateLimitService service = new RateLimitService();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(service);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/auth/login");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        // 5 requests allowed for AUTH_LOGIN
        for (int i = 0; i < 5; i++) {
            assertTrue(interceptor.preHandle(req, res, null));
        }

        // 6th request should fail
        assertFalse(interceptor.preHandle(req, res, null));
        assertEquals(429, res.getStatus());
    }
}
