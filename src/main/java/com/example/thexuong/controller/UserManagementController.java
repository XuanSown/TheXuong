package com.example.thexuong.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.User;
import com.example.thexuong.exception.SelfDeactivationException;
import com.example.thexuong.repository.CartItemRepository;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;

    // Danh sách role hợp lệ — dùng cho dropdown trong form Admin.
    private static final List<String> AVAILABLE_ROLES = List.of("USER", "ADMIN", "BOTH");

    // ==================== HIỂN THỊ DANH SÁCH ====================
    @GetMapping
    public String showUsers(@RequestParam(required = false) Long editId, Model model) {
        List<User> users = userRepository.findAllByOrderByIdAsc();

        User formUser = new User();
        boolean isEdit = false;

        if (editId != null) {
            Optional<User> editUser = userRepository.findById(editId);
            if (editUser.isPresent()) {
                formUser = editUser.get();
                formUser.setPassword(""); // Không đưa Hash ra ngoài giao diện, tránh đụng độ Double-Encode
                isEdit = true;
            }
        }

        Long currentUserId = getCurrentUserId();

        model.addAttribute("users", users);
        model.addAttribute("availableRoles", AVAILABLE_ROLES);
        model.addAttribute("formUser", formUser);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("currentUserId", currentUserId);

        return "admin/users";
    }

    // ==================== LƯU USER (TẠO MỚI / CẬP NHẬT) ====================
    @PostMapping("/save")
    public String saveUser(@ModelAttribute("formUser") User formUser,
            @RequestParam(required = false) String role,
            RedirectAttributes redirectAttributes) {

        // Validate role hợp lệ (chỉ áp dụng khi tạo mới hoặc khi admin chọn role)
        String safeRole = (role == null || role.isBlank()) ? "USER" : role.toUpperCase();
        if (!AVAILABLE_ROLES.contains(safeRole)) {
            redirectAttributes.addFlashAttribute("error", "Role không hợp lệ: " + role);
            return "redirect:/admin/users";
        }

        // ---- TẠO MỚI ----
        if (formUser.getId() == null) {
            if (formUser.getEmail() == null || formUser.getEmail().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Email không được để trống.");
                return "redirect:/admin/users";
            }
            if (userRepository.existsByEmail(formUser.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại.");
                return "redirect:/admin/users";
            }
            if (formUser.getUsername() != null && !formUser.getUsername().isBlank()) {
                if (userRepository.findByUsername(formUser.getUsername()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Username đã tồn tại.");
                    return "redirect:/admin/users";
                }
            }
            if (formUser.getPassword() == null || formUser.getPassword().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu không được để trống.");
                return "redirect:/admin/users";
            }

            // Tài khoản Google → luôn role USER (bỏ qua role admin chọn)
            String provider = "GOOGLE".equals(formUser.getProvider()) ? "GOOGLE" : "LOCAL";
            String finalRole = "GOOGLE".equals(provider) ? "USER" : safeRole;

            userService.createUser(
                    formUser.getEmail(),
                    formUser.getUsername(),
                    formUser.getFullName(),
                    formUser.getPassword(), // raw password, Service sẽ encode
                    provider,
                    finalRole
            );

            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công.");
            return "redirect:/admin/users";
        }

        // ---- CẬP NHẬT ----
        User existing = userRepository.findById(formUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

        if (formUser.getEmail() == null || formUser.getEmail().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Email không được để trống.");
            return "redirect:/admin/users";
        }

        // Kiểm tra email trùng với user khác
        Optional<User> emailOwner = userRepository.findByEmail(formUser.getEmail());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(existing.getId())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại.");
            return "redirect:/admin/users";
        }

        // Kiểm tra username trùng với user khác
        if (formUser.getUsername() != null && !formUser.getUsername().isBlank()) {
            Optional<User> usernameOwner = userRepository.findByUsername(formUser.getUsername());
            if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(existing.getId())) {
                redirectAttributes.addFlashAttribute("error", "Username đã tồn tại.");
                return "redirect:/admin/users";
            }
            existing.setUsername(formUser.getUsername());
        } else {
            existing.setUsername(formUser.getEmail());
        }

        existing.setEmail(formUser.getEmail());
        existing.setFullName(formUser.getFullName());

        // Đổi mật khẩu chỉ khi admin nhập mới
        if (formUser.getPassword() != null && !formUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(formUser.getPassword()));
        }

        // Cập nhật Role — tài khoản Google luôn giữ USER
        if (!"GOOGLE".equals(existing.getProvider())) {
            existing.setRole(safeRole);
        }

        userRepository.save(existing);

        redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công.");
        return "redirect:/admin/users";
    }

    // ==================== TOGGLE ACTIVE ====================
    /**
     * Bật/tắt trạng thái active của User. Chặn tự khóa tài khoản đang đăng nhập.
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId();
        try {
            userService.toggleActive(id, currentUserId);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản.");
        } catch (SelfDeactivationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ==================== XÓA USER ====================
    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId();

        if (currentUserId != null && currentUserId.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Không thể tự xóa tài khoản của mình.");
            return "redirect:/admin/users";
        }

        try {
            // Xóa CartItems → Cart → User (thứ tự tránh FK constraint)
            Optional<Cart> userCart = cartRepository.findByUserId(id);
            if (userCart.isPresent()) {
                Cart cart = userCart.get();
                cartItemRepository.deleteAllByCartId(cart.getId());
                cartRepository.delete(cart);
            }

            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng thành công.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xóa! Người dùng này đã có Đơn hàng hoặc Đánh giá trong hệ thống.");
        }

        return "redirect:/admin/users";
    }

    // ==================== HELPER ====================
    /**
     * Lấy ID của người đang đăng nhập từ SecurityContext.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .map(User::getId)
                .orElse(null);
    }
}
