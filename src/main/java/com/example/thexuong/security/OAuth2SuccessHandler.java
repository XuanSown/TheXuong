package com.example.thexuong.security;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.LoginHistoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
// Xử lý sau khi login Google thành công
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;
    private final LoginHistoryService loginHistoryService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 1. Lấy thông tin User từ Google
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 2. Đồng bộ User vào Database (nếu chưa có thì tạo mới)
        //    Role mặc định = "USER" (không có bảng Role/RoleGroup nữa).
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .username(email)    // Dùng email làm username
                    .fullName(name)     // Lưu tên hiển thị từ Google
                    .password("")       // Google user không cần password
                    .provider("GOOGLE")
                    .role("CUSTOMER")   // Role mặc định (theo CHECK CHK_Users_role)
                    .active(true)
                    .build();

            return userRepository.save(newUser);
        });

        // 3. Build UserDetails từ User entity và issue JWT vào httpOnly cookie
        String role = (user.getRole() == null || user.getRole().isBlank()) ? "CUSTOMER" : user.getRole();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(email)
                .password("")
                .disabled(!Boolean.TRUE.equals(user.getActive()))
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        jwtCookieService.setAuthCookies(response, accessToken, refreshToken);

        loginHistoryService.recordLogin(
                email,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                "GOOGLE", true, null);

        // 4. Redirect to frontend OAuth callback page to complete the flow
        String redirectUrl = frontendUrl + "/oauth/callback";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
