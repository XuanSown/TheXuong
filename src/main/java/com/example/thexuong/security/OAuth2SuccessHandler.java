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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 2. Đồng bộ User vào Database (nếu chưa có thì tạo mới)
        User dbUser = userRepository.findByEmail(email).orElseGet(() -> {
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

        // 3. Nạp Authorities từ DB (để nhận diện Admin/User)
        dbUser = userRepository.findWithRolesByEmail(email).orElse(dbUser);
        
        Set<GrantedAuthority> authorities = Stream.concat(
                dbUser.getRoles().stream(),
                dbUser.getRoleGroup() != null ? dbUser.getRoleGroup().getRoles().stream() : Stream.empty()
        )
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
        .collect(Collectors.toSet());

        OAuth2User customOAuth2User = new OAuth2User() {
            @Override
            public Map<String, Object> getAttributes() {
                return oAuth2User.getAttributes();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return authorities;
            }

            @Override
            public String getName() {
                return email;
            }
        };

        OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                customOAuth2User, 
                authorities, 
                oauthToken.getAuthorizedClientRegistrationId()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // 4. Chuyển hướng về trang chủ sau khi login thành công
        setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, newAuth);
    }
}