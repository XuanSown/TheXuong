package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private final CartService cartService;

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Cart cart = cartService.getCartByUser(principal.getName());
        model.addAttribute("cart", cart);

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getProductVariant().getProduct().getPrice().doubleValue()*item.getQuantity())
                .sum();
        model.addAttribute("totalPrice", total);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("variantId") Long variantId,
                            @RequestParam("quantity") int quantity,
                            Principal principal) {
        if (principal == null) return "redirect:/login";

        cartService.addToCart(principal.getName(), variantId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return "redirect:/cart";
    }
}
