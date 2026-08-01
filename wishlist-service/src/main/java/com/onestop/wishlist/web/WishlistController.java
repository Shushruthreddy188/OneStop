package com.onestop.wishlist.web;

import com.onestop.wishlist.service.WishlistService;
import com.onestop.wishlist.web.dto.WishlistDtos.AddItemRequest;
import com.onestop.wishlist.web.dto.WishlistDtos.WishlistDto;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public WishlistDto get(@AuthenticationPrincipal Long customerId) {
        return wishlistService.getWishlist(customerId);
    }

    @PostMapping("/items")
    public WishlistDto add(@AuthenticationPrincipal Long customerId,
                           @Valid @RequestBody AddItemRequest request) {
        return wishlistService.add(customerId, request.productId());
    }

    @DeleteMapping("/items/{productId}")
    public WishlistDto remove(@AuthenticationPrincipal Long customerId,
                              @PathVariable Long productId) {
        return wishlistService.remove(customerId, productId);
    }
}
