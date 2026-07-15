package com.example.thexuong.exception;

/**
 * Ném ra khi không tìm thấy User theo ID hoặc email.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Không tìm thấy người dùng.");
    }

    public UserNotFoundException(String email) {
        super("Không tìm thấy người dùng.");
    }
}
