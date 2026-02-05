package com.example.thexuong.controller;

import com.example.thexuong.repository.OrderDetailRepository;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        // 1. Top Bán Chạy (Lấy 5 cái đầu tiên)
        List<Object[]> topSelling = orderDetailRepository.findTopSellingProducts();
        model.addAttribute("topSelling", topSelling.stream().limit(5).toList());

        // 2. Top Bán Chậm (Lấy 5 cái đầu tiên)
        List<Object[]> slowSelling = orderDetailRepository.findSlowSellingProducts();
        model.addAttribute("slowSelling", slowSelling.stream().limit(5).toList());

        // 3. Doanh thu theo ngày
        List<Object[]> revenueByDay = orderRepository.getRevenueByDay();
        model.addAttribute("revenueByDay", revenueByDay);

        // 4. Tồn kho
        List<Object[]> inventory = productVariantRepository.getInventoryStatistics();
        model.addAttribute("inventory", inventory);

        return "admin/statistics";
    }

}
