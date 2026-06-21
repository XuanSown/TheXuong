package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.CartService;
import com.example.thexuong.service.OrderService;
import com.example.thexuong.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class OrderController {
    @Autowired
    private final OrderService orderService;
    @Autowired
    private final CartService cartService;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private VNPayService vnPayService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Cart cart = cartService.getCartByUser(principal.getName());
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // FIX LỖI: Tìm bằng Email trước, nếu không có mới tìm bằng Username
        String identifier = principal.getName();
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier).orElse(null));

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getProductVariant().getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();
        model.addAttribute("totalPrice", total);

        return "checkout";
    }

    @GetMapping("/orders")
    public String myOrders(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        // FIX LỖI TƯƠNG TỰ Ở TRANG DANH SÁCH ĐƠN HÀNG
        String identifier = principal.getName();
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier).orElse(null));

        if (user != null) {
            List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(user.getId());
            model.addAttribute("orders", orders);
        }
        return "my-orders";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("fullName") String fullName,
                             @RequestParam("phoneNumber") String phoneNumber,
                             @RequestParam("address") String address,
                             @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
                             @RequestParam(value = "voucherCode", required = false) String voucherCode,
                             HttpServletRequest request,
                             Principal principal) {
        if (principal == null) return "redirect:/login";

        Order savedOrder = orderService.placeOrder(principal.getName(), fullName, phoneNumber, address);

        if("VNPAY".equals(paymentMethod)) {
            int totalAmount = savedOrder.getTotalMoney().intValue();
            // Format orderInfo chứa cả mã voucher nếu có (regex parse ở vnpayReturn)
            // Format: "Thanh toan don hang ma so X voucher=TX-ABCDEF" hoặc "Thanh toan don hang ma so X"
            String orderInfo = voucherCode != null && !voucherCode.isBlank()
                    ? "Thanh toan don hang ma so " + savedOrder.getId() + " voucher=" + voucherCode.trim()
                    : "Thanh toan don hang ma so " + savedOrder.getId();
            String vnpayUrl = vnPayService.createOrder(totalAmount, orderInfo, request);
            return "redirect:" + vnpayUrl;
        }

        return "redirect:/orders";
    }

    @GetMapping("/order/{id}")
    public String viewOrderDetail(@PathVariable("id") Long id, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        try {
            Order order = orderService.getOrderByIdAndUser(id, principal.getName());
            model.addAttribute("order", order);
            return "my-order-detail";
        } catch (Exception e) {
            return "redirect:/orders";
        }
    }

    @PostMapping("/order/update")
    public String updateOrderInfo(@RequestParam("orderId") Long orderId,
                                  @RequestParam("phoneNumber") String phoneNumber,
                                  @RequestParam("address") String address,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            orderService.updateOrderInfo(orderId, phoneNumber, address, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/order/" + orderId;
    }

    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam("orderId") Long orderId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            orderService.cancelOrder(orderId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/order/" + orderId;
    }

    // ============================================================
    // Task 0.8: User xác nhận đã nhận hàng → DELIVERED → COMPLETED
    // ============================================================
    @PostMapping("/order/{id}/confirm-received")
    public String confirmReceived(@PathVariable("id") Long id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            orderService.confirmReceived(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cảm ơn anh/chị đã xác nhận nhận hàng! Đơn hàng đã hoàn tất.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/order/" + id;
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String orderInfo = request.getParameter("vnp_OrderInfo");

        // Task 0.7: Parse orderId + voucher code từ vnp_OrderInfo
        // Format: "Thanh toan don hang ma so X" hoặc "Thanh toan don hang ma so X voucher=TX-XXX"
        Pattern pattern = Pattern.compile("Thanh toan don hang ma so (\\d+)(?: voucher=(TX-[A-Z0-9]+))?");
        Matcher matcher = orderInfo != null ? pattern.matcher(orderInfo) : null;

        if ("00".equals(vnp_ResponseCode)) {
            try {
                if (matcher != null && matcher.find()) {
                    Long orderId = Long.parseLong(matcher.group(1));
                    String voucherCode = matcher.group(2); // có thể null

                    Order order = orderRepository.findById(orderId).orElse(null);
                    if (order != null) {
                        // SỬA BUG NGHIÊM TRỌNG: set CONFIRMED + paidAt thay vì PENDING
                        order.setStatus(OrderStatus.CONFIRMED);
                        order.setPaidAt(LocalDateTime.now());
                        orderRepository.save(order);
                        // TODO Batch 3: nếu voucherCode != null → voucherService.markAsUsed(code, orderId)
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán VNPAY thành công! Đơn hàng của bạn đang được xử lý.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán VNPAY thất bại hoặc do hủy giao dịch!");
        }
        return "redirect:/orders";
    }
}
