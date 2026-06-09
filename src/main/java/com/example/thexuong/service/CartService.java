package com.example.thexuong.service;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.CartItem;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.CartItemRepository;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    @Autowired
    private final CartRepository cartRepository;
    @Autowired
    private final CartItemRepository cartItemRepository;
    @Autowired
    private final ProductVariantRepository productVariantRepository;
    @Autowired
    private final UserRepository userRepository;

    public Cart getCartByUser(String identifier) {
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                .orElseThrow(() -> new RuntimeException("User not found: " + identifier)));

        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).items(new ArrayList<>()).build();
                    return cartRepository.save(newCart);
                });
    }

    public Cart getCartByToken(String token) {
        return cartRepository.findByTokenWithItems(token)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().token(token).items(new ArrayList<>()).build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public void addToCart(String username, Long variantId, int quantity) {
        Cart cart = getCartByUser(username);
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(quantity)
                    .build();

            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void addGuestToCart(String token, Long variantId, int quantity) {
        Cart cart = getCartByToken(token);
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(quantity)
                    .build();

            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void mergeGuestCartToUser(String token, String username) {
        Cart guestCart = cartRepository.findByTokenWithItems(token).orElse(null);
        if (guestCart != null && !guestCart.getItems().isEmpty()) {
            Cart userCart = getCartByUser(username);
            for (CartItem guestItem : guestCart.getItems()) {
                Optional<CartItem> existing = userCart.getItems().stream()
                        .filter(i -> i.getProductVariant().getId().equals(guestItem.getProductVariant().getId()))
                        .findFirst();
                if (existing.isPresent()) {
                    existing.get().setQuantity(existing.get().getQuantity() + guestItem.getQuantity());
                    cartItemRepository.save(existing.get());
                } else {
                    guestItem.setCart(userCart);
                    userCart.getItems().add(guestItem);
                    cartItemRepository.save(guestItem);
                }
            }
            guestCart.getItems().clear();
            cartRepository.delete(guestCart);
        }
    }

    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Cart cart) {
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
