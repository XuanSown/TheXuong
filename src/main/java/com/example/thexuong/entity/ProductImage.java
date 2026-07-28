package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@Builder
@Entity
@IdClass(ProductImageId.class)
@Table(name = "ProductImages")
public class ProductImage {

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Id
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    public ProductImage(Long productId, int sortOrder, String imageUrl) {
        this.productId = productId;
        this.sortOrder = sortOrder;
        this.imageUrl = imageUrl;
    }
}
