package com.example.thexuong.service;

import com.example.thexuong.dto.*;
import com.example.thexuong.entity.*;
import com.example.thexuong.enums.BulkAction;
import com.example.thexuong.exception.PointBalanceException;
import com.example.thexuong.exception.VoucherInvalidException;
import com.example.thexuong.repository.*;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock private VoucherRepository voucherRepository;
    @Mock private UserVoucherRepository userVoucherRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserPointsRepository userPointsRepository;
    @Mock private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private VoucherService voucherService;

    private Voucher mockCatalog;
    private User mockUser;
    private UserPoints mockUserPoints;
    private UserVoucher mockUserVoucher;

    @BeforeEach
    void setUp() {
        mockCatalog = Voucher.builder()
                .id(1L)
                .code("TX-CAT-100K")
                .discountAmount(new BigDecimal("100000"))
                .requiredPoints(500)
                .minOrderAmount(new BigDecimal("300000"))
                .vipOnly(false)
                .status(Voucher.Status.ACTIVE)
                .build();

        mockUser = new User();
        mockUser.setId(10L);
        mockUser.setRole("CUSTOMER");

        mockUserPoints = UserPoints.builder()
                .userId(10L)
                .currentPoints(1000)
                .totalSpent(0L)
                .build();

        mockUserVoucher = UserVoucher.builder()
                .id(100L)
                .userId(10L)
                .voucherId(1L)
                .code("TX-ABCDEF")
                .status(UserVoucher.Status.UNUSED)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    // ==================== generateUniqueCode ====================
    @Test
    void generateUniqueCode_SuccessFirstTry() {
        when(userVoucherRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(voucherRepository.findByCode(anyString())).thenReturn(Optional.empty());

        String code = voucherService.generateUniqueCode();
        assertTrue(code.startsWith("TX-"));
        assertEquals(9, code.length());
    }

    @Test
    void generateUniqueCode_Fails10Times_ThrowsException() {
        when(userVoucherRepository.findByCode(anyString())).thenReturn(Optional.of(new UserVoucher()));
        
        assertThrows(RuntimeException.class, () -> voucherService.generateUniqueCode());
    }

    // ==================== redeemVoucher ====================
    @Test
    void redeemVoucher_CatalogNotFound_ThrowsException() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(VoucherInvalidException.class, () -> voucherService.redeemVoucher(10L, 1L));
    }

    @Test
    void redeemVoucher_CatalogNotActive_ThrowsException() {
        mockCatalog.setStatus(Voucher.Status.LOCKED);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        assertThrows(VoucherInvalidException.class, () -> voucherService.redeemVoucher(10L, 1L));
    }

    @Test
    void redeemVoucher_VipOnlyButUserNotVip_ThrowsException() {
        mockCatalog.setVipOnly(true);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        when(userRepository.findById(10L)).thenReturn(Optional.of(mockUser)); // role CUSTOMER

        assertThrows(VoucherInvalidException.class, () -> voucherService.redeemVoucher(10L, 1L));
    }

    @Test
    void redeemVoucher_InsufficientPoints_ThrowsException() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        mockUserPoints.setCurrentPoints(100); // Need 500
        when(userPointsRepository.findByUserId(10L)).thenReturn(Optional.of(mockUserPoints));

        assertThrows(PointBalanceException.class, () -> voucherService.redeemVoucher(10L, 1L));
    }

    @Test
    void redeemVoucher_NegativePoints_ThrowsException() {
        mockCatalog.setRequiredPoints(-10);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        
        assertThrows(IllegalArgumentException.class, () -> voucherService.redeemVoucher(10L, 1L));
    }

    @Test
    void redeemVoucher_Success() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        when(userPointsRepository.findByUserId(10L)).thenReturn(Optional.of(mockUserPoints));
        when(userVoucherRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(voucherRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(userVoucherRepository.save(any(UserVoucher.class))).thenAnswer(i -> i.getArgument(0));

        UserVoucher res = voucherService.redeemVoucher(10L, 1L);

        assertNotNull(res);
        assertEquals(UserVoucher.Status.UNUSED, res.getStatus());
        assertEquals(500, mockUserPoints.getCurrentPoints()); // 1000 - 500
        verify(pointTransactionRepository, times(1)).save(any());
        verify(userVoucherRepository, times(1)).save(any());
    }

    // ==================== issueVoucherToUser ====================
    @Test
    void issueVoucherToUser_Success() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        when(userVoucherRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(voucherRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(userVoucherRepository.save(any(UserVoucher.class))).thenAnswer(i -> i.getArgument(0));

        UserVoucher res = voucherService.issueVoucherToUser(1L, 10L);

        assertNotNull(res);
        assertEquals(UserVoucher.Status.UNUSED, res.getStatus());
        verify(userPointsRepository, never()).findByUserId(anyLong()); // No points deducted
    }

    // ==================== validateAndGetDiscount ====================
    @Test
    void validate_CodeNotFound_ThrowsException() {
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.empty());
        assertThrows(VoucherInvalidException.class, () -> 
            voucherService.validateAndGetDiscount("TX-ABCDEF", 10L, new BigDecimal("500000")));
    }

    @Test
    void validate_WrongUser_ThrowsException() {
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        assertThrows(VoucherInvalidException.class, () -> 
            voucherService.validateAndGetDiscount("TX-ABCDEF", 99L, new BigDecimal("500000")));
    }

    @Test
    void validate_StatusUsed_ThrowsException() {
        mockUserVoucher.setStatus(UserVoucher.Status.USED);
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        assertThrows(VoucherInvalidException.class, () -> 
            voucherService.validateAndGetDiscount("TX-ABCDEF", 10L, new BigDecimal("500000")));
    }

    @Test
    void validate_ExpiredTime_ThrowsException() {
        mockUserVoucher.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        assertThrows(VoucherInvalidException.class, () -> 
            voucherService.validateAndGetDiscount("TX-ABCDEF", 10L, new BigDecimal("500000")));
    }

    @Test
    void validate_MinOrderNotMet_ThrowsException() {
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        
        assertThrows(VoucherInvalidException.class, () -> 
            voucherService.validateAndGetDiscount("TX-ABCDEF", 10L, new BigDecimal("200000"))); // Min 300k
    }

    @Test
    void validate_Success() {
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        
        BigDecimal discount = voucherService.validateAndGetDiscount("TX-ABCDEF", 10L, new BigDecimal("500000"));
        
        assertEquals(new BigDecimal("100000"), discount);
    }

    // ==================== markAsUsed ====================
    @Test
    void markAsUsed_NullCode_DoesNothing() {
        voucherService.markAsUsed(null, 1L);
        verify(userVoucherRepository, never()).findByCode(anyString());
    }

    @Test
    void markAsUsed_NotUnused_ThrowsException() {
        mockUserVoucher.setStatus(UserVoucher.Status.EXPIRED);
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        assertThrows(VoucherInvalidException.class, () -> voucherService.markAsUsed("TX-ABCDEF", 1L));
    }

    @Test
    void markAsUsed_Success() {
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        voucherService.markAsUsed("TX-ABCDEF", 999L);
        assertEquals(UserVoucher.Status.USED, mockUserVoucher.getStatus());
        assertEquals(999L, mockUserVoucher.getUsedInOrderId());
        verify(userVoucherRepository, times(1)).save(mockUserVoucher);
    }

    // ==================== restoreVoucher ====================
    @Test
    void restoreVoucher_Used_Restores() {
        mockUserVoucher.setStatus(UserVoucher.Status.USED);
        when(userVoucherRepository.findByCode("TX-ABCDEF")).thenReturn(Optional.of(mockUserVoucher));
        
        voucherService.restoreVoucher("TX-ABCDEF");
        
        assertEquals(UserVoucher.Status.UNUSED, mockUserVoucher.getStatus());
        assertNull(mockUserVoucher.getUsedInOrderId());
        verify(userVoucherRepository, times(1)).save(mockUserVoucher);
    }

    // ==================== expireOldVouchers ====================
    @Test
    void expireOldVouchers_UpdatesStatus() {
        when(userVoucherRepository.findExpiredUnusedVouchers(any())).thenReturn(List.of(mockUserVoucher));
        
        int count = voucherService.expireOldVouchers(LocalDateTime.now());
        
        assertEquals(1, count);
        assertEquals(UserVoucher.Status.EXPIRED, mockUserVoucher.getStatus());
        verify(userVoucherRepository, times(1)).save(mockUserVoucher);
    }

    // ==================== Admin / CRUD ====================
    @Test
    void createVoucher_ExistsCode_ThrowsException() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setCode("EXISTING");
        when(voucherRepository.existsByCode("EXISTING")).thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> voucherService.createVoucher(req, "admin"));
    }

    @Test
    void createVoucher_AutoCode_Success() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setDiscountAmount(new BigDecimal("50000"));
        
        when(voucherRepository.existsByCode(anyString())).thenReturn(false);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(i -> i.getArgument(0));
        
        VoucherResponse res = voucherService.createVoucher(req, "admin");
        
        assertNotNull(res);
        assertTrue(res.getCode().startsWith("TX-CAT-"));
    }

    @Test
    void updateVoucher_Success() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(i -> i.getArgument(0));
        
        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setDiscountAmount(new BigDecimal("99000"));
        req.setStatus("LOCKED");
        
        VoucherResponse res = voucherService.updateVoucher(1L, req, "admin");
        
        assertEquals(new BigDecimal("99000"), res.getDiscountAmount());
        assertEquals("LOCKED", res.getStatus());
    }

    @Test
    void deleteVoucher_Claimed_SoftDelete() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        when(userVoucherRepository.countByVoucherId(1L)).thenReturn(5L); // Has claimed
        
        voucherService.deleteVoucher(1L, "admin");
        
        assertEquals(Voucher.Status.EXPIRED, mockCatalog.getStatus());
        verify(voucherRepository, times(1)).save(mockCatalog);
        verify(voucherRepository, never()).delete(any(Voucher.class));
    }

    @Test
    void deleteVoucher_NotClaimed_HardDelete() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        when(userVoucherRepository.countByVoucherId(1L)).thenReturn(0L); // No claimed
        
        voucherService.deleteVoucher(1L, "admin");
        
        verify(voucherRepository, times(1)).delete(mockCatalog);
    }

    @Test
    void bulkAction_SuccessAndFailure() {
        BulkVoucherRequest req = new BulkVoucherRequest();
        req.setIds(List.of(1L, 2L)); // 1 exists, 2 not found
        req.setAction(BulkAction.LOCK);
        
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockCatalog));
        when(voucherRepository.findById(2L)).thenReturn(Optional.empty());
        
        BulkVoucherResponse res = voucherService.bulkAction(req, "admin");
        
        assertEquals(1, res.getSuccessCount());
        assertEquals(1, res.getFailureCount());
        assertEquals(Voucher.Status.LOCKED, mockCatalog.getStatus());
    }
}
