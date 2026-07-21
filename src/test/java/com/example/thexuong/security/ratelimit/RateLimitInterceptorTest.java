package com.example.thexuong.security.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void testUserOrderAuthenticatedKeying() throws Exception {
        RateLimitService service = new RateLimitService();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(service);
        try {
            Authentication alice = mock(Authentication.class);
            when(alice.getName()).thenReturn("alice@example.com");
            when(alice.getPrincipal()).thenReturn("alice@example.com");
            when(alice.isAuthenticated()).thenReturn(true);
            SecurityContextHolder.getContext().setAuthentication(alice);

            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRequestURI("/api/v1/orders");
            req.setRemoteAddr("127.0.0.1");

            // 5 requests allowed for USER_ORDER (Alice's bucket)
            MockHttpServletResponse res = new MockHttpServletResponse();
            for (int i = 0; i < 5; i++) {
                assertTrue(interceptor.preHandle(req, res, null));
            }
            // 6th request should fail
            assertFalse(interceptor.preHandle(req, res, null));
            assertEquals(429, res.getStatus());

            // Bob gets his own bucket, independent of Alice, same IP
            SecurityContextHolder.clearContext();
            Authentication bob = mock(Authentication.class);
            when(bob.getName()).thenReturn("bob@example.com");
            when(bob.getPrincipal()).thenReturn("bob@example.com");
            when(bob.isAuthenticated()).thenReturn(true);
            SecurityContextHolder.getContext().setAuthentication(bob);

            MockHttpServletResponse res2 = new MockHttpServletResponse();
            for (int i = 0; i < 5; i++) {
                assertTrue(interceptor.preHandle(req, res2, null));
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testUserOrderFallsBackToIpWhenAnonymous() throws Exception {
        RateLimitService service = new RateLimitService();
        RateLimitInterceptor interceptor = new RateLimitInterceptor(service);
        try {
            Authentication anon = mock(Authentication.class);
            when(anon.getName()).thenReturn("anonymousUser");
            when(anon.getPrincipal()).thenReturn("anonymousUser");
            when(anon.isAuthenticated()).thenReturn(true);
            SecurityContextHolder.getContext().setAuthentication(anon);

            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRequestURI("/api/v1/orders");
            req.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse res = new MockHttpServletResponse();

            // 5 requests allowed (IP fallback bucket)
            for (int i = 0; i < 5; i++) {
                assertTrue(interceptor.preHandle(req, res, null));
            }
            // 6th request should fail
            assertFalse(interceptor.preHandle(req, res, null));
            assertEquals(429, res.getStatus());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
