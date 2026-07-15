package com.example.thexuong.service;

import com.example.thexuong.entity.OrderEvent;
import com.example.thexuong.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ghi log mỗi status transition của Order.
 * Gọi từ OrderService.updateStatus + confirmReceived + refundOrder + adminUpdateStatus.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {

    private final OrderEventRepository orderEventRepository;

    @Transactional
    public void recordTransition(Long orderId, String fromStatus, String toStatus,
                                 Long actorId, String actorType, String note) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .actorId(actorId)
                .actorType(actorType)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();
        orderEventRepository.save(event);
    }
}
