package com.example.thexuong.controller;

import java.security.Principal;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.service.CartService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private final CartService cartService;

    @GetMapping
    public String viewCart(Model model, Principal principal,
                           @CookieValue(value = "cart_token", required = false) String cartToken,
                           HttpServletResponse response) {
        Cart cart = null;
        if (principal != null) {
            // User is logged in, check if they have a guest cart to merge
            if (cartToken != null) {
                cartService.mergeGuestCartToUser(cartToken, principal.getName());
                // Clear the cookie after merging
                Cookie cookie = new Cookie("cart_token", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            cart = cartService.getCartByUser(principal.getName());
        } else {
            // User is guest
            if (cartToken != null) {
                cart = cartService.getCartByToken(cartToken);
            }
        }

        if (cart != null) {
            model.addAttribute("cart", cart);
            double total = cart.getItems().stream()
                    .mapToDouble(item -> {
                        var price = item.getProductVariant().getProduct().getPrice();
                        return (price != null ? price.doubleValue() : 0) * item.getQuantity();
                    })
                    .sum();
            model.addAttribute("totalPrice", total);
        } else {
            model.addAttribute("totalPrice", 0.0);
        }

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity,
            @CookieValue(value = "cart_token", required = false) String cartToken,
            HttpServletResponse response,
            Principal principal) {
        
        if (principal != null) {
            cartService.addToCart(principal.getName(), variantId, quantity);
        } else {
            if (cartToken == null) {
                cartToken = UUID.randomUUID().toString();
                Cookie cookie = new Cookie("cart_token", cartToken);
                cookie.setPath("/");
                cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                response.addCookie(cookie);
            }
            cartService.addGuestToCart(cartToken, variantId, quantity);
        }

        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return "redirect:/cart";
    }
}
