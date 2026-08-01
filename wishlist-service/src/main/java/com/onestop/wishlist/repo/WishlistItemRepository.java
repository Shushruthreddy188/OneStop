package com.onestop.wishlist.repo;

import com.onestop.wishlist.domain.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByCustomerIdOrderByIdDesc(Long customerId);

    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);

    long deleteByCustomerIdAndProductId(Long customerId, Long productId);
}
