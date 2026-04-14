package com.example.thexuong.security;

import com.example.thexuong.entity.Role;
import com.example.thexuong.entity.RoleGroup;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.RoleGroupRepository;
import com.example.thexuong.repository.RoleRepository;
import com.example.thexuong.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
// Xử lý sau khi login Google thành công
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleGroupRepository roleGroupRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 1. Lấy thông tin User từ Google
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 2. Đồng bộ User vào Database (nếu chưa có thì tạo mới)
        userRepository.findByEmail(email).orElseGet(() -> {
            // Lấy Role "USER" từ DB (đã seed sẵn trong migration SQL)
            Role userRole = roleRepository.findByName("USER").orElse(null);

            // Lấy RoleGroup mặc định "Khách hàng"
            RoleGroup defaultGroup = roleGroupRepository.findByName("Khách hàng").orElse(null);

            User newUser = User.builder()
                    .email(email)
                    .username(email)    // Dùng email làm username
                    .fullName(name)     // Lưu tên hiển thị từ Google
                    .password("")       // Google user không cần password
                    .provider("GOOGLE")
                    .active(true)
                    .roleGroup(defaultGroup)
                    .build();

            // Gán Role USER vào bảng user_roles
            if (userRole != null) {
                newUser.getRoles().add(userRole);
            }

            return userRepository.save(newUser);
        });

        // 3. Chuyển hướng về trang chủ sau khi login thành công
        setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}