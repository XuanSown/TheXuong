package com.example.thexuong.controller;

import com.example.thexuong.dto.CartItemDto;
import com.example.thexuong.dto.ProductDto;
import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.CartItem;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for Cart (Vue frontend consumption).
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartRestController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    /**
     * GET /api/cart
     * Returns current user's cart with items
     */
    @GetMapping
    public ResponseEntity<?> getCart(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        Cart cart = cartService.getCartByUser(email);

        double total = cart.getItems().stream()
                .mapToDouble(item -> {
                    var price = item.getProductVariant().getProduct().getPrice();
                    return (price != null ? price.doubleValue() : 0) * item.getQuantity();
                })
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("id", cart.getId());
        response.put("items", cart.getItems().stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList()));
        response.put("total", total);
        response.put("itemCount", cart.getItems().size());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/cart/items
     * Body: { variantId, quantity }
     */
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            Authentication authentication,
            @Valid @RequestBody AddItemRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        cartService.addToCart(email, request.getVariantId(), request.getQuantity());

        // Return updated cart
        Cart cart = cartService.getCartByUser(email);
        return ResponseEntity.ok(Map.of(
                "message", "Thêm vào giỏ hàng thành công",
                "cart", toCartResponse(cart)
        ));
    }

    /**
     * PUT /api/cart/items/{id}
     * Body: { quantity }
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<?> updateItemQuantity(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuantityRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        cartService.updateCartItemQuantity(id, request.getQuantity());

        // Return updated cart
        String email = authentication.getName();
        Cart cart = cartService.getCartByUser(email);
        return ResponseEntity.ok(Map.of(
                "message", "Cập nhật số lượng thành công",
                "cart", toCartResponse(cart)
        ));
    }

    /**
     * DELETE /api/cart/items/{id}
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> removeItem(
            Authentication authentication,
            @PathVariable Long id) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        cartService.removeCartItem(id);

        // Return updated cart
        String email = authentication.getName();
        Cart cart = cartService.getCartByUser(email);
        return ResponseEntity.ok(Map.of(
                "message", "Xóa khỏi giỏ hàng thành công",
                "cart", toCartResponse(cart)
        ));
    }

    /**
     * DELETE /api/cart/clear
     * Remove all items from cart
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        String email = authentication.getName();
        Cart cart = cartService.getCartByUser(email);
        cart.getItems().clear();
        cartRepository.save(cart);

        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa toàn bộ giỏ hàng",
                "cart", toCartResponse(cart)
        ));
    }

    // ========== Helper Methods ==========

    private Map<String, Object> toCartResponse(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> {
                    var price = item.getProductVariant().getProduct().getPrice();
                    return (price != null ? price.doubleValue() : 0) * item.getQuantity();
                })
                .sum();

        return new HashMap<>() {{
            put("id", cart.getId());
            put("items", cart.getItems().stream()
                    .map(CartRestController.this::toCartItemDto)
                    .collect(Collectors.toList()));
            put("total", total);
            put("itemCount", cart.getItems().size());
        }};
    }

    private CartItemDto toCartItemDto(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        BigDecimal price = variant.getProduct().getPrice();
        BigDecimal subtotal = price != null ? price.multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO;

        ProductDto productDto = ProductDto.builder()
                .id(variant.getProduct().getId())
                .name(variant.getProduct().getName())
                .price(price != null ? price.doubleValue() : null)
                .image(variant.getProduct().getImageUrl())
                .build();

        return CartItemDto.builder()
                .id(item.getId())
                .product(productDto)
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }

    // ========== Request DTOs ==========
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class AddItemRequest {
        @NotNull(message = "Variant ID không được để trống")
        private Long variantId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        private Integer quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class UpdateQuantityRequest {
        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 0, message = "Số lượng không thể âm")
        private Integer quantity;
    }
}
