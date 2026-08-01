package com.onestop.wishlist.service;

import com.onestop.wishlist.client.CatalogClient;
import com.onestop.wishlist.client.CatalogClient.ProductInfo;
import com.onestop.wishlist.domain.WishlistItem;
import com.onestop.wishlist.error.ApiExceptions.ProductNotFoundException;
import com.onestop.wishlist.repo.WishlistItemRepository;
import com.onestop.wishlist.web.dto.WishlistDtos.WishlistDto;
import com.onestop.wishlist.web.dto.WishlistDtos.WishlistItemDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository items;
    private final CatalogClient catalogClient;

    public WishlistService(WishlistItemRepository items, CatalogClient catalogClient) {
        this.items = items;
        this.catalogClient = catalogClient;
    }

    @Transactional(readOnly = true)
    public WishlistDto getWishlist(Long customerId) {
        List<WishlistItemDto> dtos = items.findByCustomerIdOrderByIdDesc(customerId)
                .stream().map(WishlistService::toDto).toList();
        return new WishlistDto(dtos, dtos.size());
    }

    /** Add a product (idempotent — adding an already-saved product is a no-op). */
    @Transactional
    public WishlistDto add(Long customerId, Long productId) {
        if (!items.existsByCustomerIdAndProductId(customerId, productId)) {
            ProductInfo product = catalogClient.findProduct(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            WishlistItem item = new WishlistItem();
            item.setCustomerId(customerId);
            item.setProductId(product.id());
            item.setProductName(product.name());
            item.setSellingPrice(product.sellingPrice());
            item.setMrp(product.mrp());
            items.save(item);
        }
        return getWishlist(customerId);
    }

    @Transactional
    public WishlistDto remove(Long customerId, Long productId) {
        items.deleteByCustomerIdAndProductId(customerId, productId);
        return getWishlist(customerId);
    }

    private static WishlistItemDto toDto(WishlistItem i) {
        return new WishlistItemDto(i.getId(), i.getProductId(), i.getProductName(),
                i.getSellingPrice(), i.getMrp());
    }
}
