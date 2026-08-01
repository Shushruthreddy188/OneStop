package com.onestop.address.repo;

import com.onestop.address.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerIdOrderByIsDefaultDescIdDesc(Long customerId);

    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);
}
