package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    @Autowired
    private final CartService cartService;

    @ModelAttribute("cartCount")
    public int populateCartCount(@org.springframework.web.bind.annotation.CookieValue(value = "cart_token", required = false) String cartToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

        try {
            Cart cart = null;
            if (isAuthenticated) {
                cart = cartService.getCartByUser(auth.getName());
            } else if (cartToken != null) {
                cart = cartService.getCartByToken(cartToken);
            }

            if (cart != null && cart.getItems() != null) {
                return cart.getItems().size();
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}
