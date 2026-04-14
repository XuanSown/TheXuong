package com.example.thexuong.exception;

/**
 * Ném ra khi cố xóa RoleGroup (Chức danh) đang có User thuộc về.
 * Buộc Admin phải chuyển User sang nhóm khác trước khi xóa.
 */
public class RoleGroupInUseException extends RuntimeException {
    public RoleGroupInUseException(String groupName) {
        super("Không thể xóa chức danh '" + groupName + "' vì vẫn còn người dùng thuộc nhóm này. Hãy chuyển họ sang nhóm khác trước.");
    }
}
