package com.example.thexuong.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    // Thread-safe Set để lưu danh sách user đang online
    private final Set<String> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** Trả về snapshot danh sách user đang online tại thời điểm gọi */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers);
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // Lấy username từ Spring Security Principal — không thể bị giả mạo
        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            onlineUsers.add(username);
            log.info("✅ User Connected: {}", username);

            // Broadcast thông báo JOIN + danh sách user online
            ChatMessage joinMessage = new ChatMessage();
            joinMessage.setType(ChatMessage.MessageType.JOIN);
            joinMessage.setSender(username);
            joinMessage.setContent(username + " đã tham gia chat!");
            joinMessage.setOnlineUsers(new ArrayList<>(onlineUsers));

            messagingTemplate.convertAndSend("/topic/public", joinMessage);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            onlineUsers.remove(username);
            log.info("❌ User Disconnected: {}", username);

            // Broadcast thông báo LEAVE + danh sách user online đã cập nhật
            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setType(ChatMessage.MessageType.LEAVE);
            leaveMessage.setSender(username);
            leaveMessage.setContent(username + " đã rời khỏi chat.");
            leaveMessage.setOnlineUsers(new ArrayList<>(onlineUsers));

            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        }
    }
}
