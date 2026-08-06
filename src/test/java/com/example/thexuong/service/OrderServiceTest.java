package com.example.thexuong.service;

import com.example.thexuong.entity.*;
import com.example.thexuong.exception.IllegalOrderTransitionException;
import com.example.thexuong.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartService cartService;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private PointService pointService;
    @Mock private VoucherService voucherService;
    @Mock private UserRepository userRepository;
    @Mock private PointTierService pointTierService;
    @Mock private PointTierRepository pointTierRepository;
    @Mock private OrderEventService orderEventService;
    @Mock private EmailService emailService;
    @Mock private UserPointsRepository userPointsRepository;
    @Mock private InventoryService inventoryService;
    @Mock private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private OrderService orderService;

    private User mockUser;
    private Cart mockCart;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");
        mockUser.setUsername("testuser");
        mockUser.setFullName("Test User");

        mockCart = new Cart();
        mockCart.setUser(mockUser);
        
        Product product = new Product();
        product.setId(10L);
        product.setPrice(new BigDecimal("100000"));
        product.setName("Product Name");

        Size size = new Size();
        size.setId(5L);
        size.setName("L");

        ProductVariant variant = new ProductVariant();
        variant.setId(100L);
        variant.setProduct(product);
        variant.setSize(size);

        CartItem item = new CartItem();
        item.setProductVariant(variant);
        item.setQuantity(2); // Subtotal = 200,000
        
        mockCart.setItems(new ArrayList<>(List.of(item)));

        mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setStatus(OrderStatus.PENDING);
        mockOrder.setOrderDetails(new ArrayList<>());
    }

    // ==========================================
    // 1. placeOrder Tests
    // ==========================================

    @Test
    void placeOrder_EmptyCart_ThrowsException() {
        mockCart.setItems(new ArrayList<>());
        when(cartService.getCartByUser("testuser")).thenReturn(mockCart);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.placeOrder("testuser", "Name", "090", "Addr", null, 0)
        );
        assertTrue(ex.getMessage().contains("Giỏ hàng trống"));
    }

    @Test
    void placeOrder_Success() {
        when(cartService.getCartByUser("testuser")).thenReturn(mockCart);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = orderService.placeOrder("testuser", "Name", "090", "Addr", null, 0);

        assertNotNull(result);
        assertEquals(new BigDecimal("200000"), result.getSubtotal());
        assertEquals(new BigDecimal("200000"), result.getTotalMoney());
        verify(orderDetailRepository, times(1)).save(any(OrderDetail.class));
        verify(inventoryService, times(1)).deductStock(10L, 5L, 2);
        verify(cartService, times(1)).clearCart(mockCart);
    }

    @Test
    void placeOrder_WithVoucher_Success() {
        when(cartService.getCartByUser("testuser")).thenReturn(mockCart);
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.of(mockUser));
        when(voucherService.validateAndGetDiscount(eq("VOUCHER50"), eq(1L), any(BigDecimal.class)))
            .thenReturn(new BigDecimal("50000"));
        
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.placeOrder("testuser", "Name", "090", "Addr", "VOUCHER50", 0);

        assertEquals(new BigDecimal("50000"), result.getDiscountAmount());
        assertEquals(new BigDecimal("150000"), result.getTotalMoney());
        assertEquals("VOUCHER50", result.getVoucherCode());
    }

    @Test
    void placeOrder_WithPoints_Success() {
        when(cartService.getCartByUser("testuser")).thenReturn(mockCart);
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.of(mockUser));
        when(pointService.getCurrentPoints(1L)).thenReturn(100000); // Has 100k points
        
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.placeOrder("testuser", "Name", "090", "Addr", null, 50000);

        assertEquals(50000, result.getPointsUsed());
        assertEquals(new BigDecimal("50000"), result.getDiscountAmount());
        assertEquals(new BigDecimal("150000"), result.getTotalMoney());
        verify(pointService, times(1)).spendPoints(eq(1L), eq(50000), anyString());
    }

    @Test
    void placeOrder_WithPoints_NotEnoughPoints_ThrowsException() {
        when(cartService.getCartByUser("testuser")).thenReturn(mockCart);
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.of(mockUser));
        when(pointService.getCurrentPoints(1L)).thenReturn(10000); // Has 10k points

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.placeOrder("testuser", "Name", "090", "Addr", null, 50000)
        );
        assertTrue(ex.getMessage().contains("không đủ 50000 điểm"));
    }

    @Test
    void placeOrder_WithTierDiscount_Success() {
        mockUser.setTierCode("VIP");
        when(cartService.getCartByUser("testuser")).thenReturn(mockCart);
        // Khi không truyền voucher hay điểm, code KHÔNG tự gọi userRepository.findByEmail
        // Nên tier discount sẽ không chạy nếu không truyền voucher/point.
        // Đây có thể là logic hiện tại của code. Ta truyền point=0 để kích hoạt việc lấy user.
        // Wait, the logic is: `if (voucher != null || pointsToUse > 0) -> get userId`.
        // So if neither is passed, tier discount won't trigger because userId=null.
        // To trigger it, we pass pointsToUse=0 but voucher="TEST" or just test it based on current logic.
        // Let's pass a dummy voucher to trigger the userId logic.
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(voucherService.validateAndGetDiscount(any(), any(), any())).thenReturn(BigDecimal.ZERO);

        PointTier tier = new PointTier();
        tier.setCode("VIP");
        tier.setAutoDiscountPercent(new BigDecimal("10")); // 10% discount
        when(pointTierRepository.findByCode("VIP")).thenReturn(Optional.of(tier));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.placeOrder("testuser", "Name", "090", "Addr", "DUMMY", 0);

        // Subtotal = 200000. 10% = 20000
        assertEquals(new BigDecimal("20000"), result.getDiscountAmount());
        assertEquals(new BigDecimal("180000"), result.getTotalMoney());
    }

    // ==========================================
    // 2. getOrderByIdAndUser Tests
    // ==========================================

    @Test
    void getOrderByIdAndUser_Success() {
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        Order result = orderService.getOrderByIdAndUser(1L, 1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderByIdAndUser_NotFound_ThrowsException() {
        when(orderRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.getOrderByIdAndUser(99L, 1L)
        );
        assertTrue(ex.getMessage().contains("không tồn tại"));
    }

    @Test
    void getOrderByIdAndUser_WrongUser_ThrowsException() {
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.getOrderByIdAndUser(1L, 2L)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("quyền truy cập"));
    }

    // ==========================================
    // 3. updateOrderInfo Tests
    // ==========================================

    @Test
    void updateOrderInfo_Pending_Success() {
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        orderService.updateOrderInfo(1L, "098", "New Addr", 1L);
        assertEquals("098", mockOrder.getPhoneNumber());
        assertEquals("New Addr", mockOrder.getAddress());
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    void updateOrderInfo_NotPending_ThrowsException() {
        mockOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.updateOrderInfo(1L, "098", "New Addr", 1L)
        );
        assertTrue(ex.getMessage().contains("không thể thay đổi thông tin"));
    }

    // ==========================================
    // 4. cancelOrder Tests
    // ==========================================

    @Test
    void cancelOrder_Pending_Success() {
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        orderService.cancelOrder(1L, 1L);
        assertEquals(OrderStatus.CANCEL_REQUESTED, mockOrder.getStatus());
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    void cancelOrder_NotPending_ThrowsException() {
        mockOrder.setStatus(OrderStatus.SHIPPING);
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.cancelOrder(1L, 1L)
        );
        assertTrue(ex.getMessage().contains("không thể yêu cầu hủy"));
    }

    // ==========================================
    // 5. confirmReceived Tests
    // ==========================================

    @Test
    void confirmReceived_Delivered_Success() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        mockOrder.setTotalForPointCalc(new BigDecimal("200000"));
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.confirmReceived(1L, 1L);
        assertEquals(OrderStatus.COMPLETED, mockOrder.getStatus());
        assertNotNull(mockOrder.getCompletedAt());
        verify(pointService, times(1)).earnPoints(eq(1L), eq(1L), any(), anyString());
    }

    @Test
    void confirmReceived_Pending_ThrowsException() {
        mockOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));

        IllegalOrderTransitionException ex = assertThrows(IllegalOrderTransitionException.class, () -> 
            orderService.confirmReceived(1L, 1L)
        );
        assertTrue(ex.getMessage().contains("Không thể xác nhận"));
    }

    @Test
    void confirmReceived_EarnPointsException_StillSucceeds() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        mockOrder.setTotalForPointCalc(new BigDecimal("200000"));
        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        // Giả lập lỗi khi earn points
        when(pointService.earnPoints(anyLong(), anyLong(), any(), anyString())).thenThrow(new RuntimeException("DB Error"));

        // Vẫn phải chạy thành công do có try/catch
        Order result = orderService.confirmReceived(1L, 1L);
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
    }

    // ==========================================
    // 6. refundOrder Tests
    // ==========================================

    @Test
    void refundOrder_Delivered_Success() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.refundOrder(1L, "admin");
        assertEquals(OrderStatus.REFUNDED, mockOrder.getStatus());
    }

    @Test
    void refundOrder_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.refundOrder(99L, "admin")
        );
        assertTrue(ex.getMessage().contains("Order not found"));
    }

    @Test
    void refundOrder_Pending_ThrowsException() {
        mockOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        IllegalOrderTransitionException ex = assertThrows(IllegalOrderTransitionException.class, () -> 
            orderService.refundOrder(1L, "admin")
        );
        assertTrue(ex.getMessage().contains("Không thể hoàn tiền"));
    }

    // ==========================================
    // 7. adminUpdateStatus Tests
    // ==========================================

    @Test
    void adminUpdateStatus_PendingToConfirmed_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.adminUpdateStatus(1L, OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, mockOrder.getStatus());
        assertNotNull(mockOrder.getPaidAt());
    }

    @Test
    void adminUpdateStatus_ConfirmedToShipping_Success() {
        mockOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.adminUpdateStatus(1L, OrderStatus.SHIPPING);
        assertEquals(OrderStatus.SHIPPING, mockOrder.getStatus());
        assertNotNull(mockOrder.getShippedAt());
    }

    @Test
    void adminUpdateStatus_ShippingToDelivered_Success() {
        mockOrder.setStatus(OrderStatus.SHIPPING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.adminUpdateStatus(1L, OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, mockOrder.getStatus());
        assertNotNull(mockOrder.getDeliveredAt());
    }

    @Test
    void adminUpdateStatus_PendingToCancelled_Success() {
        mockOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        orderService.adminUpdateStatus(1L, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());
        assertNotNull(mockOrder.getCancelledAt());
    }

    @Test
    void adminUpdateStatus_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            orderService.adminUpdateStatus(99L, OrderStatus.CONFIRMED)
        );
        assertTrue(ex.getMessage().contains("Order not found"));
    }

    @Test
    void adminUpdateStatus_InvalidTransition_ThrowsException() {
        mockOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        IllegalOrderTransitionException ex = assertThrows(IllegalOrderTransitionException.class, () -> 
            orderService.adminUpdateStatus(1L, OrderStatus.DELIVERED) // PENDING -> DELIVERED is illegal
        );
        assertTrue(ex.getMessage().contains("Transition không hợp lệ"));
    }
}
