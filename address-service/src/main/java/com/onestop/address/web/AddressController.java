package com.onestop.address.web;

import com.onestop.address.service.AddressService;
import com.onestop.address.web.dto.AddressDtos.AddressDto;
import com.onestop.address.web.dto.AddressDtos.AddressRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressDto> list(@AuthenticationPrincipal Long customerId) {
        return addressService.list(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto create(@AuthenticationPrincipal Long customerId,
                             @Valid @RequestBody AddressRequest request) {
        return addressService.create(customerId, request);
    }

    @PutMapping("/{id}")
    public AddressDto update(@AuthenticationPrincipal Long customerId,
                             @PathVariable Long id,
                             @Valid @RequestBody AddressRequest request) {
        return addressService.update(customerId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long customerId, @PathVariable Long id) {
        addressService.delete(customerId, id);
    }
}
