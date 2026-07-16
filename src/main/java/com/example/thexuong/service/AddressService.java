package com.example.thexuong.service;

import com.example.thexuong.dto.address.AddressRequest;
import com.example.thexuong.dto.address.AddressResponse;
import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserAddress;
import com.example.thexuong.repository.UserAddressRepository;
import com.example.thexuong.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> listByUser(String username) {
        return addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userIdOf(username)).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse create(String username, AddressRequest req) {
        User user = userOf(username);
        UserAddress a = UserAddress.builder()
                .user(user).label(req.getLabel())
                .recipientName(req.getRecipientName()).recipientPhone(req.getRecipientPhone())
                .provinceCode(req.getProvinceCode()).districtCode(req.getDistrictCode()).wardCode(req.getWardCode())
                .streetDetail(req.getStreetDetail()).latitude(req.getLatitude()).longitude(req.getLongitude())
                .isDefault(req.getIsDefault() != null && req.getIsDefault())
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .build();
        a = addressRepository.save(a);
        if (Boolean.TRUE.equals(a.getIsDefault())) clearOtherDefaults(user.getId(), a.getId());
        // ponytail: address đầu tiên auto default để user luôn có 1 default
        if (addressRepository.countByUserId(user.getId()) == 1) { a.setIsDefault(true); addressRepository.save(a); }
        return toResponse(a);
    }

    @Transactional
    public AddressResponse update(String username, Long id, AddressRequest req) {
        UserAddress a = getOwnedOrThrow(username, id);
        a.setLabel(req.getLabel());
        a.setRecipientName(req.getRecipientName()); a.setRecipientPhone(req.getRecipientPhone());
        a.setProvinceCode(req.getProvinceCode()); a.setDistrictCode(req.getDistrictCode()); a.setWardCode(req.getWardCode());
        a.setStreetDetail(req.getStreetDetail()); a.setLatitude(req.getLatitude()); a.setLongitude(req.getLongitude());
        if (req.getIsDefault() != null && req.getIsDefault() && !Boolean.TRUE.equals(a.getIsDefault())) {
            a.setIsDefault(true); clearOtherDefaults(a.getUser().getId(), a.getId());
        }
        a.setUpdatedAt(java.time.LocalDateTime.now());
        return toResponse(addressRepository.save(a));
    }

    @Transactional
    public void delete(String username, Long id) {
        UserAddress a = getOwnedOrThrow(username, id);
        boolean wasDefault = Boolean.TRUE.equals(a.getIsDefault());
        addressRepository.delete(a);
        // ponytail: reassign default cho address còn lại nếu xóa cái default
        if (wasDefault) {
            List<UserAddress> rest = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(a.getUser().getId());
            if (!rest.isEmpty()) { rest.get(0).setIsDefault(true); addressRepository.save(rest.get(0)); }
        }
    }

    @Transactional
    public void setDefault(String username, Long id) {
        UserAddress a = getOwnedOrThrow(username, id);
        clearOtherDefaults(a.getUser().getId(), a.getId());
        a.setIsDefault(true);
        addressRepository.save(a);
    }

    @Transactional(readOnly = true)
    public UserAddress getOwnedOrThrow(String username, Long id) {
        UserAddress a = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy địa chỉ"));
        if (!a.getUser().getEmail().equals(username)) throw new AccessDeniedException("Không có quyền");
        return a;
    }

    private void clearOtherDefaults(Long userId, Long keepId) {
        addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId).forEach(x -> {
            if (!x.getId().equals(keepId) && Boolean.TRUE.equals(x.getIsDefault())) { x.setIsDefault(false); addressRepository.save(x); }
        });
    }
    private Long userIdOf(String username) { return userOf(username).getId(); }
    private User userOf(String username) { return userRepository.findByEmail(username).orElseThrow(() -> new EntityNotFoundException("User không tồn tại")); }

    private AddressResponse toResponse(UserAddress a) {
        return AddressResponse.builder()
                .id(a.getId()).label(a.getLabel())
                .recipientName(a.getRecipientName()).recipientPhone(a.getRecipientPhone())
                .provinceCode(a.getProvinceCode()).districtCode(a.getDistrictCode()).wardCode(a.getWardCode())
                .streetDetail(a.getStreetDetail()).latitude(a.getLatitude()).longitude(a.getLongitude())
                .isDefault(a.getIsDefault()).build();
    }
}
