package com.example.thexuong.exception;

/**
 * 404 — FAQ không tồn tại.
 */
public class FaqNotFoundException extends RuntimeException {
    public FaqNotFoundException(Long id) {
        super("Không tìm thấy FAQ với ID: " + id);
    }
}
