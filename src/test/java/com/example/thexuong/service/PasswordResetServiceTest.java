package com.example.thexuong.service;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");
        
        ReflectionTestUtils.setField(passwordResetService, "frontendBaseUrl", "http://localhost:5173");
    }

    // ==================== createPasswordResetToken ====================

    @Test
    void createToken_NullOrBlankEmail_DoesNothing() {
        passwordResetService.createPasswordResetToken(null);
        passwordResetService.createPasswordResetToken("  ");

        verify(userRepository, never()).findByEmail(anyString());
        verify(emailService, never()).sendPasswordResetLink(anyString(), anyString());
    }

    @Test
    void createToken_UserNotFound_DoesNothing() {
        when(userRepository.findByEmail("notfound")).thenReturn(Optional.empty());

        passwordResetService.createPasswordResetToken("notfound");

        verify(emailService, never()).sendPasswordResetLink(anyString(), anyString());
    }

    @Test
    void createToken_Success_SendsEmail() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));

        passwordResetService.createPasswordResetToken("test@gmail.com");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(emailService, times(1)).sendPasswordResetLink(emailCaptor.capture(), urlCaptor.capture());
        
        assertEquals("test@gmail.com", emailCaptor.getValue());
        assertTrue(urlCaptor.getValue().startsWith("http://localhost:5173/reset-password?token="));
        
        Map<?, ?> tokens = (Map<?, ?>) ReflectionTestUtils.getField(passwordResetService, "tokens");
        assertEquals(1, tokens.size());
    }

    @Test
    void createToken_EmailThrowsException_StillCreatesToken() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        doThrow(new RuntimeException("SMTP Error")).when(emailService).sendPasswordResetLink(anyString(), anyString());

        // Should not throw exception out to the caller
        assertDoesNotThrow(() -> passwordResetService.createPasswordResetToken("test@gmail.com"));

        // Verify token is still stored in map despite email error
        Map<?, ?> tokens = (Map<?, ?>) ReflectionTestUtils.getField(passwordResetService, "tokens");
        assertEquals(1, tokens.size());
    }

    // ==================== resetPassword ====================

    @Test
    void resetPassword_NullToken_ThrowsException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            passwordResetService.resetPassword(null, "newPass")
        );
        assertEquals("Token khong hop le.", ex.getMessage());
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            passwordResetService.resetPassword("invalid-token", "newPass")
        );
        assertEquals("Token khong hop le hoac da duoc su dung.", ex.getMessage());
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        // Setup expired token via reflection
        Map<String, Object> tokens = new ConcurrentHashMap<>();
        
        try {
            // Reconstruct the private record dynamically or use inner class if possible.
            // Since it's a private record, creating an instance from outside is tricky via standard reflection.
            // A simpler way: call createToken to get a valid token, then mutate its expiration date.
            when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
            passwordResetService.createPasswordResetToken("test@gmail.com");
            
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService, times(1)).sendPasswordResetLink(anyString(), urlCaptor.capture());
            
            String token = urlCaptor.getValue().split("token=")[1];
            
            // Now the map has the token. We need to overwrite it with an expired one.
            // The record is: private record TokenEntry(Long userId, LocalDateTime expiresAt) {}
            // We can just use constructor of that record.
            Class<?> recordClass = Class.forName("com.example.thexuong.service.PasswordResetService$TokenEntry");
            java.lang.reflect.Constructor<?> constructor = recordClass.getDeclaredConstructor(Long.class, LocalDateTime.class);
            constructor.setAccessible(true);
            Object expiredEntry = constructor.newInstance(1L, LocalDateTime.now().minusMinutes(1)); // Expired 1 min ago
            
            Map<String, Object> serviceMap = (Map<String, Object>) ReflectionTestUtils.getField(passwordResetService, "tokens");
            serviceMap.put(token, expiredEntry);
            
            RuntimeException ex = assertThrows(RuntimeException.class, () -> 
                passwordResetService.resetPassword(token, "newPass")
            );
            assertEquals("Token da het han.", ex.getMessage());
            
        } catch (Exception e) {
            fail("Reflection setup failed: " + e.getMessage());
        }
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        try {
            Class<?> recordClass = Class.forName("com.example.thexuong.service.PasswordResetService$TokenEntry");
            java.lang.reflect.Constructor<?> constructor = recordClass.getDeclaredConstructor(Long.class, LocalDateTime.class);
            constructor.setAccessible(true);
            Object validEntry = constructor.newInstance(99L, LocalDateTime.now().plusMinutes(10));
            
            Map<String, Object> serviceMap = (Map<String, Object>) ReflectionTestUtils.getField(passwordResetService, "tokens");
            serviceMap.put("valid-token", validEntry);
            
            when(userRepository.findById(99L)).thenReturn(Optional.empty());
            
            RuntimeException ex = assertThrows(RuntimeException.class, () -> 
                passwordResetService.resetPassword("valid-token", "newPass")
            );
            assertEquals("Nguoi dung khong ton tai.", ex.getMessage());
            
        } catch (Exception e) {
            fail("Reflection setup failed: " + e.getMessage());
        }
    }

    @Test
    void resetPassword_Success() {
        try {
            Class<?> recordClass = Class.forName("com.example.thexuong.service.PasswordResetService$TokenEntry");
            java.lang.reflect.Constructor<?> constructor = recordClass.getDeclaredConstructor(Long.class, LocalDateTime.class);
            constructor.setAccessible(true);
            Object validEntry = constructor.newInstance(1L, LocalDateTime.now().plusMinutes(10));
            
            Map<String, Object> serviceMap = (Map<String, Object>) ReflectionTestUtils.getField(passwordResetService, "tokens");
            serviceMap.put("valid-token", validEntry);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
            
            passwordResetService.resetPassword("valid-token", "newPass");
            
            assertEquals("encodedNewPass", mockUser.getPassword());
            verify(userRepository, times(1)).save(mockUser);
            
            // Verify token is removed
            assertNull(serviceMap.get("valid-token"));
            
        } catch (Exception e) {
            fail("Reflection setup failed: " + e.getMessage());
        }
    }
}
