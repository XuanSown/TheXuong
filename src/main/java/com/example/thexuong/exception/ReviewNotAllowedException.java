package com.example.thexuong.exception;

/**
 * 403 — User không đủ điều kiện: chưa mua sản phẩm, không phải chủ review, không phải admin.
 */
public class ReviewNotAllowedException extends RuntimeException {
    public ReviewNotAllowedException(String message) {
        super(message);
    }
}
