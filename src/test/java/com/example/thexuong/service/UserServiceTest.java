package com.example.thexuong.service;

import com.example.thexuong.entity.User;
import com.example.thexuong.exception.SelfDeactivationException;
import com.example.thexuong.exception.UserNotFoundException;
import com.example.thexuong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");
        mockUser.setUsername("testuser");
        mockUser.setProvider("LOCAL");
        mockUser.setPassword("encodedOldPassword");
        mockUser.setActive(true);
    }

    // 1. getUserByEmail
    @Test
    void getUserByEmail_Found() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        User result = userService.getUserByEmail("test@gmail.com");
        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        User result = userService.getUserByEmail("notfound@gmail.com");
        assertNull(result);
    }

    // 2. getUserByEmailWithAddresses
    @Test
    void getUserByEmailWithAddresses_Found() {
        when(userRepository.findWithAddressesByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        User result = userService.getUserByEmailWithAddresses("test@gmail.com");
        assertNotNull(result);
    }

    @Test
    void getUserByEmailWithAddresses_NotFound() {
        when(userRepository.findWithAddressesByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        User result = userService.getUserByEmailWithAddresses("notfound@gmail.com");
        assertNull(result);
    }

    // 3. getUserById
    @Test
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        User result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    // 4. updateProfile (by email)
    @Test
    void updateProfileByEmail_Found_NoPasswordChange() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        userService.updateProfile("test@gmail.com", "New Name", "098", null);

        assertEquals("New Name", mockUser.getFullName());
        assertEquals("098", mockUser.getPhoneNumber());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateProfileByEmail_Found_WithPasswordChange() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        userService.updateProfile("test@gmail.com", "New Name", "098", "newPass");

        assertEquals("encodedNewPass", mockUser.getPassword());
        verify(passwordEncoder, times(1)).encode("newPass");
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateProfileByEmail_NotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> 
            userService.updateProfile("notfound", "Name", "Phone", null)
        );
    }

    // 5. updateProfile (by id - Admin)
    @Test
    void updateProfileById_Found_NoPasswordChange() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        userService.updateProfile(1L, "Admin Update", null, "");

        assertEquals("Admin Update", mockUser.getFullName());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateProfileById_Found_WithPasswordChange() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("adminPass")).thenReturn("encodedAdminPass");

        userService.updateProfile(1L, null, "111", "adminPass");

        assertEquals("111", mockUser.getPhoneNumber());
        assertEquals("encodedAdminPass", mockUser.getPassword());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateProfileById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> 
            userService.updateProfile(99L, "Name", "Phone", null)
        );
    }

    // 6. changePassword
    @Test
    void changePassword_Success() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newValidPass")).thenReturn("encodedNew");

        userService.changePassword("test@gmail.com", "oldPass", "newValidPass");

        assertEquals("encodedNew", mockUser.getPassword());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void changePassword_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> 
            userService.changePassword("notfound", "old", "new")
        );
    }

    @Test
    void changePassword_OAuthUser_ThrowsException() {
        mockUser.setProvider("GOOGLE");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            userService.changePassword("test@gmail.com", "old", "newValidPass")
        );
        assertTrue(ex.getMessage().contains("GOOGLE"));
    }

    @Test
    void changePassword_NoCurrentPassword_ThrowsException() {
        mockUser.setPassword(null); // Explicit null
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            userService.changePassword("test@gmail.com", "old", "newValidPass")
        );
        assertTrue(ex.getMessage().contains("khong co mat khau de xac thuc"));
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongOld", "encodedOldPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            userService.changePassword("test@gmail.com", "wrongOld", "newValidPass")
        );
        assertTrue(ex.getMessage().contains("Mat khau hien tai khong dung"));
    }

    @Test
    void changePassword_ShortNewPassword_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            userService.changePassword("test@gmail.com", "oldPass", "short")
        );
        assertTrue(ex.getMessage().contains("it nhat 8 ky tu"));
    }

    // 7. toggleActive
    @Test
    void toggleActive_Success_Deactivate() {
        // User is currently active=true
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        userService.toggleActive(1L, 2L); // 2L is admin

        assertFalse(mockUser.getActive());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void toggleActive_Success_Activate() {
        mockUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        userService.toggleActive(1L, 2L);

        assertTrue(mockUser.getActive());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void toggleActive_SelfDeactivation_ThrowsException() {
        assertThrows(SelfDeactivationException.class, () -> 
            userService.toggleActive(1L, 1L)
        );
    }

    @Test
    void toggleActive_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> 
            userService.toggleActive(99L, 2L)
        );
    }

    // 8. setRole
    @Test
    void setRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        userService.setRole(1L, "admin");

        assertEquals("ADMIN", mockUser.getRole()); // must be upper cased
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void setRole_NullOrBlank_DoesNothing() {
        userService.setRole(1L, null);
        userService.setRole(1L, "   ");
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setRole_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> 
            userService.setRole(99L, "ADMIN")
        );
    }

    // 9. deleteUser
    @Test
    void deleteUser_Success() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    // 10. createUser
    @Test
    void createUser_LocalProvider_FullInfo() {
        when(passwordEncoder.encode("raw123")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(5L);
            return u;
        });

        User result = userService.createUser("new@gmail.com", "newuser", "New", "raw123", "LOCAL", "ADMIN");

        assertEquals(5L, result.getId());
        assertEquals("new@gmail.com", result.getEmail());
        assertEquals("newuser", result.getUsername());
        assertEquals("LOCAL", result.getProvider());
        assertEquals("ADMIN", result.getRole());
        assertEquals("encoded123", result.getPassword());
        assertTrue(result.getActive());
    }

    @Test
    void createUser_GoogleProvider_NoPassword() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("google@gmail.com", null, "Google User", "ignored", "GOOGLE", null);

        assertEquals("google@gmail.com", result.getEmail());
        assertEquals("google@gmail.com", result.getUsername()); // fallback to email
        assertEquals("GOOGLE", result.getProvider());
        assertEquals("CUSTOMER", result.getRole()); // null -> CUSTOMER
        assertEquals("", result.getPassword()); // Google -> no password
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createUser_RoleUser_ConvertsToCustomer() {
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser("test2@gmail.com", "test2", "Name", "raw", "LOCAL", "USER");

        assertEquals("CUSTOMER", result.getRole()); // "USER" converted to "CUSTOMER"
    }

}
