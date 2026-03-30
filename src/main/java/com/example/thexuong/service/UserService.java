package com.example.thexuong.service;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void updateProfile(String currentEmail, String fullName, String phoneNumber, String address, String newPassword){
        User user = getUserByEmail(currentEmail);

        user.setFullName(fullName);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);
        if(newPassword != null && newPassword.isEmpty()){
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
