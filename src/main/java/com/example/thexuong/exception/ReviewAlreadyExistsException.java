package com.example.thexuong.exception;

/**
 * 409 — User đã review sản phẩm này rồi (UNIQUE user_id + product_id).
 */
public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String message) {
        super(message);
    }
}
