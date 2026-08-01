package com.onestop.address.service;

import com.onestop.address.domain.Address;
import com.onestop.address.repo.AddressRepository;
import com.onestop.address.web.dto.AddressDtos.AddressRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AddressServiceTest {
    @Test
    void creatingDefaultAddressClearsCustomersPreviousDefault() {
        AddressRepository repo = mock(AddressRepository.class);
        Address previous = new Address(); previous.setDefault(true); previous.setCustomerId(5L);
        when(repo.findByCustomerIdOrderByIsDefaultDescIdDesc(5L)).thenReturn(List.of(previous));
        when(repo.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));
        var request = new AddressRequest("Home", "A", null, " 1 Main ", null,
                " Chicago ", "IL", "60601", " US ", true);

        var result = new AddressService(repo).create(5L, request);

        assertThat(previous.isDefault()).isFalse();
        assertThat(result.isDefault()).isTrue();
        assertThat(result.line1()).isEqualTo("1 Main");
    }
}
