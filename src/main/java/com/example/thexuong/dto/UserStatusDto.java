package com.example.thexuong.dto;

import com.example.thexuong.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi sau thao tác toggle active.
 * Chỉ trả về dữ liệu tối thiểu cần thiết cho Frontend cập nhật UI.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {

private Long id;
private String email;
private String fullName;
private String role;
private Boolean active;
private String provider;
private String tierCode;

/** Chuyển đổi từ Entity sang DTO — tránh expose trực tiếp Entity ra API. */
public static UserStatusDto from(User user) {
return UserStatusDto.builder()
.id(user.getId())
.email(user.getEmail())
.fullName(user.getFullName())
.role(user.getRole())
.active(user.getActive())
.provider(user.getProvider())
.tierCode(user.getTierCode())
.build();
}
}
