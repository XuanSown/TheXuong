package com.example.thexuong.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    /**
     * Nhận tin nhắn từ client gửi tới /app/chat.sendMessage
     * và broadcast xuống /topic/public.
     * Tên sender được lấy từ Principal (Spring Security),
     * KHÔNG cho phép client tự xác nhận sender.
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        // Ghi đè sender bằng thông tin xác thực từ Spring Security
        chatMessage.setSender(principal.getName());
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        // onlineUsers không cần thiết ở đây, Event Listener sẽ quản lý
        chatMessage.setOnlineUsers(null);
        return chatMessage;
    }
}
