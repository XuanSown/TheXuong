package com.example.thexuong.exception;

/**
 * Ném ra khi cố chuyển trạng thái đơn hàng sai state machine.
 * Xem OrderStatus.canTransitionTo() để biết các transition hợp lệ.
 */
public class IllegalOrderTransitionException extends RuntimeException {
    public IllegalOrderTransitionException(String message) {
        super(message);
    }
}
