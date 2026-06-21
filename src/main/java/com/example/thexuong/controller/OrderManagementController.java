package com.example.thexuong.controller;

import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderManagementController {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final OrderService orderService;

    @GetMapping
    public String showOrders(@RequestParam(required = false) Long editId, Model model) {
        // 1. Lấy danh sách đơn hàng, sắp xếp mới nhất lên đầu (giảm dần theo ID)
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        Order formOrder = new Order();
        boolean isEdit = false;

        // 2. Nếu đang chỉnh sửa (có editId trên URL)
        if (editId != null) {
            Optional<Order> editOrder = orderRepository.findById(editId);
            if (editOrder.isPresent()) {
                formOrder = editOrder.get();
                isEdit = true;
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("formOrder", formOrder);
        model.addAttribute("isEdit", isEdit);

        // 3. QUAN TRỌNG: Trả về đúng file trong thư mục admin
        // File phải nằm tại: src/main/resources/templates/admin/orders.html
        return "admin/orders";
    }

    @PostMapping("/save")
    public String saveOrder(@ModelAttribute("formOrder") Order formOrder,
                            RedirectAttributes redirectAttributes) {
        if (formOrder.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: Không xác định được đơn hàng.");
            return "redirect:/admin/orders";
        }

        Order existing = orderRepository.findById(formOrder.getId()).orElse(null);
        if (existing != null) {
            // Cập nhật thông tin (KHÔNG cho phép set status ở đây — dùng /status/{id})
            existing.setFullName(formOrder.getFullName());
            existing.setPhoneNumber(formOrder.getPhoneNumber());
            existing.setAddress(formOrder.getAddress());
            existing.setPaymentMethod(formOrder.getPaymentMethod());

            orderRepository.save(existing);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng trong CSDL.");
        }

        return "redirect:/admin/orders";
    }

    /**
     * Task 0.14: Admin cập nhật status phải qua OrderService.adminUpdateStatus()
     * để áp dụng state machine (canTransitionTo).
     */
    @PostMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam("status") OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.adminUpdateStatus(id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Đã cập nhật trạng thái đơn hàng #" + id + " → " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if(orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đơn hàng #" + id);
        } else {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại.");
        }
        return "redirect:/admin/orders";
    }
}
