package com.onestop.cart.web;

import com.onestop.cart.service.CartService;
import com.onestop.cart.web.dto.CartDtos.AddItemRequest;
import com.onestop.cart.web.dto.CartDtos.CartDto;
import com.onestop.cart.web.dto.CartDtos.UpdateItemRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDto getCart(@AuthenticationPrincipal Long customerId) {
        return cartService.getCart(customerId);
    }

    @PostMapping("/items")
    public CartDto addItem(@AuthenticationPrincipal Long customerId,
                           @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(customerId, request);
    }

    @PatchMapping("/items/{itemId}")
    public CartDto updateItem(@AuthenticationPrincipal Long customerId,
                              @PathVariable Long itemId,
                              @Valid @RequestBody UpdateItemRequest request) {
        return cartService.updateItem(customerId, itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    public CartDto removeItem(@AuthenticationPrincipal Long customerId,
                              @PathVariable Long itemId) {
        return cartService.removeItem(customerId, itemId);
    }

    @DeleteMapping
    public CartDto clearCart(@AuthenticationPrincipal Long customerId) {
        return cartService.clear(customerId);
    }
}
