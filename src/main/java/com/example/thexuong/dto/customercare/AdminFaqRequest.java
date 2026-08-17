package com.example.thexuong.dto.customercare;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request tạo/sửa FAQ từ Admin.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFaqRequest {

    @NotBlank(message = "Chủ đề không được để trống")
    private String topic;

    @NotBlank(message = "Từ khóa nhận diện không được để trống")
    private String questionKeywords;

    @NotBlank(message = "Câu trả lời không được để trống")
    private String answer;
}
