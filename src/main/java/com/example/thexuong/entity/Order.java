package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)") // Tên người nhận
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)") // Đã có sẵn trong file của bạn, giữ nguyên
    private String address;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "total_money")
    private BigDecimal totalMoney;

    // ============================================================
    // Batch 3: Voucher + snapshot fields
    // ============================================================

    /** Subtotal = tổng tiền hàng (chưa trừ voucher, chưa cộng ship). */
    @Column(name = "subtotal")
    private BigDecimal subtotal;

    /** Phí ship (mặc định 0). TODO Batch 4: VIP free ship. */
    @Builder.Default
    @Column(name = "shipping_fee")
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /** Số tiền giảm từ voucher. */
    @Builder.Default
    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Số điểm user dùng để giảm giá. */
    @Builder.Default
    @Column(name = "points_used")
    private Integer pointsUsed = 0;

    /** Mã voucher user đã dùng (TX-XXXXXX). */
    @Column(name = "voucher_code", length = 20)
    private String voucherCode;

    /** Snapshot tổng tiền dùng để tính điểm (bằng subtotal). */
    @Column(name = "total_for_point_calc")
    private BigDecimal totalForPointCalc;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    // 7 trạng thái chuẩn (xem OrderStatus.java):
    // PENDING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED
    //                                     ↓
    //                                 REFUNDED (hoàn tiền)
    // CANCELLED (huỷ trước CONFIRMED)

    // Timestamp cho từng state transition (Batch 0 Task 0.4 + 0.5)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;
}
