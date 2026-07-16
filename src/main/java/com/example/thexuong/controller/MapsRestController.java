package com.example.thexuong.controller;

import com.example.thexuong.service.MapsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
public class MapsRestController {
    private final MapsService mapsService;

    @GetMapping("/reverse-geocode")
    public ResponseEntity<Map<String,Object>> reverseGeocode(Authentication auth,
            @RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(mapsService.reverseGeocode(lat, lng));
    }
}
