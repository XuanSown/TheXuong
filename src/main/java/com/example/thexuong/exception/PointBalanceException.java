package com.example.thexuong.exception;

/**
 * Ném ra khi user cố spend/reverse nhiều điểm hơn số dư hiện tại.
 * Handler trong GlobalExceptionHandler sẽ trả HTTP 400 với message tiếng Việt.
 */
public class PointBalanceException extends RuntimeException {
    public PointBalanceException(String message) {
        super(message);
    }
}
