package com.example.thexuong.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final WebSocketEventListener webSocketEventListener;

    /**
     * Trả về snapshot danh sách user đang online tại thời điểm gọi.
     * Endpoint này cho phép client mới vừa subscribe lấy ngay danh sách
     * online hiện tại mà không cần chờ JOIN broadcast tiếp theo.
     * Chỉ người dùng đã xác thực mới được phép gọi.
     */
    @GetMapping("/online-users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getOnlineUsers() {
        return ResponseEntity.ok(webSocketEventListener.getOnlineUsers());
    }
}
