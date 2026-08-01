package com.onestop.identity.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Request/response payloads for authentication and profile. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            String firstName,
            String lastName,
            String phone) {
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password) {
    }

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UserDto user) {
    }

    public record UserDto(
            Long id,
            String email,
            String firstName,
            String lastName,
            String phone,
            String status,
            List<String> roles) {
    }

    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            String phone) {
    }

    public record AddressRequest(
            @NotBlank String line1,
            String line2,
            @NotBlank String city,
            String state,
            String postalCode,
            @NotBlank String country,
            boolean isDefault) {
    }

    public record AddressDto(
            Long id,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country,
            boolean isDefault) {
    }
}
