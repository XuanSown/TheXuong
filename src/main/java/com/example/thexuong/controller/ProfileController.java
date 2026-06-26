package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserRepository  userRepository;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return "redirect:/login";

        model.addAttribute("user", currentUser);
        return "profile";
    }

    // Xử lý cập nhật hồ sơ
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User formData,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) return "redirect:/login";

        try {
            currentUser.setFullName(formData.getFullName());
            currentUser.setPhoneNumber(formData.getPhoneNumber());
            currentUser.setAddress(formData.getAddress());

            if (newPassword != null && !newPassword.isBlank()) {

                if (currentUser.getProvider() != null && !currentUser.getProvider().equals("LOCAL")) {
                    redirectAttributes.addFlashAttribute("error", "Tài khoản mạng xã hội không thể đổi mật khẩu!");
                    return "redirect:/profile";
                }

                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                    return "redirect:/profile";
                }

                currentUser.setPassword(passwordEncoder.encode(newPassword));
            }
            userRepository.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
