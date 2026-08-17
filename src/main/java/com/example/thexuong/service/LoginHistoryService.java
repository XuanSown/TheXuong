package com.example.thexuong.service;

import com.example.thexuong.dto.customercare.PageResponse;
import com.example.thexuong.dto.loginhistory.AdminLoginHistoryResponse;
import com.example.thexuong.entity.LoginHistory;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.LoginHistoryRepository;
import com.example.thexuong.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    /**
     * Ghi 1 lần đăng nhập. KHÔNG BAO GIỜ throw — không được phá luồng login.
     * success=true chỉ ghi khi user là ADMIN/BOTH.
     * success=false ghi mọi email (kể cả email không tồn tại).
     */
    @Transactional
    public void recordLogin(String email, String ip, String userAgent, String provider,
                            boolean success, String failureReason) {
        try {
            String cleanEmail = (email == null ? "" : email.trim());
            User user = userRepository.findByEmail(cleanEmail).orElse(null);

            if (success && (user == null || !isAdminRole(user.getRole()))) {
                return;
            }

            LoginHistory history = LoginHistory.builder()
                    .userId(user != null ? user.getId() : null)
                    .email(cleanEmail)
                    .ipAddress(truncate(ip, 45))
                    .userAgent(truncate(userAgent, 500))
                    .provider(provider)
                    .success(success)
                    .failureReason(truncate(failureReason, 255))
                    .build();
            loginHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Không ghi được login history: {}", e.getMessage());
        }
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "BOTH".equalsIgnoreCase(role);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * Danh sách login history: filter email (like), provider, success, from/to (yyyy-MM-dd).
     * Default sort createdAt DESC, id DESC.
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminLoginHistoryResponse> getHistory(
            String email, String provider, Boolean success, String from, String to,
            int page, int size, String sort) {

        Specification<LoginHistory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + email.trim().toLowerCase() + "%"));
            }
            if (provider != null && !provider.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("provider")),
                        provider.trim().toLowerCase()));
            }
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            LocalDateTime fromDt = parseDateParam(from, true);
            if (fromDt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDt));
            }
            LocalDateTime toDt = parseDateParam(to, false);
            if (toDt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDt));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && sort.toLowerCase().startsWith("createdat,asc")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(direction, "createdAt").and(Sort.by(direction, "id"))
        );

        Page<LoginHistory> historyPage = loginHistoryRepository.findAll(spec, pageable);
        return PageResponse.from(historyPage.map(AdminLoginHistoryResponse::fromEntity));
    }

    /**
     * yyyy-MM-dd → from=đầu ngày, to=cuối ngày. Giá trị không hợp lệ → null (bỏ qua, không crash).
     */
    private LocalDateTime parseDateParam(String value, boolean startOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return startOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }
}
