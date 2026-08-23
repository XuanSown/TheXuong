// ponytail: throws RuntimeException on insufficient stock so the @Transactional order rolls back; do not switch to boolean return — callers in OrderService rely on rollback.
package com.example.thexuong.service;

import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.exception.InsufficientStockException;
import com.example.thexuong.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public void deductStock(Long productId, Long sizeId, int quantity) {
        if (quantity <= 0) return;
        ProductVariant variant = productVariantRepository
                .findByProductIdAndSizeId(productId, sizeId)
                .orElseThrow(() -> new RuntimeException(
                        "Khong tim thay variant cho productId=" + productId + " sizeId=" + sizeId));
        int current = variant.getQuantity() != null ? variant.getQuantity() : 0;
        if (current < quantity) {
            throw new InsufficientStockException("Không đủ tồn kho: còn " + current + ", cần " + quantity);
        }
        variant.setQuantity(current - quantity);
        productVariantRepository.save(variant);
    }

    @Transactional
    public void restoreStock(Long productId, Long sizeId, int quantity) {
        if (quantity <= 0) return;
        ProductVariant variant = productVariantRepository
                .findByProductIdAndSizeId(productId, sizeId)
                .orElse(null);
        if (variant == null) {
            log.warn("restoreStock: variant not found for productId={} sizeId={}", productId, sizeId);
            return;
        }
        int current = variant.getQuantity() != null ? variant.getQuantity() : 0;
        variant.setQuantity(current + quantity);
        productVariantRepository.save(variant);
    }
}
