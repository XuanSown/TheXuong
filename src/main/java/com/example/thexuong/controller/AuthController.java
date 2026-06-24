package com.example.thexuong.controller;

import com.example.thexuong.entity.User;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    // Inject UserService thay vì UserRepository trực tiếp.
    // UserService.createUser() đã xử lý toàn bộ: hash pass, gán role mặc định "USER", set active.
    // Controller không nên biết về logic nghiệp vụ này.
    private final UserService userService;

    // 1. Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String registerSuccess,
            @RequestParam(required = false) String logout,
            Model model) {

        // FIX BUG #2: Truyền param về template để hiển thị thông báo đúng
        if (error != null)           model.addAttribute("loginError", true);
        if (registerSuccess != null) model.addAttribute("registerSuccess", true);
        if (logout != null)          model.addAttribute("logoutSuccess", true);

        return "login";
    }

    // 2. Hiển thị trang đăng ký
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // 3. Xử lý đăng ký (Form Submit)
    @PostMapping("/register")
    public String register(@ModelAttribute("user") User formUser, Model model) {

        // Validate email không rỗng
        if (formUser.getEmail() == null || formUser.getEmail().isBlank()) {
            model.addAttribute("error", "Email không được để trống!");
            return "register";
        }

        // Validate password không rỗng
        if (formUser.getPassword() == null || formUser.getPassword().isBlank()) {
            model.addAttribute("error", "Mật khẩu không được để trống!");
            return "register";
        }

        try {
            // Dùng UserService.createUser() thay vì gọi thẳng repository.
            // UserService tự:
            //   - Mã hóa password (BCrypt)
            //   - Gán role mặc định "USER" (vì role=null)
            //   - Set active = true
            userService.createUser(
                    formUser.getEmail(),
                    null,                    // username = null → tự dùng email
                    formUser.getFullName(),
                    formUser.getPassword(),  // raw password, Service sẽ encode
                    "LOCAL",
                    null                     // role = null → tự gán "USER"
            );

            return "redirect:/login?registerSuccess";

        } catch (Exception e) {
            // Phân loại lỗi để hiển thị message thân thiện hơn
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("email")) {
                model.addAttribute("error", "Email đã được đăng ký. Vui lòng dùng email khác.");
            } else {
                model.addAttribute("error", "Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.");
            }
            return "register";
        }
    }
}