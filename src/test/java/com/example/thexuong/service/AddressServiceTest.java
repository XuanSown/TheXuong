package com.example.thexuong.service;

import com.example.thexuong.dto.address.AddressRequest;
import com.example.thexuong.dto.address.AddressResponse;
import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserAddress;
import com.example.thexuong.repository.UserAddressRepository;
import com.example.thexuong.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private UserAddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User mockUser;
    private UserAddress mockAddress;
    private AddressRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");

        mockAddress = UserAddress.builder()
                .id(100L)
                .user(mockUser)
                .label("Home")
                .recipientName("John Doe")
                .recipientPhone("0123456789")
                .isDefault(false)
                .createdAt(LocalDateTime.now())
                .build();

        mockRequest = new AddressRequest();
        mockRequest.setLabel("Office");
        mockRequest.setRecipientName("Jane Doe");
        mockRequest.setRecipientPhone("0987654321");
        mockRequest.setIsDefault(false);
    }

    // ==================== listByUser ====================

    @Test
    void listByUser_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> addressService.listByUser("test@gmail.com"));
    }

    @Test
    void listByUser_Success_ReturnsList() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(1L)).thenReturn(List.of(mockAddress));

        List<AddressResponse> res = addressService.listByUser("test@gmail.com");

        assertEquals(1, res.size());
        assertEquals(100L, res.get(0).getId());
        assertEquals("Home", res.get(0).getLabel());
    }

    // ==================== create ====================

    @Test
    void create_Normal_NotFirst_NotDefault() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(addressRepository.save(any(UserAddress.class))).thenAnswer(i -> {
            UserAddress a = i.getArgument(0);
            if (a.getId() == null) a.setId(200L); // simulate save
            return a;
        });
        when(addressRepository.countByUserId(1L)).thenReturn(2L); // not the first address

        AddressResponse res = addressService.create("test@gmail.com", mockRequest);

        assertEquals("Office", res.getLabel());
        assertFalse(res.getIsDefault());
        verify(addressRepository, times(1)).save(any(UserAddress.class)); // Only the initial save
    }

    @Test
    void create_FirstAddress_AutoSetDefault() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(addressRepository.save(any(UserAddress.class))).thenAnswer(i -> {
            UserAddress a = i.getArgument(0);
            if (a.getId() == null) a.setId(200L);
            return a;
        });
        when(addressRepository.countByUserId(1L)).thenReturn(1L); // First address

        AddressResponse res = addressService.create("test@gmail.com", mockRequest);

        assertTrue(res.getIsDefault());
        verify(addressRepository, times(2)).save(any(UserAddress.class)); // 1 for initial save, 1 for auto-default
    }

    @Test
    void create_RequestedDefault_ClearsOthers() {
        mockRequest.setIsDefault(true);
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(addressRepository.save(any(UserAddress.class))).thenAnswer(i -> {
            UserAddress a = i.getArgument(0);
            if (a.getId() == null) a.setId(200L);
            return a;
        });
        
        UserAddress otherDefault = new UserAddress();
        otherDefault.setId(300L);
        otherDefault.setIsDefault(true);
        
        when(addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(1L)).thenReturn(List.of(otherDefault));
        when(addressRepository.countByUserId(1L)).thenReturn(2L);

        addressService.create("test@gmail.com", mockRequest);

        // otherDefault should have its isDefault cleared
        assertFalse(otherDefault.getIsDefault());
        verify(addressRepository, atLeast(2)).save(any(UserAddress.class)); // Initial save + saving cleared default
    }

    // ==================== update ====================

    @Test
    void update_AddressNotFound_ThrowsException() {
        when(addressRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> addressService.update("test@gmail.com", 100L, mockRequest));
    }

    @Test
    void update_AccessDenied_ThrowsException() {
        User otherUser = new User();
        otherUser.setEmail("other@gmail.com");
        mockAddress.setUser(otherUser);

        when(addressRepository.findById(100L)).thenReturn(Optional.of(mockAddress));

        assertThrows(AccessDeniedException.class, () -> addressService.update("test@gmail.com", 100L, mockRequest));
    }

    @Test
    void update_Normal_NoDefaultChange() {
        when(addressRepository.findById(100L)).thenReturn(Optional.of(mockAddress));
        when(addressRepository.save(any(UserAddress.class))).thenReturn(mockAddress);

        AddressResponse res = addressService.update("test@gmail.com", 100L, mockRequest);

        assertEquals("Office", res.getLabel());
        assertEquals("Jane Doe", res.getRecipientName());
        verify(addressRepository, never()).findByUserIdOrderByIsDefaultDescIdAsc(anyLong()); // Should not clear defaults
    }

    @Test
    void update_SetDefault_ClearsOthers() {
        mockRequest.setIsDefault(true); // update to default
        when(addressRepository.findById(100L)).thenReturn(Optional.of(mockAddress)); // previously false
        when(addressRepository.save(any(UserAddress.class))).thenReturn(mockAddress);
        
        UserAddress otherDefault = new UserAddress();
        otherDefault.setId(300L);
        otherDefault.setIsDefault(true);
        
        when(addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(1L)).thenReturn(List.of(otherDefault, mockAddress));

        addressService.update("test@gmail.com", 100L, mockRequest);

        assertTrue(mockAddress.getIsDefault());
        assertFalse(otherDefault.getIsDefault());
        verify(addressRepository, atLeast(2)).save(any(UserAddress.class)); // Save the updated one + save the cleared one
    }

    // ==================== delete ====================

    @Test
    void delete_NormalNotDefault_JustDeletes() {
        when(addressRepository.findById(100L)).thenReturn(Optional.of(mockAddress));
        
        addressService.delete("test@gmail.com", 100L);

        verify(addressRepository, times(1)).delete(mockAddress);
        verify(addressRepository, never()).findByUserIdOrderByIsDefaultDescIdAsc(anyLong());
    }

    @Test
    void delete_WasDefaultNoOthers_JustDeletes() {
        mockAddress.setIsDefault(true);
        when(addressRepository.findById(100L)).thenReturn(Optional.of(mockAddress));
        when(addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(1L)).thenReturn(Collections.emptyList());
        
        addressService.delete("test@gmail.com", 100L);

        verify(addressRepository, times(1)).delete(mockAddress);
        verify(addressRepository, never()).save(any()); // No other to save as default
    }

    @Test
    void delete_WasDefaultHasOthers_ReassignsDefault() {
        mockAddress.setIsDefault(true);
        when(addressRepository.findById(100L)).thenReturn(Optional.of(mockAddress));
        
        UserAddress otherAddress = new UserAddress();
        otherAddress.setId(400L);
        otherAddress.setIsDefault(false);
        
        when(addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(1L)).thenReturn(List.of(otherAddress));
        
        addressService.delete("test@gmail.com", 100L);

        verify(addressRepository, times(1)).delete(mockAddress);
        assertTrue(otherAddress.getIsDefault());
        verify(addressRepository, times(1)).save(otherAddress);
    }

    // ==================== setDefault ====================

    @Test
    void setDefault_Success_ClearsOthers() {
        when(addressRepository.findById(100L)).thenReturn(Optional.of(mockAddress));
        
        UserAddress otherDefault = new UserAddress();
        otherDefault.setId(500L);
        otherDefault.setIsDefault(true);
        
        when(addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(1L)).thenReturn(List.of(otherDefault, mockAddress));
        
        addressService.setDefault("test@gmail.com", 100L);

        assertTrue(mockAddress.getIsDefault());
        assertFalse(otherDefault.getIsDefault());
        
        verify(addressRepository, times(1)).save(otherDefault); // cleared
        verify(addressRepository, times(1)).save(mockAddress); // set
    }
}
