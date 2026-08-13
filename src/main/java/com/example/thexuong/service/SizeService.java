package com.example.thexuong.service;

import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.SizeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SizeService {

    private final SizeRepository sizeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public Map<String, Integer> parseSizeQuantities(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            log.warn("parseSizeQuantities failed: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Transactional
    public List<ProductVariant> createVariants(Product product, Map<String, Integer> sizeQuantities) {
        List<ProductVariant> variants = new ArrayList<>();
        for (Map.Entry<String, Integer> e : sizeQuantities.entrySet()) {
            String sizeName = e.getKey();
            Integer qty = e.getValue() == null ? 0 : e.getValue();
            Size size = sizeRepository.findByName(sizeName).orElse(null);
            if (size == null) continue;
            ProductVariant v = ProductVariant.builder()
                    .product(product)
                    .size(size)
                    .quantity(qty)
                    .sku(product.getId() + "-" + sizeName)
                    .build();
            variants.add(productVariantRepository.save(v));
        }
        return variants;
    }

    @Transactional
    public void updateVariants(Long productId, Map<String, Integer> sizeQuantities) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham: " + productId));

        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(productId);
        Map<String, ProductVariant> existingMap = new HashMap<>();
        for (ProductVariant v : existingVariants) {
            if (v.getSize() != null) {
                existingMap.put(v.getSize().getName(), v);
            }
        }

        for (Map.Entry<String, Integer> e : sizeQuantities.entrySet()) {
            String sizeName = e.getKey();
            Integer qty = e.getValue() == null ? 0 : e.getValue();
            
            if (existingMap.containsKey(sizeName)) {
                ProductVariant v = existingMap.get(sizeName);
                v.setQuantity(qty);
                productVariantRepository.save(v);
                existingMap.remove(sizeName);
            } else {
                Size size = sizeRepository.findByName(sizeName).orElse(null);
                if (size != null) {
                    ProductVariant v = ProductVariant.builder()
                            .product(product)
                            .size(size)
                            .quantity(qty)
                            .sku(product.getId() + "-" + sizeName)
                            .build();
                    productVariantRepository.save(v);
                }
            }
        }
        
        // Cac variant khong duoc gui len (admin xoa tren UI) -> set ve 0 de tranh loi khoa ngoai (CartItems)
        for (ProductVariant v : existingMap.values()) {
            v.setQuantity(0);
            productVariantRepository.save(v);
        }
    }
}
