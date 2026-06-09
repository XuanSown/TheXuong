package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.CartService;
import com.example.thexuong.service.OrderService;
import com.example.thexuong.service.VNPayService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

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
    public String checkoutPage(Model model, Principal principal,
                               @CookieValue(value = "cart_token", required = false) String cartToken,
                               HttpServletResponse response) {
        if (principal == null) return "redirect:/login";

        // Merge guest cart if exists
        if (cartToken != null) {
            cartService.mergeGuestCartToUser(cartToken, principal.getName());
            Cookie cookie = new Cookie("cart_token", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

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
                             HttpServletRequest request,
                             Principal principal) {
        if (principal == null) return "redirect:/login";

        Order savedOrder = orderService.placeOrder(principal.getName(), fullName, phoneNumber, address);

        if("VNPAY".equals(paymentMethod)) {
            int totalAmount = savedOrder.getTotalMoney().intValue();
            String orderInfo = "Thanh toan don hang ma so " + savedOrder.getId();
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

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String orderInfo = request.getParameter("vnp_OrderInfo");

        if("00".equals(vnp_ResponseCode)) {
            try {
                if (orderInfo != null && orderInfo.contains("Thanh toan don hang ma so ")) {
                    String orderIdStr = orderInfo.replace("Thanh toan don hang ma so ", "").trim();
                    Long orderId = Long.parseLong(orderIdStr);

                    Order order = orderRepository.findById(orderId).orElse(null);
                    if (order != null) {
                        order.setStatus("PENDING");
                        orderRepository.save(order);
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