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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
private final CartRepository cartRepository;
private final CartItemRepository cartItemRepository;
private final ProductVariantRepository productVariantRepository;
private final UserRepository userRepository;
private final InventoryService inventoryService;

public Cart getCartByUser(String email) {
User user = userRepository.findByEmail(email)
.orElseThrow(() -> new RuntimeException("User not found"+ email));
return cartRepository.findByUserIdWithItems(user.getId())
.orElseGet(() -> {
Cart newCart = Cart.builder().user(user).items(new ArrayList<>()).build();
return cartRepository.save(newCart);
});
}

@Transactional
public void addToCart(String username, Long variantId, int quantity) {
Cart cart = getCartByUser(username);
ProductVariant variant = productVariantRepository.findById(variantId)
.orElseThrow(() -> new RuntimeException("Variant not found"));

// Phase 3: Kiểm tra tồn kho trước khi thêm vào giỏ
int currentStock = variant.getQuantity() != null ? variant.getQuantity() : 0;

// Tính tổng số lượng sẽ có trong giỏ (existing + new)
Optional<CartItem> existingItem = cart.getItems().stream()
.filter(item -> item.getProductVariant().getId().equals(variantId))
.findFirst();

int existingQty = existingItem.map(CartItem::getQuantity).orElse(0);
int totalQtyAfterAdd = existingQty + quantity;

if (totalQtyAfterAdd > currentStock) {
throw new RuntimeException(String.format(
"Không đủ hàng trong kho. Size %s: còn %d, giỏ đang có %d, cần thêm %d",
variant.getSize().getName(), currentStock, existingQty, quantity));
}

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
    public void removeCartItem(String email, Long cartItemId) {
        Cart cart = getCartByUser(email);
        CartItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    }

@Transactional
public void updateCartItemQuantity(String email, Long cartItemId, int quantity) {
    Cart cart = getCartByUser(email);
    CartItem item = cart.getItems().stream()
        .filter(i -> i.getId().equals(cartItemId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Cart item not found"));

    // Phase 3: Kiểm tra tồn kho khi cập nhật số lượng
    ProductVariant variant = item.getProductVariant();
    int currentStock = variant.getQuantity() != null ? variant.getQuantity() : 0;
    if (quantity > currentStock) {
        throw new RuntimeException(String.format(
            "Không đủ hàng. Size %s: còn %d, yêu cầu %d",
            variant.getSize().getName(), currentStock, quantity));
    }

    item.setQuantity(quantity);
    cartItemRepository.save(item);
}

@Transactional
public void clearCart(Cart cart) {
cartItemRepository.deleteAll(cart.getItems());
cart.getItems().clear();
cartRepository.save(cart);
}
}
