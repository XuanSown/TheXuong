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
    public int populateCartCount(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return 0;

        try {
            Cart cart = cartService.getCartByUser(auth.getName());
            if (cart != null && cart.getItems() != null){
                return  cart.getItems().size();
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}
