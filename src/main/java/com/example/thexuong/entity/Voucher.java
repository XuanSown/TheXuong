package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Catalog voucher (admin quản lý).
 * Mỗi row = 1 mệnh giá có thể đổi.
 *
 * Phân biệt với UserVoucher:
 * - Vouchers.code = "TX-CAT-100K" (mã danh mục, hiển thị trong admin)
 * - UserVouchers.code = "TX-ABCDEF" (mã DUY NHẤT user nhận, dùng khi checkout)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã catalog (UNIQUE). VD: TX-CAT-10K, TX-CAT-100K. */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    /** Mệnh giá giảm (VND). */
    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    /** Số điểm cần để đổi. */
    @Column(name = "required_points", nullable = false)
    private Integer requiredPoints;

    /** Đơn tối thiểu (VND). NULL = không yêu cầu. */
    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    /** JSON array category IDs áp dụng. NULL = tất cả category. */
    @Column(name = "applicable_category_ids", columnDefinition = "NVARCHAR(MAX)")
    private String applicableCategoryIds;

    /** JSON array product IDs áp dụng. NULL = tất cả sản phẩm. */
    @Column(name = "applicable_product_ids", columnDefinition = "NVARCHAR(MAX)")
    private String applicableProductIds;

    /** 1 = chỉ user VIP mới đổi được (Phương án C tier benefits). */
    @Builder.Default
    @Column(name = "vip_only", nullable = false)
    private Boolean vipOnly = false;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE,   // hoạt động, user có thể đổi
        LOCKED,   // admin tạm khoá (không đổi được nhưng không xoá)
        EXPIRED   // hết hạn catalog (admin chỉnh tay)
    }
}
