package com.example.thexuong.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapsServiceTest {

    @InjectMocks
    private MapsService mapsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapsService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(mapsService, "language", "vi");
        ReflectionTestUtils.setField(mapsService, "region", "vn");
    }

    @Test
    void reverseGeocode_ApiKeyNull_ThrowsIllegalStateException() {
        ReflectionTestUtils.setField(mapsService, "apiKey", null);
        assertThrows(IllegalStateException.class, () -> mapsService.reverseGeocode(10.0, 10.0));
    }

    @Test
    void reverseGeocode_ApiKeyBlank_ThrowsIllegalStateException() {
        ReflectionTestUtils.setField(mapsService, "apiKey", "   ");
        assertThrows(IllegalStateException.class, () -> mapsService.reverseGeocode(10.0, 10.0));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void reverseGeocode_NoResults_ReturnsEmptyStrings() {
        try (MockedStatic<RestClient> mockedStatic = mockStatic(RestClient.class)) {
            RestClient.Builder builderMock = mock(RestClient.Builder.class);
            when(builderMock.baseUrl(anyString())).thenReturn(builderMock);
            RestClient clientMock = mock(RestClient.class);
            when(builderMock.build()).thenReturn(clientMock);
            mockedStatic.when(RestClient::builder).thenReturn(builderMock);

            RestClient.RequestHeadersUriSpec uriSpecMock = mock(RestClient.RequestHeadersUriSpec.class);
            RestClient.RequestHeadersSpec headerSpecMock = mock(RestClient.RequestHeadersSpec.class);
            RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);

            when(clientMock.get()).thenReturn(uriSpecMock);
            when(uriSpecMock.uri(any(Function.class))).thenReturn(headerSpecMock);
            when(headerSpecMock.retrieve()).thenReturn(responseSpecMock);

            Map<String, Object> mockResponse = new HashMap<>();
            // results is null or empty
            mockResponse.put("results", Collections.emptyList());
            when(responseSpecMock.body(Map.class)).thenReturn(mockResponse);

            Map<String, Object> res = mapsService.reverseGeocode(10.0, 10.0);

            assertEquals("", res.get("formattedAddress"));
            assertTrue(((List<?>) res.get("addressComponents")).isEmpty());
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void reverseGeocode_WithResults_ReturnsBestMatch() {
        try (MockedStatic<RestClient> mockedStatic = mockStatic(RestClient.class)) {
            RestClient.Builder builderMock = mock(RestClient.Builder.class);
            when(builderMock.baseUrl(anyString())).thenReturn(builderMock);
            RestClient clientMock = mock(RestClient.class);
            when(builderMock.build()).thenReturn(clientMock);
            mockedStatic.when(RestClient::builder).thenReturn(builderMock);

            RestClient.RequestHeadersUriSpec uriSpecMock = mock(RestClient.RequestHeadersUriSpec.class);
            RestClient.RequestHeadersSpec headerSpecMock = mock(RestClient.RequestHeadersSpec.class);
            RestClient.ResponseSpec responseSpecMock = mock(RestClient.ResponseSpec.class);

            when(clientMock.get()).thenReturn(uriSpecMock);
            when(uriSpecMock.uri(any(Function.class))).thenReturn(headerSpecMock);
            when(headerSpecMock.retrieve()).thenReturn(responseSpecMock);

            Map<String, Object> mockResponse = new HashMap<>();
            Map<String, Object> bestMatch = new HashMap<>();
            bestMatch.put("formatted_address", "123 Test St");
            bestMatch.put("address_components", List.of("Component 1", "Component 2"));
            mockResponse.put("results", List.of(bestMatch));
            
            when(responseSpecMock.body(Map.class)).thenReturn(mockResponse);

            Map<String, Object> res = mapsService.reverseGeocode(10.0, 10.0);

            assertEquals("123 Test St", res.get("formattedAddress"));
            assertEquals(2, ((List<?>) res.get("addressComponents")).size());
        }
    }
}
