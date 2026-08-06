package com.example.thexuong.service;

import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private InventoryService inventoryService;

    // ==================== deductStock ====================

    @Test
    void deductStock_QuantityZeroOrNegative_ReturnsEarly() {
        inventoryService.deductStock(1L, 2L, 0);
        inventoryService.deductStock(1L, 2L, -5);

        verify(productVariantRepository, never()).findByProductIdAndSizeId(any(), any());
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void deductStock_VariantNotFound_ThrowsException() {
        when(productVariantRepository.findByProductIdAndSizeId(1L, 2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                inventoryService.deductStock(1L, 2L, 5));
        assertTrue(ex.getMessage().contains("Khong tim thay variant"));
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void deductStock_InsufficientStock_ThrowsException() {
        ProductVariant mockVariant = new ProductVariant();
        mockVariant.setQuantity(3);
        when(productVariantRepository.findByProductIdAndSizeId(1L, 2L)).thenReturn(Optional.of(mockVariant));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                inventoryService.deductStock(1L, 2L, 5));
        assertTrue(ex.getMessage().contains("Khong du ton kho: con 3, can 5"));
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void deductStock_CurrentQuantityNull_DefaultsToZeroAndThrowsException() {
        ProductVariant mockVariant = new ProductVariant();
        mockVariant.setQuantity(null);
        when(productVariantRepository.findByProductIdAndSizeId(1L, 2L)).thenReturn(Optional.of(mockVariant));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                inventoryService.deductStock(1L, 2L, 1));
        assertTrue(ex.getMessage().contains("Khong du ton kho: con 0, can 1"));
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void deductStock_Success_DecreasesStockAndSaves() {
        ProductVariant mockVariant = new ProductVariant();
        mockVariant.setQuantity(10);
        when(productVariantRepository.findByProductIdAndSizeId(1L, 2L)).thenReturn(Optional.of(mockVariant));

        inventoryService.deductStock(1L, 2L, 4);

        assertEquals(6, mockVariant.getQuantity());
        verify(productVariantRepository, times(1)).save(mockVariant);
    }

    // ==================== restoreStock ====================

    @Test
    void restoreStock_QuantityZeroOrNegative_ReturnsEarly() {
        inventoryService.restoreStock(1L, 2L, 0);
        inventoryService.restoreStock(1L, 2L, -3);

        verify(productVariantRepository, never()).findByProductIdAndSizeId(any(), any());
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void restoreStock_VariantNotFound_LogsWarnAndReturnsEarly() {
        when(productVariantRepository.findByProductIdAndSizeId(1L, 2L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> inventoryService.restoreStock(1L, 2L, 5));
        
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void restoreStock_CurrentQuantityNull_DefaultsToZeroAndAdds() {
        ProductVariant mockVariant = new ProductVariant();
        mockVariant.setQuantity(null);
        when(productVariantRepository.findByProductIdAndSizeId(1L, 2L)).thenReturn(Optional.of(mockVariant));

        inventoryService.restoreStock(1L, 2L, 5);

        assertEquals(5, mockVariant.getQuantity());
        verify(productVariantRepository, times(1)).save(mockVariant);
    }

    @Test
    void restoreStock_Success_IncreasesStockAndSaves() {
        ProductVariant mockVariant = new ProductVariant();
        mockVariant.setQuantity(10);
        when(productVariantRepository.findByProductIdAndSizeId(1L, 2L)).thenReturn(Optional.of(mockVariant));

        inventoryService.restoreStock(1L, 2L, 7);

        assertEquals(17, mockVariant.getQuantity());
        verify(productVariantRepository, times(1)).save(mockVariant);
    }
}
