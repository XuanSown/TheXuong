package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserAddresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "label", length = 50)
    private String label;

    @Column(name = "recipient_name", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "province_code", length = 5, nullable = false)
    private String provinceCode;

    @Column(name = "district_code", length = 5, nullable = false)
    private String districtCode;

    @Column(name = "ward_code", length = 5, nullable = false)
    private String wardCode;

    @Column(name = "street_detail", columnDefinition = "NVARCHAR(255)")
    private String streetDetail;

    @Column(name = "latitude", columnDefinition = "DECIMAL(10,7)")
    private Double latitude;
    @Column(name = "longitude", columnDefinition = "DECIMAL(10,7)")
    private Double longitude;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
