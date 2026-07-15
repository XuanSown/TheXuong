package com.example.thexuong.service;

import com.example.thexuong.entity.User;
import com.example.thexuong.exception.SelfDeactivationException;
import com.example.thexuong.exception.UserNotFoundException;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;

// ==================== QUERY ====================

public User getUserByEmail(String email) {
return userRepository.findByEmail(email).orElse(null);
}

/**
* Tim User theo ID — nem UserNotFoundException neu khong tim thay.
*/
public User getUserById(Long id) {
return userRepository.findById(id)
.orElseThrow(() -> new UserNotFoundException(id));
}

// ==================== PROFILE ====================

@Transactional
public void updateProfile(String currentEmail, String fullName, String phoneNumber,
String address, String newPassword) {
log.debug("Updating profile for user {}", currentEmail);
User user = userRepository.findByEmail(currentEmail)
.orElseThrow(() -> new UserNotFoundException(currentEmail));

user.setFullName(fullName);
user.setAddress(address);
user.setPhoneNumber(phoneNumber);

if (newPassword != null && !newPassword.isBlank()) {
log.debug("Changing password for user {}", currentEmail);
user.setPassword(passwordEncoder.encode(newPassword));
}
userRepository.save(user);
log.info("Profile updated for user {}", currentEmail);
}

    /** Cap nhat thong tin user theo userId (dung cho admin). */
    @Transactional
    public void updateProfile(Long userId, String fullName, String phoneNumber, String address, String newPassword) {
        log.debug("Admin updating profile for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (fullName != null) user.setFullName(fullName);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (address != null) user.setAddress(address);
        
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user);
        log.info("Admin updated profile for user {}", userId);
    }

/**
* Change password for LOCAL accounts only
* Throws IllegalArgumentException if:
* - User is OAuth (non-LOCAL provider)
* - Current password is incorrect
* - New password is too short (< 8 chars)
*/
@Transactional
public void changePassword(String email, String currentPassword, String newPassword) {
log.debug("Changing password for user {}", email);
User user = userRepository.findByEmail(email)
.orElseThrow(() -> new UserNotFoundException(email));

// Check if user is OAuth - cannot change password
if (user.getProvider() != null && !"LOCAL".equals(user.getProvider())) {
throw new IllegalArgumentException(
"Tai khoan " + user.getProvider() + " khong the doi mat khau. Vui long su dung tai khoan local."
);
}

// Verify current password (handle null for OAuth users who might have empty password)
if (user.getPassword() == null || user.getPassword().isEmpty()) {
throw new IllegalArgumentException("Tai khoan nay khong co mat khau de xac thuc.");
}

if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
throw new IllegalArgumentException("Mat khau hien tai khong dung.");
}

// Validate new password strength
if (newPassword == null || newPassword.length() < 8) {
throw new IllegalArgumentException("Mat khau moi phai co it nhat 8 ky tu.");
}

// Encode and save new password
user.setPassword(passwordEncoder.encode(newPassword));
userRepository.save(user);
log.info("Password changed successfully for user {}", email);
}


// ==================== ACTIVE STATUS ====================

/**
* Toggle trang thai active cua User (bat ↔ tat).
*
* @param targetUserId ID cua user can toggle
* @param currentUserId ID cua nguoi dang dang nhap (lay tu SecurityContext)
* @throws SelfDeactivationException neu Admin co tat chinh minh
* @throws UserNotFoundException neu khong tim thay target user
*/
@Transactional
public void toggleActive(Long targetUserId, Long currentUserId) {
log.debug("Toggling active status for user {} by user {}", targetUserId, currentUserId);
// Chan tu khoa — so sanh Long bang .equals() de tranh bug auto-unboxing voi gia tri lon
if (targetUserId.equals(currentUserId)) {
throw new SelfDeactivationException();
}

User target = getUserById(targetUserId);
// Dao trang thai: true → false va nguoc lai
target.setActive(!Boolean.TRUE.equals(target.getActive()));
userRepository.save(target);
log.info("User {} active status toggled to {}", targetUserId, target.getActive());
}

// ==================== ROLE (don gian) ====================

/**
* Cap nhat role (String) cho User. Khong dung Set<Role> / Set<RoleGroup> nua.
* Neu {@code role} null/blank → giu nguyen (khong doi).
*/
@Transactional
public void setRole(Long userId, String role) {
if (role == null || role.isBlank()) return;
User user = getUserById(userId);
user.setRole(role.toUpperCase());
userRepository.save(user);
}

// ==================== DELETE ====================

@Transactional
public void deleteUser(Long userId) {
log.info("Deleting user {}", userId);
userRepository.deleteById(userId);
log.debug("User {} deleted successfully", userId);
}

// ==================== ADMIN: CREATE USER ====================

/**
* Tao User moi tu form Admin.
*
* @param email Email (bat buoc, unique)
* @param username Username (co the null → tu dung email)
* @param fullName Ho ten
* @param rawPassword Mat khau tho (se duoc BCrypt encode)
* @param provider "LOCAL" hoac "GOOGLE"
* @param role Role mong muon (CUSTOMER / ADMIN / BOTH). Null/blank → mac dinh "CUSTOMER".
*/
@Transactional
public User createUser(String email, String username, String fullName,
String rawPassword, String provider, String role) {
String finalRole = (role == null || role.isBlank()) ? "CUSTOMER" : role.toUpperCase();
if ("USER".equals(finalRole)) finalRole = "CUSTOMER";
log.debug("Creating user: email={}, username={}, role={}, provider={}", email, username, finalRole, provider);

User.UserBuilder builder = User.builder()
.email(email)
.username(username != null && !username.isBlank() ? username : email)
.fullName(fullName)
.provider(provider != null ? provider : "LOCAL")
.role(finalRole)
.active(true);

// Ma hoa password neu la LOCAL, Google user khong can
if ("LOCAL".equals(provider) && rawPassword != null && !rawPassword.isBlank()) {
builder.password(passwordEncoder.encode(rawPassword));
} else {
builder.password("");
}

User saved = userRepository.save(builder.build());
log.info("User created: id={}, email={}", saved.getId(), email);
return saved;
}
}
