package com.example.thexuong.service;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.CartItem;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.Size;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.CartItemRepository;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private CartService cartService;

    private User mockUser;
    private Cart mockCart;
    private ProductVariant mockVariant;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");

        mockCart = new Cart();
        mockCart.setId(1L);
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>());

        Size mockSize = new Size();
        mockSize.setName("XL");

        mockVariant = new ProductVariant();
        mockVariant.setId(100L);
        mockVariant.setSize(mockSize);
        mockVariant.setQuantity(10); // Stock = 10
    }

    @Test
    void getCartByUser_UserExistsAndHasCart() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));

        Cart result = cartService.getCartByUser("test@gmail.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getCartByUser_UserExistsButNoCart_CreatesNewCart() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        Cart result = cartService.getCartByUser("test@gmail.com");

        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getCartByUser_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.getCartByUser("test@gmail.com");
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void addToCart_NewItem_EnoughStock() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
        when(productVariantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));

        cartService.addToCart("test@gmail.com", 100L, 2);

        assertEquals(1, mockCart.getItems().size());
        assertEquals(2, mockCart.getItems().get(0).getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem_EnoughStock() {
        CartItem existingItem = new CartItem();
        existingItem.setId(10L);
        existingItem.setProductVariant(mockVariant);
        existingItem.setQuantity(3);
        mockCart.getItems().add(existingItem);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
        when(productVariantRepository.findById(100L)).thenReturn(Optional.of(mockVariant));

        cartService.addToCart("test@gmail.com", 100L, 2);

        assertEquals(1, mockCart.getItems().size());
        assertEquals(5, existingItem.getQuantity()); // 3 + 2
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void addToCart_NotEnoughStock_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
        when(productVariantRepository.findById(100L)).thenReturn(Optional.of(mockVariant)); // Stock = 10

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart("test@gmail.com", 100L, 11);
        });

        assertTrue(exception.getMessage().contains("Không đủ hàng trong kho"));
    }

    @Test
    void addToCart_VariantNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
        when(productVariantRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart("test@gmail.com", 100L, 2);
        });

        assertTrue(exception.getMessage().contains("Variant not found"));
    }

    @Test
    void removeCartItem_Success() {
        CartItem existingItem = new CartItem();
        existingItem.setId(10L);
        existingItem.setProductVariant(mockVariant);
        mockCart.getItems().add(existingItem);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));

        cartService.removeCartItem("test@gmail.com", 100L);

        assertTrue(mockCart.getItems().isEmpty());
        verify(cartItemRepository, times(1)).delete(existingItem);
    }

    @Test
    void removeCartItem_ItemNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
        // Giỏ hàng trống, không có item ID=100

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.removeCartItem("test@gmail.com", 100L);
        });

        assertTrue(exception.getMessage().contains("Cart item not found"));
    }

    @Test
    void updateCartItemQuantity_Success() {
        CartItem existingItem = new CartItem();
        existingItem.setId(10L);
        existingItem.setProductVariant(mockVariant);
        existingItem.setQuantity(3);
        mockCart.getItems().add(existingItem);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));

        cartService.updateCartItemQuantity("test@gmail.com", 100L, 8);

        assertEquals(8, existingItem.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void updateCartItemQuantity_NotEnoughStock_ThrowsException() {
        CartItem existingItem = new CartItem();
        existingItem.setId(10L);
        existingItem.setProductVariant(mockVariant);
        existingItem.setQuantity(3);
        mockCart.getItems().add(existingItem);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItemQuantity("test@gmail.com", 100L, 12);
        });

        assertTrue(exception.getMessage().contains("Không đủ hàng"));
    }

    @Test
    void updateCartItemQuantity_ItemNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
        // Giỏ hàng rỗng

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItemQuantity("test@gmail.com", 100L, 5);
        });

        assertTrue(exception.getMessage().contains("Cart item not found"));
    }

    @Test
    void clearCart_Success() {
        CartItem existingItem1 = new CartItem();
        CartItem existingItem2 = new CartItem();
        mockCart.setItems(new ArrayList<>(List.of(existingItem1, existingItem2)));

        cartService.clearCart(mockCart);

        assertTrue(mockCart.getItems().isEmpty());
        verify(cartItemRepository, times(1)).deleteAll(any());
        verify(cartRepository, times(1)).save(mockCart);
    }
}
