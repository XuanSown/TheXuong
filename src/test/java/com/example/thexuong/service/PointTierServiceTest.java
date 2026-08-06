package com.example.thexuong.service;

import com.example.thexuong.dto.UserLoyaltyDto;
import com.example.thexuong.entity.*;
import com.example.thexuong.repository.*;
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
class PointTierServiceTest {

    @Mock private PointTierRepository pointTierRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PointTransactionRepository pointTransactionRepository;
    @Mock private TierHistoryRepository tierHistoryRepository;
    @Mock private VoucherService voucherService;
    @Mock private UserPointsRepository userPointsRepository;

    @InjectMocks
    private PointTierService pointTierService;

    // ==================== getTierForUser ====================

    @Test
    void getTierForUser_UserNotFound_ReturnsTHUONG() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals("THUONG", pointTierService.getTierForUser(1L));
    }

    @Test
    void getTierForUser_NoMatch_ReturnsTHUONG() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(null);
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), eq(PointTransaction.Type.EARN), any())).thenReturn(null);
        
        PointTier tier = new PointTier();
        tier.setCode("VIP");
        tier.setMinTotalSpent(new BigDecimal("5000000"));
        tier.setMinTotalPoints(50);
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(tier));

        assertEquals("THUONG", pointTierService.getTierForUser(1L));
    }

    @Test
    void getTierForUser_MatchBySpent_ReturnsMatchedTier() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("6000000")); // > 5M
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), eq(PointTransaction.Type.EARN), any())).thenReturn(10L); // < 50
        
        PointTier tier = new PointTier();
        tier.setCode("VIP");
        tier.setMinTotalSpent(new BigDecimal("5000000"));
        tier.setMinTotalPoints(50);
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(tier));

        assertEquals("VIP", pointTierService.getTierForUser(1L));
    }

    @Test
    void getTierForUser_MatchByPoints_ReturnsMatchedTier() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("1000000")); // < 5M
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), eq(PointTransaction.Type.EARN), any())).thenReturn(60L); // > 50
        
        PointTier tier = new PointTier();
        tier.setCode("VIP");
        tier.setMinTotalSpent(new BigDecimal("5000000"));
        tier.setMinTotalPoints(50);
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(tier));

        assertEquals("VIP", pointTierService.getTierForUser(1L));
    }

    // ==================== getLoyaltyProgress ====================

    @Test
    void getLoyaltyProgress_UserNotFound_ReturnsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(pointTierService.getLoyaltyProgress(1L));
    }

    @Test
    void getLoyaltyProgress_NoNextTier_ReturnsDtoWithoutNextTier() {
        User user = new User();
        user.setTierCode("VIP");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(null);
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), eq(PointTransaction.Type.EARN), any())).thenReturn(null);
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.empty());

        PointTier currentTier = new PointTier();
        currentTier.setCode("VIP");
        currentTier.setName("Vip Member");
        currentTier.setMinTotalSpent(new BigDecimal("5000000"));
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(currentTier));
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(currentTier));

        UserLoyaltyDto dto = pointTierService.getLoyaltyProgress(1L);
        assertEquals("VIP", dto.getCurrentTierCode());
        assertEquals("Vip Member", dto.getCurrentTierName());
        assertEquals(0, dto.getCurrentPoints());
        assertEquals(BigDecimal.ZERO, dto.getTotalSpent365Days());
        assertNull(dto.getNextTierCode());
    }

    @Test
    void getLoyaltyProgress_NextTierExists_CalculatesRemaining() {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("1000000"));
        when(pointTransactionRepository.sumPointsByUserAndTypeSince(eq(1L), eq(PointTransaction.Type.EARN), any())).thenReturn(10L);
        
        UserPoints userPoints = new UserPoints();
        userPoints.setCurrentPoints(5);
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(userPoints));

        PointTier currentTier = new PointTier();
        currentTier.setCode("THUONG");
        currentTier.setMinTotalSpent(new BigDecimal("0"));
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(currentTier));

        PointTier nextTier = new PointTier();
        nextTier.setCode("VIP");
        nextTier.setName("Vip");
        nextTier.setMinTotalSpent(new BigDecimal("5000000"));
        nextTier.setMinTotalPoints(50);
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(currentTier, nextTier));

        UserLoyaltyDto dto = pointTierService.getLoyaltyProgress(1L);
        assertEquals("VIP", dto.getNextTierCode());
        assertEquals(new BigDecimal("4000000"), dto.getSpentRemainingToNextTier()); // 5M - 1M
        assertEquals(40, dto.getPointsRemainingToNextTier()); // 50 - 10
    }
    
    @Test
    void getLoyaltyProgress_NextTierExists_SpentExceeded_CalculatesRemainingZero() {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("6000000"));
        
        PointTier currentTier = new PointTier();
        currentTier.setCode("THUONG");
        currentTier.setMinTotalSpent(new BigDecimal("0"));
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(currentTier));

        PointTier nextTier = new PointTier();
        nextTier.setCode("VIP");
        nextTier.setName("Vip");
        nextTier.setMinTotalSpent(new BigDecimal("5000000"));
        nextTier.setMinTotalPoints(50);
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(currentTier, nextTier));

        UserLoyaltyDto dto = pointTierService.getLoyaltyProgress(1L);
        assertEquals("VIP", dto.getNextTierCode());
        assertEquals(new BigDecimal("0"), dto.getSpentRemainingToNextTier()); // Spent > min
    }

    // ==================== upgradeTierIfEligible ====================

    @Test
    void upgradeTierIfEligible_UserNotFound_ReturnsFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(pointTierService.upgradeTierIfEligible(1L));
    }

    @Test
    void upgradeTierIfEligible_NoUpgrade_ReturnsFalse() {
        User user = new User();
        user.setTierCode("VIP");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Mock getTierForUser -> THUONG
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(BigDecimal.ZERO);
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of());

        PointTier currentTier = new PointTier();
        currentTier.setCode("VIP");
        currentTier.setMinTotalSpent(new BigDecimal("5000000"));
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(currentTier));

        assertFalse(pointTierService.upgradeTierIfEligible(1L)); // newPriority (0) <= currentPriority (5M)
    }

    @Test
    void upgradeTierIfEligible_UpgradeWithoutReward_ReturnsTrue() {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PointTier currentTier = new PointTier();
        currentTier.setCode("THUONG");
        currentTier.setMinTotalSpent(BigDecimal.ZERO);
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(currentTier));

        PointTier nextTier = new PointTier();
        nextTier.setCode("VIP");
        nextTier.setMinTotalSpent(new BigDecimal("5000000"));
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(nextTier));
        
        // Force getTierForUser to return VIP
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("6000000"));
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(currentTier, nextTier));

        assertTrue(pointTierService.upgradeTierIfEligible(1L));
        assertEquals("VIP", user.getTierCode());
        assertNotNull(user.getTierPromotedAt());
        verify(userRepository).save(user);
        verify(tierHistoryRepository).save(any());
        verify(voucherService, never()).issueVoucherToUser(any(), any());
    }

    @Test
    void upgradeTierIfEligible_UpgradeWithReward_Success() throws Exception {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PointTier currentTier = new PointTier();
        currentTier.setMinTotalSpent(BigDecimal.ZERO);
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(currentTier));

        PointTier nextTier = new PointTier();
        nextTier.setCode("VIP");
        nextTier.setMinTotalSpent(new BigDecimal("5000000"));
        nextTier.setRewardVoucherId(99L);
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(nextTier));
        
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("6000000"));
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(currentTier, nextTier));

        assertTrue(pointTierService.upgradeTierIfEligible(1L));
        verify(voucherService).issueVoucherToUser(99L, 1L);
    }
    
    @Test
    void upgradeTierIfEligible_UpgradeWithRewardThrows_CatchesAndContinues() throws Exception {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PointTier currentTier = new PointTier();
        currentTier.setMinTotalSpent(BigDecimal.ZERO);
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(currentTier));

        PointTier nextTier = new PointTier();
        nextTier.setCode("VIP");
        nextTier.setMinTotalSpent(new BigDecimal("5000000"));
        nextTier.setRewardVoucherId(99L);
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(nextTier));
        
        when(orderRepository.sumTotalForPointCalcByUserSince(eq(1L), any())).thenReturn(new BigDecimal("6000000"));
        when(pointTierRepository.findAllByOrderByMinTotalSpentAsc()).thenReturn(List.of(currentTier, nextTier));

        doThrow(new RuntimeException("Voucher failed")).when(voucherService).issueVoucherToUser(99L, 1L);

        assertTrue(pointTierService.upgradeTierIfEligible(1L)); // should not throw
        verify(voucherService).issueVoucherToUser(99L, 1L);
    }

    // ==================== setFirstOrderTier ====================

    @Test
    void setFirstOrderTier_UserNotFound_DoesNothing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        pointTierService.setFirstOrderTier(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void setFirstOrderTier_TierAlreadySet_DoesNothing() {
        User user = new User();
        user.setTierCode("VIP");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        pointTierService.setFirstOrderTier(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void setFirstOrderTier_TierNull_SetsThuong() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        pointTierService.setFirstOrderTier(1L);
        assertEquals("THUONG", user.getTierCode());
        verify(userRepository).save(user);
        verify(tierHistoryRepository).save(any());
    }

    // ==================== updateTierManually ====================

    @Test
    void updateTierManually_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            pointTierService.updateTierManually(1L, "VIP", "Reason"));
        assertEquals("Không tìm thấy người dùng", ex.getMessage());
    }

    @Test
    void updateTierManually_InvalidNewTierCode_ThrowsException() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.empty());
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            pointTierService.updateTierManually(1L, "VIP", "Reason"));
        assertTrue(ex.getMessage().contains("Mã hạng không hợp lệ"));
    }

    @Test
    void updateTierManually_SameTier_ThrowsException() {
        User user = new User();
        user.setTierCode("VIP");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        PointTier tier = new PointTier();
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(tier)); // new tier
        // old tier will also be found since code is same, but the code checks equality before fetching old tier, wait it fetches old tier then checks.
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            pointTierService.updateTierManually(1L, "VIP", "Reason"));
        assertEquals("Người dùng đã ở hạng này rồi.", ex.getMessage());
    }

    @Test
    void updateTierManually_Demotion_NoVoucher() throws Exception {
        User user = new User();
        user.setTierCode("VIP");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        PointTier oldTier = new PointTier();
        oldTier.setMinTotalSpent(new BigDecimal("5000000"));
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(oldTier));
        
        PointTier newTier = new PointTier();
        newTier.setMinTotalSpent(BigDecimal.ZERO); // Demotion to THUONG
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(newTier));

        pointTierService.updateTierManually(1L, "THUONG", "Demoted");

        assertEquals("THUONG", user.getTierCode());
        verify(userRepository).save(user);
        verify(voucherService, never()).issueVoucherToUser(any(), any());
    }

    @Test
    void updateTierManually_PromotionWithRewardVoucher_IssuesVoucher() throws Exception {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        PointTier oldTier = new PointTier();
        oldTier.setMinTotalSpent(BigDecimal.ZERO);
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(oldTier));
        
        PointTier newTier = new PointTier();
        newTier.setMinTotalSpent(new BigDecimal("5000000")); // Promotion
        newTier.setRewardVoucherId(99L);
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(newTier));

        pointTierService.updateTierManually(1L, "VIP", "Promoted");

        assertEquals("VIP", user.getTierCode());
        verify(voucherService).issueVoucherToUser(99L, 1L);
    }
    
    @Test
    void updateTierManually_PromotionWithRewardThrows_CatchesAndContinues() throws Exception {
        User user = new User();
        user.setTierCode("THUONG");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        PointTier oldTier = new PointTier();
        oldTier.setMinTotalSpent(BigDecimal.ZERO);
        when(pointTierRepository.findByCode("THUONG")).thenReturn(Optional.of(oldTier));
        
        PointTier newTier = new PointTier();
        newTier.setMinTotalSpent(new BigDecimal("5000000")); // Promotion
        newTier.setRewardVoucherId(99L);
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(newTier));

        doThrow(new RuntimeException("Error")).when(voucherService).issueVoucherToUser(99L, 1L);

        assertDoesNotThrow(() -> pointTierService.updateTierManually(1L, "VIP", "Promoted"));
        verify(voucherService).issueVoucherToUser(99L, 1L);
    }
}
