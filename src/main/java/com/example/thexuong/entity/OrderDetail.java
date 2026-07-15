package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OrderDetails")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", columnDefinition = "NVARCHAR(255)")
    private String productName;

    private String size;

    private BigDecimal price;

    private Integer quantity;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "product_variant_id")
    private Long productVariantId;
}
