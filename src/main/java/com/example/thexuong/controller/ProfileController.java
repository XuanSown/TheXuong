package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    @Autowired
    private final UserService userService;


    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        // Lấy thông tin user hiện tại (username trong Spring Security chính là email)
        User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);

        return "profile";
    }

    // Xử lý cập nhật hồ sơ
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("address") String address,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            userService.updateProfile(principal.getName(), fullName, phoneNumber, address, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}
