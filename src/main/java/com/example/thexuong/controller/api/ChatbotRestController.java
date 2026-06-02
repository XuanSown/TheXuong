package com.example.thexuong.controller.api;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderDetail;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.ProductVariant;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotRestController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private static final String CHATBOT_API_KEY = "TheXuongSecretKey2026";

    // Helper method to validate API Key
    private boolean isNotAuthorized(String apiKey) {
        return apiKey == null || !apiKey.equals(CHATBOT_API_KEY);
    }

    /**
     * GET /api/chatbot/products
     * Search products by keyword, brand, or sport.
     * Returns product details along with variants (sizes & stock).
     */
    @GetMapping("/products")
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "X-Chatbot-API-Key", required = false) String apiKey) {

        if (isNotAuthorized(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: Invalid or missing API Key."));
        }

        Pageable limit = PageRequest.of(0, 50);
        List<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.findByNameContaining(keyword.trim(), limit).getContent();
        } else {
            products = productRepository.findAll(limit).getContent();
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Product product : products) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("brand", product.getBrand());
            map.put("sport", product.getSport());
            map.put("category", product.getCategory());
            map.put("price", product.getPrice());
            map.put("description", product.getDescription());

            // Fetch variants with sizes and quantities
            Product fullProduct = productRepository.findByIdWithVariants(product.getId()).orElse(product);
            List<Map<String, Object>> variantList = new ArrayList<>();
            if (fullProduct.getVariants() != null) {
                for (ProductVariant pv : fullProduct.getVariants()) {
                    Map<String, Object> vMap = new HashMap<>();
                    vMap.put("size", pv.getSize() != null ? pv.getSize().getName() : "FreeSize");
                    vMap.put("quantity", pv.getQuantity());
                    vMap.put("sku", pv.getSku());
                    variantList.add(vMap);
                }
            }
            map.put("variants", variantList);
            responseList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.ok("Tìm kiếm sản phẩm thành công.", responseList));
    }

    /**
     * GET /api/chatbot/orders/{id}
     * Get order details by ID.
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Chatbot-API-Key", required = false) String apiKey) {

        if (isNotAuthorized(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: Invalid or missing API Key."));
        }

        Order order = orderRepository.findByIdWithDetails(id).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy đơn hàng với mã ID: " + id));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("customerName", order.getFullName());
        map.put("phoneNumber", order.getPhoneNumber());
        map.put("address", order.getAddress());
        map.put("totalMoney", order.getTotalMoney());
        map.put("status", order.getStatus());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");

        List<Map<String, Object>> itemDetails = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail od : order.getOrderDetails()) {
                Map<String, Object> iMap = new HashMap<>();
                iMap.put("productName", od.getProductName());
                iMap.put("price", od.getPrice());
                iMap.put("quantity", od.getQuantity());
                iMap.put("totalPrice", od.getTotalPrice());
                itemDetails.add(iMap);
            }
        }
        map.put("items", itemDetails);

        return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết đơn hàng thành công.", map));
    }

    /**
     * GET /api/chatbot/orders/lookup
     * Lookup orders by phone number.
     */
    @GetMapping("/orders/lookup")
    public ResponseEntity<?> lookupOrdersByPhone(
            @RequestParam String phone,
            @RequestHeader(value = "X-Chatbot-API-Key", required = false) String apiKey) {

        if (isNotAuthorized(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: Invalid or missing API Key."));
        }

        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Số điện thoại không được để trống."));
        }

        // We can query all orders, then filter by phone number
        List<Order> allOrders = orderRepository.findAll();
        List<Order> matchingOrders = allOrders.stream()
                .filter(o -> o.getPhoneNumber() != null && o.getPhoneNumber().trim().replace(" ", "").equals(phone.trim().replace(" ", "")))
                .collect(Collectors.toList());

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Order order : matchingOrders) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("customerName", order.getFullName());
            map.put("phoneNumber", order.getPhoneNumber());
            map.put("totalMoney", order.getTotalMoney());
            map.put("status", order.getStatus());
            map.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");
            responseList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.ok("Tra cứu đơn hàng thành công.", responseList));
    }
}
