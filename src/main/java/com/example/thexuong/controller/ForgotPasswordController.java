package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final EmailService emailService;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    // 1. Hiển thị form nhập email
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // 2. Xử lý logic cấp lại mật khẩu
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        // Kiểm tra email có tồn tại không
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Email này không tồn tại trong hệ thống!");
            return "forgot-password";
        }

        User user = userOpt.get();

        // Kiểm tra nếu tài khoản là Google/Facebook thì không cho đổi pass kiểu này
        if (user.getProvider() != null && !user.getProvider().equals("LOCAL")) {
            model.addAttribute("error", "Tài khoản này đăng nhập bằng " + user.getProvider() + ", không thể reset mật khẩu!");
            return "forgot-password";
        }

        try {
            // A. Tạo mật khẩu ngẫu nhiên (lấy 8 ký tự đầu của UUID)
            String newPassword = UUID.randomUUID().toString().substring(0, 8);

            // B. Mã hóa và lưu vào DB
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // C. Gửi email mật khẩu MỚI (chưa mã hóa) cho user
            emailService.sendNewPassword(email, newPassword);

            model.addAttribute("success", "Mật khẩu mới đã được gửi tới email của bạn. Vui lòng kiểm tra hộp thư (cả mục Spam).");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi gửi email: " + e.getMessage());
        }

        return "forgot-password";
    }
}
