package com.example.thexuong.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)") // Tên sản phẩm
    private String name;

    @Column(columnDefinition = "NVARCHAR(100)") // Danh mục
    private String category;
    private BigDecimal price;
    @Column(columnDefinition = "NVARCHAR(100)") // Môn thể thao
    private String sport;
    @Column(columnDefinition = "NVARCHAR(100)") // Thương hiệu
    private String brand;
    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Tránh lặp vô hạn khi convert sang JSON
    @ToString.Exclude
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private Set<Review> reviews = new HashSet<>();
}
