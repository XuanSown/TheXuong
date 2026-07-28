package com.example.thexuong.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageId implements Serializable {

    private Long productId;
    private int sortOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductImageId)) return false;
        ProductImageId that = (ProductImageId) o;
        return sortOrder == that.sortOrder && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, sortOrder);
    }
}
