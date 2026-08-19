package com.example.thexuong.exception;

/**
 * Ném ra khi không tìm thấy Review (hoặc Product khi tạo review).
 */
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
