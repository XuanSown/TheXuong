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
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotRestController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private static final String CHATBOT_API_KEY = "TheXuongSecretKey2026";

    // Helper to remove Vietnamese accents
    private String removeAccents(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    // Helper method to validate API Key
    private boolean isNotAuthorized(String apiKey) {
        return apiKey == null || !apiKey.equals(CHATBOT_API_KEY);
    }

    // Helper method to clean and extract meaningful search keywords from natural chat
    private String cleanKeyword(String message) {
        if (message == null || message.trim().isEmpty()) return "";
        String cleaned = message.toLowerCase().trim();
        // Remove conversational stop words
        String[] stopWords = {"tại sao", "trong", "của", "chúng tôi", "có", "không", "mà", "tôi", "hỏi", "muốn", "mua", "tìm", "xem", "bên", "shop", "cho", "mình", "cái", "chiếc", "đôi", "loại", "này", "kia", "nhé", "nha", "ạ", "thấy"};
        for (String word : stopWords) {
            cleaned = cleaned.replaceAll("(?U)\\b" + word + "(?U)\\b", "");
        }
        // Map common synonyms
        cleaned = cleaned.replaceAll("(?U)\\bnón(?U)\\b", "mũ");
        cleaned = cleaned.replaceAll("(?U)\\bgiầy(?U)\\b", "giày");
        cleaned = cleaned.replaceAll("(?U)\\bbanh(?U)\\b", "bóng");
        cleaned = cleaned.replaceAll("(?U)\\bvớ(?U)\\b", "tất");
        cleaned = cleaned.replaceAll("(?U)\\bnikee(?U)\\b", "nike");
        cleaned = cleaned.replaceAll("(?U)\\bnai(?U)\\b", "nike");
        cleaned = cleaned.replaceAll("(?U)\\badida(?U)\\b", "adidas");
        cleaned = cleaned.replaceAll("(?U)\\baddidas(?U)\\b", "adidas");
        
        return cleaned.replaceAll("\\s+", " ").trim();
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
        System.out.println("RECEIVED KEYWORD: [" + keyword + "]");

        String searchKey = cleanKeyword(keyword);
        List<Product> allProducts = productRepository.findAll();
        List<Product> products = new ArrayList<>();

        if (!searchKey.isEmpty()) {
            String[] words = searchKey.split("\\s+");
            Map<Product, Integer> scoredProducts = new HashMap<>();

            for (Product p : allProducts) {
                String pName = removeAccents(p.getName() != null ? p.getName().toLowerCase() : "");
                String pBrand = removeAccents(p.getBrand() != null ? p.getBrand().toLowerCase() : "");
                String pCategory = removeAccents(p.getCategory() != null ? p.getCategory().toLowerCase() : "");
                String pSport = removeAccents(p.getSport() != null ? p.getSport().toLowerCase() : "");

                int score = 0;
                for (String w : words) {
                    if (w.length() > 1) {
                        String wl = removeAccents(w.toLowerCase());
                        if (pName.contains(wl)) score += 2;
                        if (pBrand.contains(wl)) score += 3;
                        if (pCategory.contains(wl)) score += 1;
                        if (pSport.contains(wl)) score += 1;
                    }
                }
                if (score > 0) {
                    scoredProducts.put(p, score);
                }
            }

            // Sort by score descending and take top 10
            products = scoredProducts.entrySet().stream()
                    .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                    .limit(10)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } else {
            products = allProducts.stream().limit(10).collect(Collectors.toList());
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
