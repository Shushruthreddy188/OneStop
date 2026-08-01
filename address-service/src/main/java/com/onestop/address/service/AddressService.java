package com.onestop.address.service;

import com.onestop.address.domain.Address;
import com.onestop.address.repo.AddressRepository;
import com.onestop.address.web.dto.AddressDtos.AddressDto;
import com.onestop.address.web.dto.AddressDtos.AddressRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addresses;

    public AddressService(AddressRepository addresses) {
        this.addresses = addresses;
    }

    @Transactional(readOnly = true)
    public List<AddressDto> list(Long customerId) {
        return addresses.findByCustomerIdOrderByIsDefaultDescIdDesc(customerId)
                .stream().map(AddressService::toDto).toList();
    }

    @Transactional
    public AddressDto create(Long customerId, AddressRequest req) {
        if (req.isDefault()) {
            clearDefaults(customerId);
        }
        Address a = new Address();
        a.setCustomerId(customerId);
        apply(a, req);
        return toDto(addresses.save(a));
    }

    @Transactional
    public AddressDto update(Long customerId, Long id, AddressRequest req) {
        Address a = addresses.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
        if (req.isDefault()) {
            clearDefaults(customerId);
        }
        apply(a, req);
        return toDto(a);
    }

    @Transactional
    public void delete(Long customerId, Long id) {
        Address a = addresses.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
        addresses.delete(a);
    }

    private void clearDefaults(Long customerId) {
        addresses.findByCustomerIdOrderByIsDefaultDescIdDesc(customerId)
                .forEach(a -> a.setDefault(false));
    }

    private static void apply(Address a, AddressRequest req) {
        a.setLabel(blankToNull(req.label()));
        a.setRecipientName(blankToNull(req.recipientName()));
        a.setPhone(blankToNull(req.phone()));
        a.setLine1(req.line1().trim());
        a.setLine2(blankToNull(req.line2()));
        a.setCity(req.city().trim());
        a.setState(blankToNull(req.state()));
        a.setPostalCode(blankToNull(req.postalCode()));
        a.setCountry(req.country().trim());
        a.setDefault(req.isDefault());
    }

    private static AddressDto toDto(Address a) {
        return new AddressDto(a.getId(), a.getLabel(), a.getRecipientName(), a.getPhone(),
                a.getLine1(), a.getLine2(), a.getCity(), a.getState(), a.getPostalCode(),
                a.getCountry(), a.isDefault());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
