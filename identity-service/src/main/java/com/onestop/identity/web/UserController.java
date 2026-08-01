package com.onestop.identity.web;

import com.onestop.identity.service.UserService;
import com.onestop.identity.web.dto.AuthDtos.AddressDto;
import com.onestop.identity.web.dto.AuthDtos.AddressRequest;
import com.onestop.identity.web.dto.AuthDtos.UpdateProfileRequest;
import com.onestop.identity.web.dto.AuthDtos.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal Long userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/me")
    public UserDto updateMe(@AuthenticationPrincipal Long userId,
                            @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }

    @GetMapping("/me/addresses")
    public List<AddressDto> addresses(@AuthenticationPrincipal Long userId) {
        return userService.listAddresses(userId);
    }

    @PostMapping("/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto addAddress(@AuthenticationPrincipal Long userId,
                                 @Valid @RequestBody AddressRequest request) {
        return userService.addAddress(userId, request);
    }
}
