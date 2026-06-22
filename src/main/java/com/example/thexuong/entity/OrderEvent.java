package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit log cho mỗi status transition của Order.
 * Dùng để:
 * - Hiển thị timeline trên UI
 * - Debug khi đơn bị stuck
 * - Tính metric (vd: average time từ PENDING → CONFIRMED)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OrderEvents")
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 20)
    private String toStatus;

    @Column(name = "actor_id")
    private Long actorId;

    /** 'USER' / 'ADMIN' / 'SYSTEM' (cron auto-transition) / 'VNPAY' (callback) */
    @Column(name = "actor_type", length = 20)
    private String actorType;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
