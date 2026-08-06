package com.example.thexuong.service;

import com.example.thexuong.config.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @Mock
    private VNPayConfig vnPayConfig;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private VNPayService vnPayService;

    @Test
    void createOrder_Success() {
        // Setup config mocks
        when(vnPayConfig.getVnp_TmnCode()).thenReturn("TMN_TEST");
        when(vnPayConfig.getVnp_ReturnUrl()).thenReturn("http://return.url");
        when(vnPayConfig.getSecretKey()).thenReturn("SECRET_KEY");
        when(vnPayConfig.getVnp_PayUrl()).thenReturn("http://pay.url");
        when(vnPayConfig.hmacSHA512(eq("SECRET_KEY"), anyString())).thenReturn("HASH_123");

        try (MockedStatic<VNPayConfig> mockedStatic = mockStatic(VNPayConfig.class)) {
            mockedStatic.when(() -> VNPayConfig.getRandomNumber(8)).thenReturn("12345678");
            mockedStatic.when(() -> VNPayConfig.getIpAddress(request)).thenReturn("127.0.0.1");

            String url = vnPayService.createOrder(10000, "Thanh toan test", request);

            assertTrue(url.startsWith("http://pay.url?"));
            assertTrue(url.contains("vnp_Version=2.1.0"));
            assertTrue(url.contains("vnp_Command=pay"));
            assertTrue(url.contains("vnp_TmnCode=TMN_TEST"));
            assertTrue(url.contains("vnp_Amount=1000000")); // 10000 * 100
            assertTrue(url.contains("vnp_CurrCode=VND"));
            assertTrue(url.contains("vnp_BankCode=NCB"));
            assertTrue(url.contains("vnp_TxnRef=12345678"));
            assertTrue(url.contains("vnp_OrderInfo=Thanh+toan+test")); // URL encoded
            assertTrue(url.contains("vnp_OrderType=other"));
            assertTrue(url.contains("vnp_Locale=vn"));
            assertTrue(url.contains("vnp_ReturnUrl=http%3A%2F%2Freturn.url")); // URL encoded
            assertTrue(url.contains("vnp_IpAddr=127.0.0.1"));
            assertTrue(url.contains("vnp_CreateDate="));
            assertTrue(url.contains("vnp_ExpireDate="));
            assertTrue(url.contains("vnp_SecureHash=HASH_123"));
        }
    }

    @Test
    void createOrder_EmptyOrderInfo_SkipsEmptyValues() {
        // Setup config mocks
        when(vnPayConfig.getVnp_TmnCode()).thenReturn("TMN_TEST");
        when(vnPayConfig.getVnp_ReturnUrl()).thenReturn("http://return.url");
        when(vnPayConfig.getSecretKey()).thenReturn("SECRET_KEY");
        when(vnPayConfig.getVnp_PayUrl()).thenReturn("http://pay.url");
        when(vnPayConfig.hmacSHA512(eq("SECRET_KEY"), anyString())).thenReturn("HASH_123");

        try (MockedStatic<VNPayConfig> mockedStatic = mockStatic(VNPayConfig.class)) {
            mockedStatic.when(() -> VNPayConfig.getRandomNumber(8)).thenReturn("12345678");
            mockedStatic.when(() -> VNPayConfig.getIpAddress(request)).thenReturn("127.0.0.1");

            // Empty order info
            String url = vnPayService.createOrder(10000, "", request);

            assertTrue(url.startsWith("http://pay.url?"));
            assertTrue(url.contains("vnp_Amount=1000000")); 
            // Should not contain empty parameters
            assertFalse(url.contains("vnp_OrderInfo="));
            assertTrue(url.contains("vnp_SecureHash=HASH_123"));
        }
    }
}
