package com.example.thexuong.service;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.SizeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SizeServiceTest {

    @Mock private SizeRepository sizeRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private SizeService sizeService;

    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setId(100L);
    }

    // ==================== parseSizeQuantities ====================

    @Test
    void parseSizeQuantities_NullOrBlank_ReturnsEmptyMap() {
        assertTrue(sizeService.parseSizeQuantities(null).isEmpty());
        assertTrue(sizeService.parseSizeQuantities("   ").isEmpty());
    }

    @Test
    void parseSizeQuantities_InvalidJson_CatchesExceptionReturnsEmptyMap() {
        Map<String, Integer> res = sizeService.parseSizeQuantities("{invalid_json");
        assertTrue(res.isEmpty());
    }

    @Test
    void parseSizeQuantities_ValidJson_ReturnsMap() {
        Map<String, Integer> res = sizeService.parseSizeQuantities("{\"L\": 10, \"XL\": 5}");
        assertEquals(2, res.size());
        assertEquals(10, res.get("L"));
        assertEquals(5, res.get("XL"));
    }

    // ==================== createVariants ====================

    @Test
    void createVariants_EmptyMap_ReturnsEmptyList() {
        List<ProductVariant> res = sizeService.createVariants(mockProduct, Collections.emptyMap());
        assertTrue(res.isEmpty());
        verify(sizeRepository, never()).findByName(any());
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void createVariants_SizeNotFound_IgnoresAndContinues() {
        Map<String, Integer> map = new HashMap<>();
        map.put("S", 10);
        map.put("M", 20);

        when(sizeRepository.findByName("S")).thenReturn(Optional.empty()); // S not found
        Size mockSizeM = new Size();
        mockSizeM.setName("M");
        when(sizeRepository.findByName("M")).thenReturn(Optional.of(mockSizeM)); // M found
        when(productVariantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ProductVariant> res = sizeService.createVariants(mockProduct, map);

        assertEquals(1, res.size()); // Only M was created
        assertEquals("100-M", res.get(0).getSku());
        assertEquals(20, res.get(0).getQuantity());
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void createVariants_NullQuantity_DefaultsToZero() {
        Map<String, Integer> map = new HashMap<>();
        map.put("L", null);

        Size mockSizeL = new Size();
        mockSizeL.setName("L");
        when(sizeRepository.findByName("L")).thenReturn(Optional.of(mockSizeL));
        when(productVariantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ProductVariant> res = sizeService.createVariants(mockProduct, map);

        assertEquals(1, res.size());
        assertEquals(0, res.get(0).getQuantity()); // default to 0
    }

    @Test
    void createVariants_Success_SavesVariantsAndReturnsList() {
        Map<String, Integer> map = new HashMap<>();
        map.put("XXL", 50);

        Size mockSize = new Size();
        mockSize.setName("XXL");
        when(sizeRepository.findByName("XXL")).thenReturn(Optional.of(mockSize));
        when(productVariantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ProductVariant> res = sizeService.createVariants(mockProduct, map);

        assertEquals(1, res.size());
        assertEquals("100-XXL", res.get(0).getSku());
        assertEquals(50, res.get(0).getQuantity());
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }

    // ==================== updateVariants ====================

    @Test
    void updateVariants_ProductNotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            sizeService.updateVariants(99L, Collections.emptyMap()));
        assertTrue(ex.getMessage().contains("Khong tim thay san pham: 99"));
        
        verify(productVariantRepository, times(1)).deleteByProductId(99L);
        verify(sizeRepository, never()).findByName(any());
    }

    @Test
    void updateVariants_Success_DeletesOldAndCreatesNew() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));
        Map<String, Integer> map = new HashMap<>();
        map.put("M", 10);
        
        Size mockSize = new Size();
        mockSize.setName("M");
        when(sizeRepository.findByName("M")).thenReturn(Optional.of(mockSize));
        when(productVariantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        sizeService.updateVariants(100L, map);

        verify(productVariantRepository, times(1)).deleteByProductId(100L);
        verify(sizeRepository, times(1)).findByName("M");
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }
}
