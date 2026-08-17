package com.example.thexuong.service;

import com.example.thexuong.dto.customercare.AdminChatLogResponse;
import com.example.thexuong.dto.customercare.AdminChatMemoryResponse;
import com.example.thexuong.dto.customercare.AdminChatMessage;
import com.example.thexuong.dto.customercare.AdminConversationDetailResponse;
import com.example.thexuong.dto.customercare.AdminFaqRequest;
import com.example.thexuong.dto.customercare.AdminFaqResponse;
import com.example.thexuong.dto.customercare.CustomerCareOverviewResponse;
import com.example.thexuong.dto.customercare.PageResponse;
import com.example.thexuong.entity.ChatLog;
import com.example.thexuong.entity.ChatMemory;
import com.example.thexuong.entity.Faq;
import com.example.thexuong.exception.FaqNotFoundException;
import com.example.thexuong.repository.ChatLogRepository;
import com.example.thexuong.repository.ChatMemoryRepository;
import com.example.thexuong.repository.FaqRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service quản trị Customer Care (FAQ / Chat Memory / Chat Logs).
 * Chỉ dành cho Admin (ADMIN/BOTH) — bảo vệ bởi SecurityConfig + @PreAuthorize.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCustomerCareService {

    private static final String MODULE = "CUSTOMER_CARE";

    private final FaqRepository faqRepository;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatLogRepository chatLogRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    // ==================== Overview ====================

    /**
     * Tổng quan nhanh hoạt động chatbot:
     * - totalFaqs: số FAQ trong knowledge base.
     * - totalConversations: số chat memory.
     * - todayMessages: số chat log trong ngày (từ đầu ngày theo giờ server).
     * - topIntent: intent xuất hiện nhiều nhất trên toàn bộ logs.
     */
    @Transactional(readOnly = true)
    public CustomerCareOverviewResponse getOverview() {
        long totalFaqs = faqRepository.count();
        long totalConversations = chatMemoryRepository.count();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayMessages = chatLogRepository.countByCreatedAtAfter(startOfDay);

        String topIntent = findTopIntent();

        return CustomerCareOverviewResponse.builder()
                .totalFaqs(totalFaqs)
                .totalConversations(totalConversations)
                .todayMessages(todayMessages)
                .topIntent(topIntent)
                .build();
    }

    private String findTopIntent() {
        List<Object[]> rows = chatLogRepository.findTopIntents(PageRequest.of(0, 1));
        if (rows.isEmpty() || rows.get(0)[0] == null) {
            return null;
        }
        return String.valueOf(rows.get(0)[0]);
    }

    // ==================== FAQ ====================

    /**
     * Danh sách FAQ có phân trang, tìm kiếm theo keyword (topic/keywords/answer)
     * và lọc theo topic. Sắp xếp mới nhất trước (updatedAt DESC, id DESC).
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminFaqResponse> getFaqs(String keyword, String topic, int page, int size) {
        Specification<Faq> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("topic")), kw),
                    cb.like(cb.lower(root.get("questionKeywords")), kw),
                    cb.like(cb.lower(root.get("answer")), kw)
            ));
        }

        if (topic != null && !topic.isBlank()) {
            String t = "%" + topic.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("topic")), t));
        }

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "updatedAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<Faq> faqPage = faqRepository.findAll(spec, pageable);
        return PageResponse.from(faqPage.map(AdminFaqResponse::fromEntity));
    }

    /**
     * Tạo FAQ mới. Dữ liệu nằm chung bảng faqs → public API /chatbot/faqs thấy ngay.
     */
    @Transactional
    public AdminFaqResponse createFaq(AdminFaqRequest request) {
        Faq faq = Faq.builder()
                .topic(request.getTopic().trim())
                .questionKeywords(request.getQuestionKeywords().trim())
                .answer(request.getAnswer().trim())
                .build();
        Faq saved = faqRepository.save(faq);

        auditLogService.logAction(
                MODULE, "CREATE", String.valueOf(saved.getId()),
                null, toJson(AdminFaqResponse.fromEntity(saved)),
                "Admin tạo FAQ: " + saved.getTopic()
        );
        return AdminFaqResponse.fromEntity(saved);
    }

    /**
     * Sửa FAQ. 404 nếu không tồn tại.
     */
    @Transactional
    public AdminFaqResponse updateFaq(Long id, AdminFaqRequest request) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new FaqNotFoundException(id));
        String oldJson = toJson(AdminFaqResponse.fromEntity(faq));

        faq.setTopic(request.getTopic().trim());
        faq.setQuestionKeywords(request.getQuestionKeywords().trim());
        faq.setAnswer(request.getAnswer().trim());
        Faq saved = faqRepository.save(faq);

        auditLogService.logAction(
                MODULE, "UPDATE", String.valueOf(id),
                oldJson, toJson(AdminFaqResponse.fromEntity(saved)),
                "Admin sửa FAQ: " + saved.getTopic()
        );
        return AdminFaqResponse.fromEntity(saved);
    }

    /**
     * Xóa FAQ (hard delete). 404 nếu không tồn tại.
     */
    @Transactional
    public void deleteFaq(Long id) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new FaqNotFoundException(id));
        String oldJson = toJson(AdminFaqResponse.fromEntity(faq));
        faqRepository.delete(faq);

        auditLogService.logAction(
                MODULE, "DELETE", String.valueOf(id),
                oldJson, null,
                "Admin xóa FAQ: " + faq.getTopic()
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    // ==================== Conversations / Chat Memory ====================

    /**
     * Danh sách chat_memory, sort updatedAt DESC, search theo chatId.
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminChatMemoryResponse> getConversations(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        Page<ChatMemory> memoryPage;
        if (keyword != null && !keyword.isBlank()) {
            memoryPage = chatMemoryRepository.findByChatIdContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            memoryPage = chatMemoryRepository.findAll(pageable);
        }
        return PageResponse.from(memoryPage.map(this::toMemorySummary));
    }

    private AdminChatMemoryResponse toMemorySummary(ChatMemory memory) {
        List<Turn> turns = parseHistory(memory.getHistoryJson());
        int count = turns != null ? turns.size() : 0;
        String lastMessage = null;
        if (turns != null && !turns.isEmpty()) {
            lastMessage = turns.get(turns.size() - 1).bot();
        }
        return AdminChatMemoryResponse.builder()
                .chatId(memory.getChatId())
                .updatedAt(memory.getUpdatedAt())
                .messageCount(count)
                .lastMessage(lastMessage)
                .build();
    }

    /**
     * Chi tiết conversation: parse + transform an toàn.
     * historyJson malformed → messages rỗng + parseError = true (không crash).
     * chatId không tồn tại → messages rỗng, parseError = false.
     */
    @Transactional(readOnly = true)
    public AdminConversationDetailResponse getConversationDetail(String chatId) {
        ChatMemory memory = chatMemoryRepository.findById(chatId).orElse(null);
        if (memory == null) {
            return AdminConversationDetailResponse.builder()
                    .chatId(chatId)
                    .updatedAt(null)
                    .messages(List.of())
                    .parseError(false)
                    .build();
        }

        List<Turn> turns = parseHistory(memory.getHistoryJson());
        List<AdminChatMessage> messages = new ArrayList<>();
        boolean parseError = false;

        if (turns == null) {
            parseError = true;
        } else {
            for (Turn turn : turns) {
                messages.add(AdminChatMessage.builder().role("user").content(turn.user()).build());
                messages.add(AdminChatMessage.builder().role("assistant").content(turn.bot()).build());
            }
        }

        return AdminConversationDetailResponse.builder()
                .chatId(memory.getChatId())
                .updatedAt(memory.getUpdatedAt())
                .messages(messages)
                .parseError(parseError)
                .build();
    }

    /**
     * Parse historyJson (mảng [{user, bot}]) an toàn.
     * Trả null nếu JSON malformed / không phải array. Bỏ qua item không hợp lệ.
     */
    private List<Turn> parseHistory(String historyJson) {
        if (historyJson == null || historyJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(historyJson);
            if (!node.isArray()) {
                return null;
            }
            List<Turn> turns = new ArrayList<>();
            for (JsonNode item : node) {
                if (item == null || !item.isObject()) {
                    continue;
                }
                String user = item.hasNonNull("user") ? item.get("user").asText() : "";
                String bot = item.hasNonNull("bot") ? item.get("bot").asText() : "";
                if (user.isBlank() && bot.isBlank()) {
                    continue;
                }
                turns.add(new Turn(user, bot));
            }
            return turns;
        } catch (Exception e) {
            log.warn("Cannot parse historyJson for admin view: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Reset memory: xóa row chat_memory, KHÔNG xóa chat_logs.
     * Idempotent — không lỗi nếu chatId không tồn tại.
     */
    @Transactional
    public void resetMemory(String chatId) {
        if (chatMemoryRepository.existsById(chatId)) {
            chatMemoryRepository.deleteById(chatId);
            auditLogService.logAction(
                    MODULE, "RESET_MEMORY", chatId,
                    null, null,
                    "Admin reset memory của chat: " + chatId
            );
        }
    }

    private record Turn(String user, String bot) {
    }

    // ==================== Chat Logs ====================

    /**
     * Danh sách chat logs (chỉ đọc).
     * Search keyword (userName/chatId/userMessage), filter intent + from/to (yyyy-MM-dd).
     * Default sort createdAt DESC, id DESC.
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminChatLogResponse> getLogs(
            String keyword, String chatId, String intent, String from, String to,
            int page, int size, String sort) {

        Specification<ChatLog> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("userName")), kw),
                    cb.like(cb.lower(root.get("chatId")), kw),
                    cb.like(cb.lower(root.get("userMessage")), kw)
            ));
        }

        if (chatId != null && !chatId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("chatId"), chatId.trim()));
        }

        if (intent != null && !intent.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("intent")), intent.trim().toLowerCase()));
        }

        LocalDateTime fromDt = parseDateParam(from, true);
        if (fromDt != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromDt));
        }

        LocalDateTime toDt = parseDateParam(to, false);
        if (toDt != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toDt));
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && sort.toLowerCase().startsWith("createdat,asc")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(direction, "createdAt").and(Sort.by(direction, "id"))
        );

        Page<ChatLog> logPage = chatLogRepository.findAll(spec, pageable);
        return PageResponse.from(logPage.map(AdminChatLogResponse::fromEntity));
    }

    /**
     * Parse tham số ngày yyyy-MM-dd → LocalDateTime.
     * from → đầu ngày, to → cuối ngày. Giá trị không hợp lệ → null (bỏ qua, không crash).
     */
    private LocalDateTime parseDateParam(String value, boolean startOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return startOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }
}
