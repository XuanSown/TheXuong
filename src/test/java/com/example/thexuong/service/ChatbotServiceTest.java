package com.example.thexuong.service;

import com.example.thexuong.dto.*;
import com.example.thexuong.entity.*;
import com.example.thexuong.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private FaqRepository faqRepository;
    @Mock private ChatMemoryRepository chatMemoryRepository;
    @Mock private ChatLogRepository chatLogRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    // ==================== getAllProductsForChatbot ====================

    @Test
    void getAllProductsForChatbot_NoVariants_ReturnsKhongRo() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Giay 1");
        
        when(productRepository.findAll()).thenReturn(List.of(p));
        when(productVariantRepository.findByProductId(1L)).thenReturn(List.of());

        List<ChatbotProductDto> result = chatbotService.getAllProductsForChatbot();
        assertEquals(1, result.size());
        assertEquals("Không rõ", result.get(0).getStockStatus());
    }

    @Test
    void getAllProductsForChatbot_TotalStockZero_ReturnsHetHang() {
        Product p = new Product();
        p.setId(1L);
        
        ProductVariant v1 = new ProductVariant();
        v1.setQuantity(0);
        ProductVariant v2 = new ProductVariant();
        v2.setQuantity(0);
        
        when(productRepository.findAll()).thenReturn(List.of(p));
        when(productVariantRepository.findByProductId(1L)).thenReturn(List.of(v1, v2));

        List<ChatbotProductDto> result = chatbotService.getAllProductsForChatbot();
        assertEquals("Hết hàng", result.get(0).getStockStatus());
    }
    
    @Test
    void getAllProductsForChatbot_TotalStockFive_ReturnsSapHet() {
        Product p = new Product();
        p.setId(1L);
        
        ProductVariant v1 = new ProductVariant();
        v1.setQuantity(3);
        ProductVariant v2 = new ProductVariant();
        v2.setQuantity(2);
        
        p.setVariants(List.of(v1, v2));
        
        when(productRepository.findAll()).thenReturn(List.of(p));

        List<ChatbotProductDto> result = chatbotService.getAllProductsForChatbot();
        assertEquals("Sắp hết", result.get(0).getStockStatus());
        // verify variant repo is not called since p.getVariants() is not empty
        verify(productVariantRepository, never()).findByProductId(any());
    }

    @Test
    void getAllProductsForChatbot_TotalStockSix_ReturnsConHang() {
        Product p = new Product();
        p.setId(1L);
        
        Category cat = new Category();
        cat.setName("Giay");
        p.setCategory(cat);
        
        Sport sport = new Sport();
        sport.setName("Bong Da");
        p.setSport(sport);
        
        Brand brand = new Brand();
        brand.setName("Nike");
        p.setBrand(brand);
        
        p.setPrice(new BigDecimal("1000000"));
        
        ProductVariant v1 = new ProductVariant();
        v1.setQuantity(6);
        p.setVariants(List.of(v1));
        
        when(productRepository.findAll()).thenReturn(List.of(p));

        List<ChatbotProductDto> result = chatbotService.getAllProductsForChatbot();
        ChatbotProductDto dto = result.get(0);
        assertEquals("Còn hàng", dto.getStockStatus());
        assertEquals("Giay", dto.getCategory());
        assertEquals("Bong Da", dto.getSport());
        assertEquals("Nike", dto.getBrand());
        assertEquals(1000000.0, dto.getPrice());
    }

    @Test
    void getAllProductsForChatbot_NullFields_MapsToNullSafely() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Giay 2");
        p.setCategory(null);
        p.setSport(null);
        p.setBrand(null);
        p.setPrice(null);
        
        ProductVariant v1 = new ProductVariant();
        v1.setQuantity(6);
        p.setVariants(List.of(v1));
        
        when(productRepository.findAll()).thenReturn(List.of(p));

        List<ChatbotProductDto> result = chatbotService.getAllProductsForChatbot();
        ChatbotProductDto dto = result.get(0);
        assertEquals("Còn hàng", dto.getStockStatus());
        assertNull(dto.getCategory());
        assertNull(dto.getSport());
        assertNull(dto.getBrand());
        assertEquals(0.0, dto.getPrice());
    }

    // ==================== getAllFaqsForChatbot ====================

    @Test
    void getAllFaqsForChatbot_Success() {
        Faq faq = new Faq();
        faq.setId(1L);
        faq.setTopic("Giao hang");
        faq.setQuestionKeywords("bao lau, may ngay");
        faq.setAnswer("3 ngay");
        
        when(faqRepository.findAllByOrderByTopicAscIdAsc()).thenReturn(List.of(faq));
        
        List<ChatbotFaqDto> result = chatbotService.getAllFaqsForChatbot();
        assertEquals(1, result.size());
        assertEquals("Giao hang", result.get(0).getTopic());
        assertEquals("3 ngay", result.get(0).getAnswer());
    }

    @Test
    void getAllFaqsForChatbot_Empty_ReturnsEmpty() {
        when(faqRepository.findAllByOrderByTopicAscIdAsc()).thenReturn(List.of());
        List<ChatbotFaqDto> result = chatbotService.getAllFaqsForChatbot();
        assertTrue(result.isEmpty());
    }

    // ==================== Chat Memory ====================

    @Test
    void getChatMemory_NotFound_ReturnsEmptyArray() {
        when(chatMemoryRepository.findById("chat1")).thenReturn(Optional.empty());
        assertEquals("[]", chatbotService.getChatMemory("chat1"));
    }

    @Test
    void getChatMemory_Found_ReturnsHistoryJson() {
        ChatMemory cm = new ChatMemory();
        cm.setHistoryJson("[{}]");
        when(chatMemoryRepository.findById("chat1")).thenReturn(Optional.of(cm));
        assertEquals("[{}]", chatbotService.getChatMemory("chat1"));
    }

    @Test
    void saveChatMemory_NewMemory_Saves() {
        when(chatMemoryRepository.findById("chat1")).thenReturn(Optional.empty());
        chatbotService.saveChatMemory("chat1", "[{new}]");
        
        ArgumentCaptor<ChatMemory> captor = ArgumentCaptor.forClass(ChatMemory.class);
        verify(chatMemoryRepository).save(captor.capture());
        assertEquals("chat1", captor.getValue().getChatId());
        assertEquals("[{new}]", captor.getValue().getHistoryJson());
    }

    @Test
    void saveChatMemory_ExistingMemory_Updates() {
        ChatMemory cm = new ChatMemory();
        cm.setChatId("chat1");
        cm.setHistoryJson("[{old}]");
        
        when(chatMemoryRepository.findById("chat1")).thenReturn(Optional.of(cm));
        chatbotService.saveChatMemory("chat1", "[{new}]");
        
        verify(chatMemoryRepository).save(cm);
        assertEquals("[{new}]", cm.getHistoryJson());
    }

    // ==================== Order Tracking ====================

    @Test
    void trackOrder_NotFound_ReturnsNull() {
        when(orderRepository.findByIdAndPhoneNumberWithDetails(1L, "0123")).thenReturn(Optional.empty());
        assertNull(chatbotService.trackOrder(1L, "0123"));
    }

    @Test
    void trackOrder_Found_ReturnsDtoWithItems() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalMoney(new BigDecimal("500000"));
        order.setPaymentMethod("COD");
        order.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));
        
        OrderDetail detail = new OrderDetail();
        detail.setQuantity(2);
        detail.setProductName("Giay Test");
        detail.setSize("42");
        
        order.setOrderDetails(List.of(detail));
        
        when(orderRepository.findByIdAndPhoneNumberWithDetails(1L, "0123")).thenReturn(Optional.of(order));
        
        ChatbotOrderDto dto = chatbotService.trackOrder(1L, "0123");
        assertNotNull(dto);
        assertEquals(1L, dto.getOrderId());
        assertEquals("PENDING", dto.getStatus());
        assertEquals(new BigDecimal("500000"), dto.getTotalMoney());
        assertEquals("COD", dto.getPaymentMethod());
        assertEquals(1, dto.getItems().size());
        assertEquals("2x Giay Test (Size 42)", dto.getItems().get(0));
    }

    @Test
    void trackOrder_Found_NullStatusAndEmptyDetails() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(null);
        order.setTotalMoney(new BigDecimal("100000"));
        
        when(orderRepository.findByIdAndPhoneNumberWithDetails(1L, "0123")).thenReturn(Optional.of(order));
        
        ChatbotOrderDto dto = chatbotService.trackOrder(1L, "0123");
        assertNotNull(dto);
        assertEquals("UNKNOWN", dto.getStatus());
        assertTrue(dto.getItems().isEmpty());
    }

    // ==================== Chat Logs ====================

    @Test
    void logInteraction_SavesLog() {
        ChatLogRequest req = new ChatLogRequest();
        req.setChatId("chat1");
        req.setUserName("User");
        req.setIntent("TRACK_ORDER");
        req.setUserMessage("order 123");
        req.setBotReply("ok");
        
        chatbotService.logInteraction(req);
        
        ArgumentCaptor<ChatLog> captor = ArgumentCaptor.forClass(ChatLog.class);
        verify(chatLogRepository).save(captor.capture());
        
        ChatLog log = captor.getValue();
        assertEquals("chat1", log.getChatId());
        assertEquals("TRACK_ORDER", log.getIntent());
        assertEquals("ok", log.getBotReply());
    }
}
