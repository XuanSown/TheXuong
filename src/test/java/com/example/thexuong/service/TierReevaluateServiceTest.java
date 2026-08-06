package com.example.thexuong.service;

import com.example.thexuong.entity.TierEvaluationLog;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.TierEvaluationLogRepository;
import com.example.thexuong.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TierReevaluateServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PointTransactionRepository pointTransactionRepository;
    @Mock private TierEvaluationLogRepository tierEvaluationLogRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private TierReevaluateService tierReevaluateService;

    // ==================== reevaluateUser ====================

    @Test
    void reevaluateUser_UserNotFound_ReturnsFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(tierReevaluateService.reevaluateUser(1L));
        verifyNoInteractions(tierEvaluationLogRepository);
    }

    @Test
    void reevaluateUser_NotVip_ReturnsFalse() {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertFalse(tierReevaluateService.reevaluateUser(1L));
        verifyNoInteractions(tierEvaluationLogRepository);
    }

    @Test
    void reevaluateUser_VipRemains_DueToSpent_ReturnsFalse() {
        User user = new User();
        user.setTierCode("VIP");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("5000000")); // >= 5M
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), any(), any())).thenReturn(0L); // points don't matter now

        assertFalse(tierReevaluateService.reevaluateUser(1L));
        
        verify(tierEvaluationLogRepository).save(any(TierEvaluationLog.class));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void reevaluateUser_VipRemains_DueToPoints_ReturnsFalse() {
        User user = new User();
        user.setTierCode("VIP");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("1000000")); // < 5M
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), any(), any())).thenReturn(50L); // >= 50

        assertFalse(tierReevaluateService.reevaluateUser(1L));
        
        verify(tierEvaluationLogRepository).save(any(TierEvaluationLog.class));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void reevaluateUser_VipDowngradedToThuong_SendsEmail_ReturnsTrue() {
        User user = new User();
        user.setTierCode("VIP");
        user.setEmail("test@example.com");
        user.setFullName("Nguyen Van A");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("4000000")); // < 5M
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), any(), any())).thenReturn(40L); // < 50

        assertTrue(tierReevaluateService.reevaluateUser(1L));
        
        assertEquals("THUONG", user.getTierCode());
        assertNotNull(user.getTierPromotedAt());
        verify(tierEvaluationLogRepository).save(any(TierEvaluationLog.class));
        verify(userRepository).save(user);
        verify(emailService).sendVipDowngraded(eq("test@example.com"), eq("Nguyen Van A"), anyString());
    }
    
    @Test
    void reevaluateUser_VipDowngraded_EmailThrowsException_CatchesAndContinues() {
        User user = new User();
        user.setTierCode("VIP");
        user.setEmail("test@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(null); // Null spent
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), any(), any())).thenReturn(null); // Null points

        doThrow(new RuntimeException("Mail server down")).when(emailService).sendVipDowngraded(any(), any(), any());

        // Does not crash
        assertTrue(tierReevaluateService.reevaluateUser(1L));
        
        assertEquals("THUONG", user.getTierCode());
        verify(userRepository).save(user);
        verify(emailService).sendVipDowngraded(any(), any(), any());
    }

    // ==================== reevaluateAllActiveVip ====================

    @Test
    void reevaluateAllActiveVip_ProcessList_ReturnsCountOfChanged() {
        User user1 = new User();
        user1.setId(1L);
        user1.setTierCode("VIP");
        
        User user2 = new User();
        user2.setId(2L);
        user2.setTierCode("VIP");
        
        when(userRepository.findByTierCodeAndTierPromotedAtBefore(eq("VIP"), any(LocalDateTime.class)))
                .thenReturn(List.of(user1, user2));
                
        // Mock get for user1
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        // user1 keeps VIP
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("6000000"));
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), any(), any())).thenReturn(0L);

        // Mock get for user2
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        // user2 downgraded
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(2L), any())).thenReturn(BigDecimal.ZERO);
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(2L), any(), any())).thenReturn(0L);

        int changedCount = tierReevaluateService.reevaluateAllActiveVip();
        
        assertEquals(1, changedCount); // Only user2 was downgraded
        verify(userRepository).save(user2);
        verify(userRepository, never()).save(user1);
    }
}
