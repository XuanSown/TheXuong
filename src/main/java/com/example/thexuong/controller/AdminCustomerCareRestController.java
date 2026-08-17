package com.example.thexuong.controller;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.customercare.AdminChatLogResponse;
import com.example.thexuong.dto.customercare.AdminChatMemoryResponse;
import com.example.thexuong.dto.customercare.AdminConversationDetailResponse;
import com.example.thexuong.dto.customercare.AdminFaqRequest;
import com.example.thexuong.dto.customercare.AdminFaqResponse;
import com.example.thexuong.dto.customercare.CustomerCareOverviewResponse;
import com.example.thexuong.dto.customercare.PageResponse;
import com.example.thexuong.service.AdminCustomerCareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST API — Customer Care Management.
 * Base path: /api/v1/admin/customer-care
 * Chỉ ADMIN/BOTH truy cập được (SecurityConfig + @PreAuthorize).
 */
@RestController
@RequestMapping("/api/v1/admin/customer-care")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class AdminCustomerCareRestController {

    private final AdminCustomerCareService adminCustomerCareService;

    /**
     * GET /api/v1/admin/customer-care/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<CustomerCareOverviewResponse>> getOverview() {
        return ResponseEntity.ok(
                ApiResponse.ok("Lấy dữ liệu tổng quan thành công.", adminCustomerCareService.getOverview())
        );
    }

    // ==================== FAQ ====================

    /**
     * GET /api/v1/admin/customer-care/faqs?keyword=&topic=&page=&size=
     */
    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<PageResponse<AdminFaqResponse>>> getFaqs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lấy danh sách FAQ thành công.",
                        adminCustomerCareService.getFaqs(keyword, topic, page, size))
        );
    }

    /**
     * POST /api/v1/admin/customer-care/faqs
     */
    @PostMapping("/faqs")
    public ResponseEntity<ApiResponse<AdminFaqResponse>> createFaq(@Valid @RequestBody AdminFaqRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Tạo FAQ thành công.", adminCustomerCareService.createFaq(request))
        );
    }

    /**
     * PUT /api/v1/admin/customer-care/faqs/{id}
     */
    @PutMapping("/faqs/{id}")
    public ResponseEntity<ApiResponse<AdminFaqResponse>> updateFaq(
            @PathVariable Long id,
            @Valid @RequestBody AdminFaqRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Cập nhật FAQ thành công.", adminCustomerCareService.updateFaq(id, request))
        );
    }

    /**
     * DELETE /api/v1/admin/customer-care/faqs/{id}
     */
    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@PathVariable Long id) {
        adminCustomerCareService.deleteFaq(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Xóa FAQ thành công.")
        );
    }

    // ==================== Conversations / Chat Memory ====================

    /**
     * GET /api/v1/admin/customer-care/conversations?keyword=&page=&size=
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<PageResponse<AdminChatMemoryResponse>>> getConversations(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lấy danh sách hội thoại thành công.",
                        adminCustomerCareService.getConversations(keyword, page, size))
        );
    }

    /**
     * GET /api/v1/admin/customer-care/conversations/{chatId}
     */
    @GetMapping("/conversations/{chatId}")
    public ResponseEntity<ApiResponse<AdminConversationDetailResponse>> getConversationDetail(
            @PathVariable String chatId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lấy chi tiết hội thoại thành công.",
                        adminCustomerCareService.getConversationDetail(chatId))
        );
    }

    /**
     * DELETE /api/v1/admin/customer-care/conversations/{chatId}
     * Reset memory — không xóa chat_logs.
     */
    @DeleteMapping("/conversations/{chatId}")
    public ResponseEntity<ApiResponse<Void>> resetMemory(@PathVariable String chatId) {
        adminCustomerCareService.resetMemory(chatId);
        return ResponseEntity.ok(
                ApiResponse.ok("Đã xóa bộ nhớ hội thoại.")
        );
    }

    // ==================== Chat Logs ====================

    /**
     * GET /api/v1/admin/customer-care/logs?keyword=&chatId=&intent=&from=&to=&page=&size=&sort=
     * Chỉ đọc — không cho phép sửa Chat Log.
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PageResponse<AdminChatLogResponse>>> getLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chatId,
            @RequestParam(required = false) String intent,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lấy danh sách chat logs thành công.",
                        adminCustomerCareService.getLogs(keyword, chatId, intent, from, to, page, size, sort))
        );
    }
}
