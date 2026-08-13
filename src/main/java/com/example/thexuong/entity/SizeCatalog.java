package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "Sizes")
public class SizeCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "size_type_id", nullable = false)
    private SizeType sizeType;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
