package com.example.thexuong.config;

import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

    // 1. BEAN: Tìm user trong Database
    // spring security sẽ gọi hàm này khi người dùng đăng nhập
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username) //ưu tiên tìm bằng email khi đăng nhập bằng gg
                .or(() -> userRepository.findByUsername(username)) //nếu không thì tìm bằng Username khi đăng nhập bình thường
                .map(u -> new User(
                        u.getEmail(), //dùng email làm định danh chính
                        u.getPassword() == null ? "" : u.getPassword(), //nếu là gg user (pass null) thì trả về rỗng
                        Collections.singleton(new SimpleGrantedAuthority(u.getRole())) // gán quyền user/admin
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email/username này!!!"));
    }

    // 2. BEAN: Cung cấp thuật toán mã hoá mật khẩu
    // dùng BCrypt - chuẩn bảo mật hiện nay
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. BEAN: Liên kết UserDetail với PasswordEncoder
    // giúp spring biết: "Lấy user ở đâu?" và "so sánh pass thế nào?"
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 4.BEAN: Quản lý đăng nhập
    // AuthController sẽ gọi thằng này để thực hiện lệnh .authenticate()
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
