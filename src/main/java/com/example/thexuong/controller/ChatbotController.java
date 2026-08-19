package com.example.thexuong.controller;

import com.example.thexuong.dto.ChatbotFaqDto;
import com.example.thexuong.dto.ChatbotProductDto;
import com.example.thexuong.dto.ChatLogRequest;
import com.example.thexuong.dto.ChatMemoryRequest;
import com.example.thexuong.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Telegram Chatbot (n8n integration).
 * Base path: /api/v1/chatbot
 *
 * These endpoints are PUBLIC (no auth required) because the chatbot
 * interacts with users who may not have an account.
 *
 * /orders/track được bảo vệ thêm bằng shared secret X-Chatbot-Secret
 * giữa n8n và backend (CHATBOT_API_SECRET) để chống brute-force mã đơn.
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Value("${chatbot.api.secret:}")
    private String chatbotApiSecret;

    // ==================== Products ====================

    /**
     * GET /api/v1/chatbot/products
     * Returns all active products for chatbot context.
     * Public endpoint - no auth required.
     */
    @GetMapping("/products")
    public ResponseEntity<?> getProducts() {
        List<ChatbotProductDto> products = chatbotService.getAllProductsForChatbot();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", products
        ));
    }

    // ==================== FAQs ====================

    /**
     * GET /api/v1/chatbot/faqs
     * Returns all FAQs for chatbot context.
     * Public endpoint - no auth required.
     */
    @GetMapping("/faqs")
    public ResponseEntity<?> getFaqs() {
        List<ChatbotFaqDto> faqs = chatbotService.getAllFaqsForChatbot();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", faqs
        ));
    }

    // ==================== Order Tracking ====================

    /**
     * GET /api/v1/chatbot/orders/track
     * Returns order details for a specific order code and phone number.
     * Protected by shared secret X-Chatbot-Secret (env CHATBOT_API_SECRET).
     */
    @GetMapping("/orders/track")
    public ResponseEntity<?> trackOrder(@RequestParam String id, @RequestParam String phone,
                                        @RequestHeader(value = "X-Chatbot-Secret", required = false) String secret) {
        if (chatbotApiSecret == null || chatbotApiSecret.isBlank()
                || !chatbotApiSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        Long orderId;
        try {
            orderId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Mã đơn hàng không hợp lệ."
            ));
        }

        com.example.thexuong.dto.ChatbotOrderDto order = chatbotService.trackOrder(orderId, phone);
        if (order == null) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không tìm thấy đơn hàng với số điện thoại này."
            ));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", order
        ));
    }

    // ==================== Chat Memory ====================

    /**
     * GET /api/v1/chatbot/memory/{chatId}
     * Returns conversation history for a chat_id.
     * Public endpoint - no auth required.
     */
    @GetMapping("/memory/{chatId}")
    public ResponseEntity<?> getChatMemory(@PathVariable String chatId) {
        String history = chatbotService.getChatMemory(chatId);
        String state = chatbotService.getChatState(chatId);
        // history_json + state_json top-level (không wrap trong data) — n8n đọc memItems[0].json.* trực tiếp
        return ResponseEntity.ok(Map.of(
                "success", true,
                "chatId", chatId,
                "history_json", history,
                "state_json", state
        ));
    }

    /**
     * POST /api/v1/chatbot/memory
     * Saves or updates conversation history + state.
     * Body: { "chatId": "string", "historyJson": "string", "stateJson": "string" }
     * Public endpoint - no auth required.
     */
    @PostMapping("/memory")
    public ResponseEntity<?> saveChatMemory(@RequestBody ChatMemoryRequest request) {
        chatbotService.saveChatMemory(request.getChatId(), request.getHistoryJson(), request.getStateJson());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã lưu lịch sử hội thoại"
        ));
    }

    // ==================== Chat Logs ====================

    /**
     * POST /api/v1/chatbot/logs
     * Logs a chatbot interaction.
     * Body: { "chatId", "userName", "intent", "userMessage", "botReply" }
     * Public endpoint - no auth required.
     */
    @PostMapping("/logs")
    public ResponseEntity<?> logInteraction(@RequestBody ChatLogRequest request) {
        chatbotService.logInteraction(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã ghi log"
        ));
    }
}
