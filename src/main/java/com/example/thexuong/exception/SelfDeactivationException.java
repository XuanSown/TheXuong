package com.example.thexuong.exception;

/**
 * Ném ra khi Admin cố tắt (disable) chính tài khoản của mình.
 * Quy tắc bảo vệ: Không ai được khóa tài khoản đang đăng nhập.
 */
public class SelfDeactivationException extends RuntimeException {
    public SelfDeactivationException() {
        super("Không thể tự khóa tài khoản của chính mình.");
    }

    public SelfDeactivationException(String message) {
        super(message);
    }
}
