package com.example.thexuong.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewDtoSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void serializesIsMineAsIsMine() throws Exception {
        ReviewDto dto = ReviewDto.builder()
                .id(1L)
                .rating(5)
                .comment("Ok")
                .createdAt(LocalDateTime.of(2026, 8, 20, 10, 0))
                .authorName("An")
                .verifiedBuyer(true)
                .mine(true)
                .canModerate(false)
                .build();

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"isMine\":true");
        assertThat(json).contains("\"canModerate\":false");
        assertThat(json).contains("\"verifiedBuyer\":true");
        assertThat(json).doesNotContain("\"mine\"");
    }
}
