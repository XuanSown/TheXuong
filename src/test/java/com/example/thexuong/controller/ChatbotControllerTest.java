package com.example.thexuong.controller;

import com.example.thexuong.entity.Faq;
import com.example.thexuong.repository.FaqRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatbotControllerTest extends BaseIntegrationTest {

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        faqRepository.deleteAll();

        Faq faq = new Faq();
        faq.setTopic("Địa chỉ");
        faq.setQuestionKeywords("Shop ở đâu?");
        faq.setAnswer("Shop ở HN");
        faqRepository.save(faq);
    }

    @Test
    void testGetProducts_Public() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chatbot/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void testGetFaqs_Public() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chatbot/faqs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testSaveChatMemory_Public() throws Exception {
        Map<String, String> req = Map.of(
                "chatId", "12345",
                "historyJson", "[{\"msg\":\"hello\"}]"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/chatbot/memory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void testGetChatMemory_Public() throws Exception {
        Map<String, String> req = Map.of("chatId", "12345", "historyJson", "[]");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/chatbot/memory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chatbot/memory/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.history_json", is("[]")));
    }
}
