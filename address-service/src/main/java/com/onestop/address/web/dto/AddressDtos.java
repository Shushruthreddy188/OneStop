package com.onestop.address.web.dto;

import jakarta.validation.constraints.NotBlank;

public final class AddressDtos {

    private AddressDtos() {
    }

    public record AddressRequest(
            String label,
            String recipientName,
            String phone,
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
            String label,
            String recipientName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country,
            boolean isDefault) {
    }
}
