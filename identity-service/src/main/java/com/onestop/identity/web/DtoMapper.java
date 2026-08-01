package com.onestop.identity.web;

import com.onestop.identity.domain.Address;
import com.onestop.identity.domain.Role;
import com.onestop.identity.domain.User;
import com.onestop.identity.web.dto.AuthDtos.AddressDto;
import com.onestop.identity.web.dto.AuthDtos.UserDto;

import java.util.List;

/** Entity -> DTO conversions. */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserDto toUserDto(User u) {
        List<String> roles = u.getRoles().stream().map(Role::getName).sorted().toList();
        return new UserDto(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getPhone(), u.getStatus(), roles);
    }

    public static AddressDto toAddressDto(Address a) {
        return new AddressDto(a.getId(), a.getLine1(), a.getLine2(), a.getCity(),
                a.getState(), a.getPostalCode(), a.getCountry(), a.isDefault());
    }
}
