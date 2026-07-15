package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
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

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String address;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "total_money")
    private BigDecimal totalMoney;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Builder.Default
    @Column(name = "shipping_fee")
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "points_used")
    private Integer pointsUsed = 0;

    @Column(name = "voucher_code", length = 20)
    private String voucherCode;

    @Column(name = "total_for_point_calc")
    private BigDecimal totalForPointCalc;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

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
    @Builder.Default
    private List<OrderDetail> orderDetails = new java.util.ArrayList<>();
}
