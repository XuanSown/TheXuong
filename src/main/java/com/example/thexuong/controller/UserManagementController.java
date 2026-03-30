package com.example.thexuong.controller;

import com.example.thexuong.entity.Cart;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.CartItemRepository;
import com.example.thexuong.repository.CartRepository;
import com.example.thexuong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    // Inject thêm các Repository để xử lý xóa dữ liệu liên quan
    @Autowired
    private final CartRepository cartRepository;

    @Autowired
    private final CartItemRepository cartItemRepository;

    @GetMapping
    public String showUsers(@RequestParam(required = false) Long editId, Model model) {
        List<User> users = userRepository.findAll(Sort.by("id").ascending());
        User formUser = new User();
        boolean isEdit = false;

        if (editId != null) {
            Optional<User> editUser = userRepository.findById(editId);
            if (editUser.isPresent()) {
                User source = editUser.get();
                formUser.setId(source.getId());
                formUser.setEmail(source.getEmail());
                formUser.setUsername(source.getUsername());
                formUser.setFullName(source.getFullName());
                formUser.setRole(source.getRole());
                formUser.setProvider(source.getProvider());
                isEdit = true;
            }
        }

        String currentUserRole = "USER";
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            User u = userRepository.findById(currentUserId).orElse(null);
            if (u != null) {
                currentUserRole = u.getRole();
            }
        }

        model.addAttribute("users", users);
        model.addAttribute("formUser", formUser);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserRole", currentUserRole);

        return "admin/users";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("formUser") User formUser,
                           @RequestParam(value = "role", defaultValue = "USER") String role,
                           RedirectAttributes redirectAttributes) {

        // ÉP ROLE: Tài khoản đăng nhập bằng Google bắt buộc là USER
        if ("GOOGLE".equals(formUser.getProvider())) {
            role = "USER";
        } else if (!List.of("USER", "ADMIN", "BOTH").contains(role)) {
            role = "USER";
        }

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
            } else {
                formUser.setUsername(formUser.getEmail());
            }
            if (formUser.getPassword() == null || formUser.getPassword().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu không được để trống.");
                return "redirect:/admin/users";
            }

            formUser.setPassword(passwordEncoder.encode(formUser.getPassword()));
            formUser.setRole(role);
            if (formUser.getProvider() == null || formUser.getProvider().isBlank()) {
                formUser.setProvider("LOCAL");
            }
            userRepository.save(formUser);
            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công.");
            return "redirect:/admin/users";
        }

        User existing = userRepository.findById(formUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

        // Chặn sửa Role của tài khoản Google khi Update
        if ("GOOGLE".equals(existing.getProvider())) {
            role = "USER";
        }

        if (formUser.getEmail() == null || formUser.getEmail().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Email không được để trống.");
            return "redirect:/admin/users";
        }

        Optional<User> emailOwner = userRepository.findByEmail(formUser.getEmail());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(existing.getId())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại.");
            return "redirect:/admin/users";
        }

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
        existing.setRole(role);

        if (formUser.getPassword() != null && !formUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(formUser.getPassword()));
        }

        userRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công.");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    @Transactional // Thêm @Transactional để đảm bảo xóa nhiều bảng cùng lúc không bị lỗi
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId();

        if (currentUserId != null && currentUserId.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Không thể tự xóa tài khoản của mình.");
            return "redirect:/admin/users";
        }

        User currentUser = userRepository.findById(currentUserId).orElse(null);
        User targetUser = userRepository.findById(id).orElse(null);

        if (currentUser != null && targetUser != null) {
            // QUY TẮC XÓA: Admin chỉ xóa được USER, BOTH xóa được mọi thứ
            if ("ADMIN".equals(currentUser.getRole()) && !"USER".equals(targetUser.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Quyền ADMIN chỉ được phép xóa tài khoản Khách hàng (USER).");
                return "redirect:/admin/users";
            }
        }

        try {
            // Bước 1: Tìm xem User này có Giỏ hàng (Cart) không
            Optional<Cart> userCart = cartRepository.findByUserId(id);
            if (userCart.isPresent()) {
                Cart cart = userCart.get();
                // Bước 2: Xóa tất cả CartItems thuộc về Cart này
                cartItemRepository.deleteAllByCartId(cart.getId());
                // Bước 3: Xóa Cart
                cartRepository.delete(cart);
            }

            // Bước 4: Cuối cùng mới xóa User
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng thành công.");

        } catch (Exception e) {
            // Nếu User có Đơn hàng (Orders) hoặc Đánh giá (Reviews), code sẽ nhảy vào đây
            redirectAttributes.addFlashAttribute("error", "Không thể xóa! Người dùng này đã có Đơn hàng hoặc Đánh giá trong hệ thống.");
        }

        return "redirect:/admin/users";
    }

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