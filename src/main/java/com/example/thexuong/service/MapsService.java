package com.example.thexuong.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MapsService {
    @Value("${app.google.maps.api-key:}") private String apiKey;
    @Value("${app.google.maps.language:vi}") private String language;
    @Value("${app.google.maps.region:vn}") private String region;

    // ponytail: trả Map thô thay vì DTO, đủ dùng cho FE match code
    @SuppressWarnings("unchecked")
    public Map<String, Object> reverseGeocode(double lat, double lng) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("Google Maps API key chưa cấu hình");
        RestClient client = RestClient.builder().baseUrl("https://maps.googleapis.com/maps/api/geocode").build();
        Map<String,Object> resp = client.get().uri(uri -> uri.path("/json")
                .queryParam("latlng", lat + "," + lng)
                .queryParam("language", language).queryParam("region", region)
                .queryParam("key", apiKey).build()).retrieve().body(Map.class);
        List<Map<String,Object>> results = (List<Map<String,Object>>) resp.get("results");
        if (results == null || results.isEmpty()) return Map.of("formattedAddress", "", "addressComponents", List.of());
        Map<String,Object> best = results.get(0);
        return Map.of(
            "formattedAddress", best.getOrDefault("formatted_address", ""),
            "addressComponents", best.getOrDefault("address_components", List.of())
        );
    }
}
