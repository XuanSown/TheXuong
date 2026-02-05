package com.example.thexuong.controller;

import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class StatsController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public String showStatistics(Model model) {
        // 1. Số liệu tổng quan
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.calculateTotalRevenue();

        // CHỈ GIỮ LẠI TỒN KHO (Bỏ đầu sản phẩm)
        Long totalStock = productRepository.getTotalStockQuantity();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        model.addAttribute("totalStock", totalStock != null ? totalStock : 0);

        // 2. Các danh sách Top sản phẩm (Giữ nguyên)
        List<Object[]> topProducts = productRepository.findTopSellingProducts();
        model.addAttribute("topProducts", topProducts);

        List<Object[]> leastProducts = productRepository.findTopSellingProducts();
        model.addAttribute("leastProducts", leastProducts);

        return "admin/stats";
    }
}