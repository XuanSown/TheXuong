package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "username",unique = true, columnDefinition = "NVARCHAR(255)")
    private String username;

    private String password;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)") // Tên người dùng tiếng Việt
    private String fullName;


    @Column(name = "provider_id")
    private String providerId;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    private String provider = "LOCAL"; //'local' hoặc 'google'
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)") // Địa chỉ có thể dài
    private String address;

    @Builder.Default
    private String role = "USER"; //'user' hoặc 'admin'
}
