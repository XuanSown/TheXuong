package com.example.thexuong.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin Product DTO with full details for CRUD operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductDto {
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    private String imageUrl;

    @Size(max = 100, message = "Sport tối đa 100 ký tự")
    private String sport;

    @Size(max = 100, message = "Brand tối đa 100 ký tự")
    private String brand;

    @Size(max = 100, message = "Category tối đa 100 ký tự")
    private String category;

    private Integer viewCount;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Map<String, Integer> sizeQuantities;

    /**
     * Danh sách URL ảnh sản phẩm (tối đa 5).
     * images[0] = ảnh chính, images[1..4] = ảnh phụ.
     * Not persisted to DB directly — used for API response.
     */
    private List<String> images;

    /**
     * Uploaded image files (multipart). Not persisted to DB.
     * When present, the controller will upload them to R2.
     */
    @JsonIgnore // Không serialize về frontend
    private List<MultipartFile> imageFiles;
}
