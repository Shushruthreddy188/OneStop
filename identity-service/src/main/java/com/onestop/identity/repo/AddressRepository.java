package com.onestop.identity.repo;

import com.onestop.identity.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser_IdOrderByIsDefaultDescIdAsc(Long userId);
}
