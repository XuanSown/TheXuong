package com.example.thexuong.controller.api;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.CartItem;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartRestController {

    private final CartService cartService;

    // DTOs
    public record CartItemResponse(
            Long id,
            Long productId,
            String productName,
            String productImage,
            Long variantId,
            String size,
            Integer quantity,
            Double price,
            Double subtotal
    ) {}

    public record CartResponse(
            CartItemResponse[] items,
            int totalItems,
            double totalPrice
    ) {}

    public record AddCartItemRequest(
            Long variantId,
            Integer quantity
    ) {}

    public record UpdateCartItemRequest(
            Integer quantity
    ) {}

    // GET /api/v1/cart - Get current user's cart
    @GetMapping
    public CartResponse getCart(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        Cart cart = cartService.getCartByUser(principal.getName());

        CartItemResponse[] items = cart.getItems().stream()
                .map(this::toResponse)
                .toArray(CartItemResponse[]::new);

        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> {
                    ProductVariant variant = item.getProductVariant();
                    double price = variant.getProduct().getPrice() != null ?
                            variant.getProduct().getPrice().doubleValue() : 0;
                    return price * item.getQuantity();
                })
                .sum();

        return new CartResponse(items, totalItems, totalPrice);
    }

    // POST /api/v1/cart/items - Add item
    @PostMapping("/items")
    public CartResponse addCartItem(@RequestBody AddCartItemRequest request, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        cartService.addToCart(principal.getName(), request.variantId(), request.quantity());
        return getCart(principal);
    }

    // PUT /api/v1/cart/items/{id} - Update quantity
    @PutMapping("/items/{id}")
    public CartResponse updateCartItem(@PathVariable Long id, @RequestBody UpdateCartItemRequest request, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        cartService.updateCartItemQuantity(id, request.quantity());
        return getCart(principal);
    }

    // DELETE /api/v1/cart/items/{id} - Remove item
    @DeleteMapping("/items/{id}")
    public CartResponse removeCartItem(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        cartService.removeCartItem(id);
        return getCart(principal);
    }

    private CartItemResponse toResponse(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        double price = variant.getProduct().getPrice() != null ?
                variant.getProduct().getPrice().doubleValue() : 0;

        return new CartItemResponse(
                item.getId(),
                variant.getProduct().getId(),
                variant.getProduct().getName(),
                variant.getProduct().getImageUrl(),
                variant.getId(),
                variant.getSize().getName(),
                item.getQuantity(),
                price,
                price * item.getQuantity()
        );
    }
}
