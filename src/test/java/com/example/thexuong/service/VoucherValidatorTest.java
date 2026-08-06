package com.example.thexuong.service;

import com.example.thexuong.dto.VoucherCreateRequest;
import com.example.thexuong.dto.VoucherUpdateRequest;
import com.example.thexuong.entity.Voucher;
import com.example.thexuong.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherValidatorTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private VoucherValidator voucherValidator;

    // ==================== validateCreate ====================

    @Test
    void validateCreate_InvalidDiscount_ThrowsException() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setDiscountAmount(new BigDecimal("15000")); // Invalid

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateCreate(req));
        assertTrue(ex.getMessage().contains("Mệnh giá phải là một trong"));
    }

    @Test
    void validateCreate_InvalidPoints_ThrowsException() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setDiscountAmount(new BigDecimal("20000"));
        req.setRequiredPoints(3); // Should be 2

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateCreate(req));
        assertTrue(ex.getMessage().contains("Điểm cần phải là 2"));
    }

    @Test
    void validateCreate_MinOrderLessThanDiscount_ThrowsException() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setDiscountAmount(new BigDecimal("50000"));
        req.setRequiredPoints(5);
        req.setMinOrderAmount(new BigDecimal("40000")); // Less than discount

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateCreate(req));
        assertTrue(ex.getMessage().contains("Đơn tối thiểu phải >= mệnh giá giảm"));
    }

    @Test
    void validateCreate_CodeExists_ThrowsException() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setDiscountAmount(new BigDecimal("10000"));
        req.setRequiredPoints(1);
        req.setMinOrderAmount(new BigDecimal("10000"));
        req.setCode("EXISTING");

        when(voucherRepository.existsByCode("EXISTING")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateCreate(req));
        assertTrue(ex.getMessage().contains("Mã voucher 'EXISTING' đã tồn tại"));
    }

    @Test
    void validateCreate_InvalidStatus_ThrowsException() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setDiscountAmount(new BigDecimal("10000"));
        req.setRequiredPoints(1);
        req.setMinOrderAmount(new BigDecimal("10000"));
        req.setStatus("INVALID");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateCreate(req));
        assertTrue(ex.getMessage().contains("Status phải là một trong"));
    }

    @Test
    void validateCreate_ValidRequest_Success() {
        VoucherCreateRequest req = new VoucherCreateRequest();
        req.setDiscountAmount(new BigDecimal("100000"));
        req.setRequiredPoints(10);
        req.setMinOrderAmount(new BigDecimal("200000"));
        req.setCode("NEWCODE");
        req.setStatus("ACTIVE");

        when(voucherRepository.existsByCode("NEWCODE")).thenReturn(false);

        assertDoesNotThrow(() -> voucherValidator.validateCreate(req));
    }

    // ==================== validateUpdate ====================

    @Test
    void validateUpdate_VoucherNotFound_ThrowsException() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.empty());

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex.getMessage().contains("Không tìm thấy voucher"));
    }

    @Test
    void validateUpdate_InvalidDiscount_ThrowsException() {
        Voucher existing = new Voucher();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setDiscountAmount(new BigDecimal("999"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex.getMessage().contains("Mệnh giá phải là một trong"));
    }

    @Test
    void validateUpdate_InvalidPoints_ThrowsException() {
        Voucher existing = new Voucher();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setDiscountAmount(new BigDecimal("50000"));
        req.setRequiredPoints(10); // Should be 5

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex.getMessage().contains("Điểm cần phải là 5"));
    }

    @Test
    void validateUpdate_MinOrderLessThanNewDiscount_ThrowsException() {
        Voucher existing = new Voucher();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setDiscountAmount(new BigDecimal("100000")); // Valid
        req.setRequiredPoints(10); // Valid
        req.setMinOrderAmount(new BigDecimal("50000")); // Less than new discount 100k

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex.getMessage().contains("Đơn tối thiểu phải >="));
    }

    @Test
    void validateUpdate_MinOrderLessThanExistingDiscount_ThrowsException() {
        Voucher existing = new Voucher();
        existing.setDiscountAmount(new BigDecimal("200000"));
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setMinOrderAmount(new BigDecimal("100000")); // Less than existing discount 200k

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex.getMessage().contains("Đơn tối thiểu phải >= mệnh giá giảm (200000đ)"));
    }

    @Test
    void validateUpdate_InvalidStatus_ThrowsException() {
        Voucher existing = new Voucher();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setStatus("WRONG");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex.getMessage().contains("Status phải là một trong"));
    }

    @Test
    void validateUpdate_LockedOrExpiredWithoutAdminNote_ThrowsException() {
        Voucher existing = new Voucher();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setStatus("LOCKED");
        req.setAdminNote("   "); // blank

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex.getMessage().contains("Bắt buộc nhập ghi chú"));

        req.setStatus("EXPIRED");
        req.setAdminNote(null);
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> voucherValidator.validateUpdate(1L, req));
        assertTrue(ex2.getMessage().contains("Bắt buộc nhập ghi chú"));
    }

    @Test
    void validateUpdate_ValidRequest_Success() {
        Voucher existing = new Voucher();
        existing.setDiscountAmount(new BigDecimal("10000"));
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(existing));

        VoucherUpdateRequest req = new VoucherUpdateRequest();
        req.setStatus("LOCKED");
        req.setAdminNote("Reason");
        req.setMinOrderAmount(new BigDecimal("50000"));

        assertDoesNotThrow(() -> voucherValidator.validateUpdate(1L, req));
    }

    // ==================== generateUniqueCode ====================

    @Test
    void generateUniqueCode_Success_FirstAttempt() {
        when(voucherRepository.existsByCode(anyString())).thenReturn(false);

        String code = voucherValidator.generateUniqueCode();
        
        assertTrue(code.startsWith("TX-"));
        assertEquals(9, code.length());
        verify(voucherRepository, times(1)).existsByCode(anyString());
    }

    @Test
    void generateUniqueCode_Success_AfterRetries() {
        when(voucherRepository.existsByCode(anyString()))
            .thenReturn(true)  // attempt 1
            .thenReturn(true)  // attempt 2
            .thenReturn(false); // attempt 3

        String code = voucherValidator.generateUniqueCode();
        
        assertTrue(code.startsWith("TX-"));
        verify(voucherRepository, times(3)).existsByCode(anyString());
    }

    @Test
    void generateUniqueCode_FailsAfter5Attempts_ThrowsException() {
        when(voucherRepository.existsByCode(anyString())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> voucherValidator.generateUniqueCode());
        assertTrue(ex.getMessage().contains("Không thể generate mã voucher duy nhất sau 5 lần thử"));
        verify(voucherRepository, times(5)).existsByCode(anyString());
    }
}
