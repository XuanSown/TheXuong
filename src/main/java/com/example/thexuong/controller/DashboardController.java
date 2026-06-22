package com.example.thexuong.controller;

import com.example.thexuong.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class DashboardController {
    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ProductVariantRepository productVariantRepository;
    @Autowired
    private final ProductRepository productRepository;
    @Autowired private final UserRepository userRepository;

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        // 1. Top Bán Chạy (Lấy 5 cái đầu tiên)
        List<Object[]> topSelling = orderDetailRepository.findTopSellingProducts();
        model.addAttribute("topSelling", topSelling.stream().limit(5).toList());

        // 2. Top Bán Chậm (Lấy 5 cái đầu tiên)
        List<Object[]> slowSelling = orderDetailRepository.findSlowSellingProducts();
        model.addAttribute("slowSelling", slowSelling.stream().limit(5).toList());

        // 3. Doanh thu theo ngày (filter cả năm)
        List<Object[]> revenueByDay = orderRepository.getRevenueByDay(null, null);
        model.addAttribute("revenueByDay", revenueByDay);

        // 4. Tồn kho
        List<Object[]> inventory = productVariantRepository.getInventoryStatistics();
        model.addAttribute("inventory", inventory);

        // 5. Top 5 xem nhiều nhất
        List<Object[]> topViewed = productRepository.findTopViewedProducts(PageRequest.of(0, 5));
        model.addAttribute("topViewed", topViewed);

        // 6. Top 5 xem ít nhất
        List<Object[]> leastViewed = productRepository.findLeastViewedProducts(PageRequest.of(0, 5));
        model.addAttribute("leastViewed", leastViewed);

        // 1. Biểu đồ trạng thái đơn hàng (filter cả năm)
        List<Object[]> orderStatusStats = orderRepository.countOrdersByStatus(null, null);
        model.addAttribute("orderStatusStats", orderStatusStats);

        // 2. Biểu đồ người dùng (Có mua hàng vs Không mua hàng)
        long totalUsers = userRepository.count(); // Tổng số tài khoản
        long usersWithOrders = orderRepository.countUsersWithOrders(null, null); // Số người đã từng mua
        long usersWithoutOrders = totalUsers - usersWithOrders; // Số người chưa mua bao giờ

        model.addAttribute("usersWithOrders", usersWithOrders);
        model.addAttribute("usersWithoutOrders", usersWithoutOrders);

        return "admin/statistics";
    }

}
