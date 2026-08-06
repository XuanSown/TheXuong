package com.example.thexuong.service;

import com.example.thexuong.entity.PointTransaction;
import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserPoints;
import com.example.thexuong.exception.PointBalanceException;
import com.example.thexuong.repository.PointTransactionRepository;
import com.example.thexuong.repository.UserPointsRepository;
import com.example.thexuong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock private UserPointsRepository userPointsRepository;
    @Mock private PointTransactionRepository pointTransactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private PointService pointService;

    private UserPoints mockPoints;

    @BeforeEach
    void setUp() {
        mockPoints = UserPoints.builder()
                .userId(1L)
                .currentPoints(500)
                .totalEarned(1000L)
                .totalSpent(500L)
                .lastActivityAt(LocalDateTime.now())
                .build();
    }

    // ==================== earnPoints ====================
    @Test
    void earnPoints_AmountNullOrZero_ReturnsZero() {
        assertEquals(0, pointService.earnPoints(1L, 100L, null, "test"));
        assertEquals(0, pointService.earnPoints(1L, 100L, BigDecimal.ZERO, "test"));
        assertEquals(0, pointService.earnPoints(1L, 100L, new BigDecimal("-10"), "test"));
    }

    @Test
    void earnPoints_AmountTooSmall_ReturnsZero() {
        // 99,000 / 100,000 = 0
        assertEquals(0, pointService.earnPoints(1L, 100L, new BigDecimal("99000"), "test"));
    }

    @Test
    void earnPoints_Success() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        when(userPointsRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pointTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // 250,000 / 100,000 = 2 points
        int earned = pointService.earnPoints(1L, 100L, new BigDecimal("250000"), "Earn");
        
        assertEquals(2, earned);
        assertEquals(502, mockPoints.getCurrentPoints());
        assertEquals(1002L, mockPoints.getTotalEarned());
        verify(pointTransactionRepository, times(1)).save(any(PointTransaction.class));
    }

    // ==================== spendPoints ====================
    @Test
    void spendPoints_PointsNegative_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> pointService.spendPoints(1L, -5, "test"));
        assertThrows(IllegalArgumentException.class, () -> pointService.spendPoints(1L, 0, "test"));
    }

    @Test
    void spendPoints_NoPointsRecord_ThrowsException() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(PointBalanceException.class, () -> pointService.spendPoints(1L, 50, "test"));
    }

    @Test
    void spendPoints_InsufficientPoints_ThrowsException() {
        mockPoints.setCurrentPoints(10);
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        assertThrows(PointBalanceException.class, () -> pointService.spendPoints(1L, 50, "test"));
    }

    @Test
    void spendPoints_Success() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        when(userPointsRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        int remaining = pointService.spendPoints(1L, 100, "Spend");
        
        assertEquals(400, remaining);
        assertEquals(400, mockPoints.getCurrentPoints());
        assertEquals(600L, mockPoints.getTotalSpent());
        verify(pointTransactionRepository, times(1)).save(any(PointTransaction.class));
    }

    // ==================== reversePoints ====================
    @Test
    void reversePoints_NoEarnTxs_ReturnsEarly() {
        when(pointTransactionRepository.findEarnTransactionsByOrderId(100L)).thenReturn(Collections.emptyList());
        pointService.reversePoints(100L, "Reverse");
        verify(userPointsRepository, never()).save(any());
    }

    @Test
    void reversePoints_Success() {
        PointTransaction earnTx = PointTransaction.builder().userId(1L).orderId(100L).points(20).build();
        when(pointTransactionRepository.findEarnTransactionsByOrderId(100L)).thenReturn(List.of(earnTx));
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        pointService.reversePoints(100L, "Reverse");
        
        assertEquals(480, mockPoints.getCurrentPoints()); // 500 - 20
        assertEquals(520L, mockPoints.getTotalSpent()); // 500 + 20
        verify(pointTransactionRepository, times(1)).save(any(PointTransaction.class));
    }

    @Test
    void reversePoints_NotEnoughToReverse_DeductsToZero() {
        PointTransaction earnTx = PointTransaction.builder().userId(1L).orderId(100L).points(1000).build();
        when(pointTransactionRepository.findEarnTransactionsByOrderId(100L)).thenReturn(List.of(earnTx));
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        pointService.reversePoints(100L, "Reverse");
        
        assertEquals(0, mockPoints.getCurrentPoints()); // 500 - 1000 -> 0
        assertEquals(1000L, mockPoints.getTotalSpent()); // 500 + 500 (actual reversed)
    }

    // ==================== refundSpentPoints ====================
    @Test
    void refundSpentPoints_InvalidInput_ReturnsEarly() {
        pointService.refundSpentPoints(1L, 0, 100L);
        pointService.refundSpentPoints(null, 50, 100L);
        verify(userPointsRepository, never()).save(any());
    }

    @Test
    void refundSpentPoints_Success() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        pointService.refundSpentPoints(1L, 50, 100L);
        
        assertEquals(550, mockPoints.getCurrentPoints()); // 500 + 50
        assertEquals(450L, mockPoints.getTotalSpent()); // 500 - 50
        verify(pointTransactionRepository, times(1)).save(any(PointTransaction.class));
    }

    // ==================== adjustPoints ====================
    @Test
    void adjustPoints_NoNote_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> pointService.adjustPoints(99L, 1L, 50, ""));
    }

    @Test
    void adjustPoints_PositiveDelta_Success() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        int newPoints = pointService.adjustPoints(99L, 1L, 50, "Gift");
        
        assertEquals(550, newPoints);
        assertEquals(1050L, mockPoints.getTotalEarned());
        assertEquals(500L, mockPoints.getTotalSpent()); // unchanged
        verify(pointTransactionRepository, times(1)).save(any(PointTransaction.class));
    }

    @Test
    void adjustPoints_NegativeDelta_Success() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        int newPoints = pointService.adjustPoints(99L, 1L, -100, "Penalty");
        
        assertEquals(400, newPoints);
        assertEquals(1000L, mockPoints.getTotalEarned()); // unchanged
        assertEquals(600L, mockPoints.getTotalSpent()); // 500 + 100
        verify(pointTransactionRepository, times(1)).save(any(PointTransaction.class));
    }

    @Test
    void adjustPoints_NegativeDelta_DeductsToZero() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints)); // has 500
        
        int newPoints = pointService.adjustPoints(99L, 1L, -1000, "Penalty");
        
        assertEquals(0, newPoints);
        assertEquals(1000L, mockPoints.getTotalSpent()); // 500 + 500 actual delta
    }

    // ==================== expireOldPoints ====================
    @Test
    void expireOldPoints_UserNotFound_Continues() {
        PointTransaction expiredTx = PointTransaction.builder().userId(1L).points(50).build();
        when(pointTransactionRepository.findExpiredEarnTransactions(any())).thenReturn(List.of(expiredTx));
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.empty()); // User record gone
        
        int expired = pointService.expireOldPoints(LocalDateTime.now());
        
        assertEquals(0, expired);
        verify(userPointsRepository, never()).save(any());
    }

    @Test
    void expireOldPoints_Success() {
        PointTransaction expiredTx = PointTransaction.builder().userId(1L).orderId(100L).points(50).build();
        when(pointTransactionRepository.findExpiredEarnTransactions(any())).thenReturn(List.of(expiredTx));
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        int expired = pointService.expireOldPoints(LocalDateTime.now());
        
        assertEquals(50, expired);
        assertEquals(450, mockPoints.getCurrentPoints());
        assertEquals(550L, mockPoints.getTotalSpent());
        verify(pointTransactionRepository, times(1)).save(any(PointTransaction.class)); // Expire Tx saved
    }

    @Test
    void expireOldPoints_NothingToExpire() {
        PointTransaction expiredTx = PointTransaction.builder().userId(1L).orderId(100L).points(50).build();
        when(pointTransactionRepository.findExpiredEarnTransactions(any())).thenReturn(List.of(expiredTx));
        mockPoints.setCurrentPoints(0); // Nothing to expire
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        int expired = pointService.expireOldPoints(LocalDateTime.now());
        
        assertEquals(0, expired);
        verify(userPointsRepository, never()).save(any());
        verify(pointTransactionRepository, never()).save(any(PointTransaction.class));
    }

    @Test
    void reversePoints_UserNotFound_ThrowsException() {
        PointTransaction earnTx = PointTransaction.builder().userId(1L).orderId(100L).points(20).build();
        when(pointTransactionRepository.findEarnTransactionsByOrderId(100L)).thenReturn(List.of(earnTx));
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> pointService.reversePoints(100L, "Reverse"));
    }

    // ==================== getOrCreateUserPoints ====================
    @Test
    void getOrCreate_NotExists_CreatesNew() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        User mockUser = new User();
        when(userRepository.getReferenceById(1L)).thenReturn(mockUser);
        
        UserPoints newPoints = UserPoints.builder().userId(1L).currentPoints(0).totalEarned(0L).totalSpent(0L).build();
        when(userPointsRepository.save(any(UserPoints.class))).thenReturn(newPoints);
        
        UserPoints res = pointService.getOrCreateUserPoints(1L);
        
        assertNotNull(res);
        assertEquals(0, res.getCurrentPoints());
    }

    @Test
    void getOrCreate_Exists_ReturnsExisting() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        
        UserPoints res = pointService.getOrCreateUserPoints(1L);
        
        assertNotNull(res);
        assertEquals(500, res.getCurrentPoints());
        verify(userPointsRepository, never()).save(any());
    }

    // ==================== getCurrentPoints ====================
    @Test
    void getCurrentPoints_NotExists_ReturnsZero() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertEquals(0, pointService.getCurrentPoints(1L));
    }

    @Test
    void getCurrentPoints_Exists_ReturnsAmount() {
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPoints));
        assertEquals(500, pointService.getCurrentPoints(1L));
    }

    // ==================== getHistory ====================
    @Test
    void getHistory_CallsRepo() {
        pointService.getHistory(1L);
        verify(pointTransactionRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }
}
