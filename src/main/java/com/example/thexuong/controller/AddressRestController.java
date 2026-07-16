package com.example.thexuong.controller;

import com.example.thexuong.dto.address.AddressRequest;
import com.example.thexuong.dto.address.AddressResponse;
import com.example.thexuong.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressRestController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> list(Authentication auth) {
        return ResponseEntity.ok(addressService.listByUser(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(Authentication auth, @Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(addressService.create(auth.getName(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(addressService.update(auth.getName(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> delete(Authentication auth, @PathVariable Long id) {
        addressService.delete(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Xóa địa chỉ thành công"));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<Map<String,String>> setDefault(Authentication auth, @PathVariable Long id) {
        addressService.setDefault(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Đặt địa chỉ mặc định thành công"));
    }
}
