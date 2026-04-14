package com.example.thexuong.exception;

/**
 * Ném ra khi không tìm thấy User theo ID hoặc email.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Không tìm thấy người dùng với ID: " + id);
    }

    public UserNotFoundException(String email) {
        super("Không tìm thấy người dùng với email: " + email);
    }
}
