package com.example.thexuong.controller;

import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin REST API for Dashboard Statistics.
 * Base path: /api/v1/admin/statistics
 */
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class AdminStatisticsRestController {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        LocalDateTime startDt = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDt = end != null ? end.atTime(23, 59, 59) : null;

        // 1. Top Bán Chạy (Top 5)
        List<Object[]> topSelling = orderDetailRepository.findTopSellingProducts().stream().limit(5).toList();

        // 2. Top Bán Chậm (Top 5)
        List<Object[]> slowSelling = orderDetailRepository.findSlowSellingProducts().stream().limit(5).toList();

        // 3. Doanh thu theo ngày (with date filter)
        List<Object[]> revenueByDay = orderRepository.getRevenueByDay(start, end);

        // 4. Tồn kho
        List<Object[]> inventory = productVariantRepository.getInventoryStatistics();

        // 5. Top 5 xem nhiều nhất
        List<Object[]> topViewed = productRepository.findTopViewedProducts(PageRequest.of(0, 5));

        // 6. Top 5 xem ít nhất
        List<Object[]> leastViewed = productRepository.findLeastViewedProducts(PageRequest.of(0, 5));

        // 7. Trạng thái đơn hàng (with date filter)
        List<Object[]> orderStatusStats = orderRepository.countOrdersByStatus(startDt, endDt);

        // 8. Thống kê người dùng (with date filter)
        long totalUsers = userRepository.count();
        long usersWithOrders = orderRepository.countUsersWithOrders(startDt, endDt);
        long usersWithoutOrders = totalUsers - usersWithOrders;

        // Build response
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("topSelling", topSelling);
        response.put("slowSelling", slowSelling);
        response.put("revenueByDay", revenueByDay);
        response.put("inventory", inventory);
        response.put("topViewed", topViewed);
        response.put("leastViewed", leastViewed);
        response.put("orderStatusStats", orderStatusStats);
        response.put("totalUsers", totalUsers);
        response.put("usersWithOrders", usersWithOrders);
        response.put("usersWithoutOrders", usersWithoutOrders);

        return ResponseEntity.ok(response);
    }
}
