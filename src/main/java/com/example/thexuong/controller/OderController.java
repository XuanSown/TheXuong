package com.example.thexuong.controller;

import com.example.thexuong.entity.Order;
import com.example.thexuong.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OderController {

    private final OrderRepository orderRepository;

    @GetMapping
    public String listOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/detail/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        model.addAttribute("order", order);
        return "admin/order-detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long orderId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + orderId));

        order.setStatus(status);
        orderRepository.save(order);

        redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
        return "redirect:/admin/orders/detail/" + orderId;
    }
}