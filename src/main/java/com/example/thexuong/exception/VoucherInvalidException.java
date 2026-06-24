package com.example.thexuong.exception;

/**
 * Ném ra khi voucher không hợp lệ (hết hạn, đã dùng, sai điều kiện, không thuộc user).
 * Handler trong GlobalExceptionHandler trả HTTP 400.
 */
public class VoucherInvalidException extends RuntimeException {
    public VoucherInvalidException(String message) {
        super(message);
    }
}
