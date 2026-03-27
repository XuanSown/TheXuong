package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CartItems")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Builder.Default
    private Integer quantity = 1;
}
