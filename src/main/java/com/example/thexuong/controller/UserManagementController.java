package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.RoleGroup;
import com.example.thexuong.entity.User;
import com.example.thexuong.exception.SelfDeactivationException;
import com.example.thexuong.repository.CartItemRepository;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.repository.RoleRepository;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.RoleGroupService;
import com.example.thexuong.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final RoleGroupService roleGroupService;
    private final RoleRepository roleRepository;

    // ==================== HIỂN THỊ DANH SÁCH ====================

    @GetMapping
    public String showUsers(@RequestParam(required = false) Long editId, Model model) {
        // Dùng findAllByOrderByIdAsc() có @EntityGraph để tránh N+1 khi load roleGroup
        List<User> users = userRepository.findAllByOrderByIdAsc();
        List<RoleGroup> roleGroups = roleGroupService.getAllRoleGroups();

        User formUser = new User();
        boolean isEdit = false;

        if (editId != null) {
            Optional<User> editUser = userRepository.findById(editId);
            if (editUser.isPresent()) {
                formUser = editUser.get();
                isEdit = true;
            }
        }

        Long currentUserId = getCurrentUserId();

        model.addAttribute("users", users);
        model.addAttribute("roleGroups", roleGroups);
        model.addAttribute("allRoles", roleRepository.findAll()); // Cho Checkboxes Roles
        model.addAttribute("formUser", formUser);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("currentUserId", currentUserId);

        return "admin/users";
    }

    // ==================== LƯU USER (TẠO MỚI / CẬP NHẬT) ====================

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("formUser") User formUser,
                           @RequestParam(required = false) Long roleGroupId,
                           @RequestParam(required = false) Set<Long> roleIds,
                           RedirectAttributes redirectAttributes) {

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

            // Tài khoản Google bắt buộc giữ nguyên RoleGroup mặc định, không là quản trị
            String provider = "GOOGLE".equals(formUser.getProvider()) ? "GOOGLE" : "LOCAL";

            userService.createUser(
                    formUser.getEmail(),
                    formUser.getUsername(),
                    formUser.getFullName(),
                    formUser.getPassword(), // raw password, Service sẽ encode
                    provider,
                    roleGroupId
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

        userRepository.save(existing);

        // Gán RoleGroup nếu có chọn (Google user không đổi được RoleGroup)
        if (roleGroupId != null && !"GOOGLE".equals(existing.getProvider())) {
            userService.assignRoleGroup(existing.getId(), roleGroupId);
        }

        // Gán Roles riêng nếu có chọn
        if (roleIds != null) {
            userService.setRoles(existing.getId(), roleIds);
        }

        redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công.");
        return "redirect:/admin/users";
    }

    // ==================== TOGGLE ACTIVE ====================

    /**
     * Bật/tắt trạng thái active của User.
     * Chặn tự khóa tài khoản đang đăng nhập.
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

    /** Lấy ID của người đang đăng nhập từ SecurityContext. */
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