package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(50)")
    private String name; // VD: "USER", "ADMIN", "PRODUCT_MANAGER"

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description; // VD: "Khách hàng", "Quản trị viên"
}
