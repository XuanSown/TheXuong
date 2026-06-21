package com.example.thexuong.entity;

/**
 * Enum trạng thái đơn hàng chuẩn hoá cho TheXuong.
 * Thay thế cho String status cũ (PENDING/SHIPPING/SHIPPED/DELIVERED/COMPLETED/CANCELLED/APPROVED rời rạc).
 *
 * State machine:
 *   PENDING    → CONFIRMED | CANCELLED
 *   CONFIRMED  → SHIPPING   | CANCELLED | REFUNDED
 *   SHIPPING   → DELIVERED  | REFUNDED
 *   DELIVERED  → COMPLETED  | REFUNDED
 *   COMPLETED, CANCELLED, REFUNDED → terminal (không thể chuyển tiếp)
 *
 * Áp dụng trong OrderService.updateStatus() qua method canTransitionTo().
 */
public enum OrderStatus {
    PENDING,      // chờ thanh toán
    CONFIRMED,    // đã thanh toán, chờ shop xử lý
    SHIPPING,     // đang giao
    DELIVERED,    // đã giao, chờ user confirm
    COMPLETED,    // user đã nhận → hook cộng điểm loyalty
    CANCELLED,    // huỷ trước khi CONFIRMED
    REFUNDED;     // hoàn tiền sau thanh toán → hook trừ điểm loyalty

    /**
     * Kiểm tra có thể chuyển sang trạng thái next không.
     * Dùng trong OrderService để chặn transition sai.
     */
    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) return false;
        return switch (this) {
            case PENDING    -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == SHIPPING   || next == CANCELLED || next == REFUNDED;
            case SHIPPING   -> next == DELIVERED  || next == REFUNDED;
            case DELIVERED  -> next == COMPLETED  || next == REFUNDED;
            case COMPLETED, CANCELLED, REFUNDED -> false;
        };
    }
}
