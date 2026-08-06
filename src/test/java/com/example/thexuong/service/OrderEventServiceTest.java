package com.example.thexuong.service;

import com.example.thexuong.entity.OrderEvent;
import com.example.thexuong.repository.OrderEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventServiceTest {

    @Mock
    private OrderEventRepository orderEventRepository;

    @InjectMocks
    private OrderEventService orderEventService;

    @Test
    void recordTransition_AllFieldsProvided_Success() {
        orderEventService.recordTransition(1L, "PENDING", "CONFIRMED", 100L, "ADMIN", "Duyệt đơn");

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(orderEventRepository, times(1)).save(captor.capture());

        OrderEvent savedEvent = captor.getValue();
        assertEquals(1L, savedEvent.getOrderId());
        assertEquals("PENDING", savedEvent.getFromStatus());
        assertEquals("CONFIRMED", savedEvent.getToStatus());
        assertEquals(100L, savedEvent.getActorId());
        assertEquals("ADMIN", savedEvent.getActorType());
        assertEquals("Duyệt đơn", savedEvent.getNote());
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    void recordTransition_NullFields_Success() {
        orderEventService.recordTransition(2L, null, "PENDING", null, "SYSTEM", null);

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(orderEventRepository, times(1)).save(captor.capture());

        OrderEvent savedEvent = captor.getValue();
        assertEquals(2L, savedEvent.getOrderId());
        assertEquals(null, savedEvent.getFromStatus());
        assertEquals("PENDING", savedEvent.getToStatus());
        assertEquals(null, savedEvent.getActorId());
        assertEquals("SYSTEM", savedEvent.getActorType());
        assertEquals(null, savedEvent.getNote());
        assertNotNull(savedEvent.getCreatedAt());
    }
}
