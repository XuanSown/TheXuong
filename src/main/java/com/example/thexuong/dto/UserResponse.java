package com.example.thexuong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Simplified User DTO for API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    @JsonProperty("phone")
    private String phoneNumber;
    private java.util.List<com.example.thexuong.dto.address.AddressResponse> addresses;
    private String role;
    private String provider;
    private Boolean active;
}
