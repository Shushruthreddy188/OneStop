package com.onestop.identity.service;

import com.onestop.identity.domain.Address;
import com.onestop.identity.domain.User;
import com.onestop.identity.error.ApiExceptions.NotFoundException;
import com.onestop.identity.repo.AddressRepository;
import com.onestop.identity.repo.UserRepository;
import com.onestop.identity.web.DtoMapper;
import com.onestop.identity.web.dto.AuthDtos.AddressDto;
import com.onestop.identity.web.dto.AuthDtos.AddressRequest;
import com.onestop.identity.web.dto.AuthDtos.UpdateProfileRequest;
import com.onestop.identity.web.dto.AuthDtos.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final AddressRepository addresses;

    public UserService(UserRepository users, AddressRepository addresses) {
        this.users = users;
        this.addresses = addresses;
    }

    @Transactional(readOnly = true)
    public UserDto getProfile(Long userId) {
        return DtoMapper.toUserDto(loadUser(userId));
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest req) {
        User user = loadUser(userId);
        if (req.firstName() != null) user.setFirstName(blankToNull(req.firstName()));
        if (req.lastName() != null) user.setLastName(blankToNull(req.lastName()));
        if (req.phone() != null) user.setPhone(blankToNull(req.phone()));
        return DtoMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<AddressDto> listAddresses(Long userId) {
        return addresses.findByUser_IdOrderByIsDefaultDescIdAsc(userId)
                .stream().map(DtoMapper::toAddressDto).toList();
    }

    @Transactional
    public AddressDto addAddress(Long userId, AddressRequest req) {
        User user = loadUser(userId);

        // If this new address is the default, clear the flag on the others.
        if (req.isDefault()) {
            addresses.findByUser_IdOrderByIsDefaultDescIdAsc(userId)
                    .forEach(a -> a.setDefault(false));
        }

        Address address = new Address();
        address.setUser(user);
        address.setLine1(req.line1().trim());
        address.setLine2(blankToNull(req.line2()));
        address.setCity(req.city().trim());
        address.setState(blankToNull(req.state()));
        address.setPostalCode(blankToNull(req.postalCode()));
        address.setCountry(req.country().trim());
        address.setDefault(req.isDefault());

        return DtoMapper.toAddressDto(addresses.save(address));
    }

    private User loadUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
