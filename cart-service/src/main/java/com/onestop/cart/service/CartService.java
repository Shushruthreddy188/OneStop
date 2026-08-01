package com.onestop.cart.service;

import com.onestop.cart.client.CatalogClient;
import com.onestop.cart.client.ProductInfo;
import com.onestop.cart.domain.Cart;
import com.onestop.cart.domain.CartItem;
import com.onestop.cart.error.ApiExceptions.ItemNotFoundException;
import com.onestop.cart.error.ApiExceptions.ProductNotFoundException;
import com.onestop.cart.repo.CartRepository;
import com.onestop.cart.web.dto.CartDtos.AddItemRequest;
import com.onestop.cart.web.dto.CartDtos.CartDto;
import com.onestop.cart.web.dto.CartDtos.CartItemDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartService {

    private static final String ACTIVE = "ACTIVE";

    private final CartRepository carts;
    private final CatalogClient catalogClient;

    public CartService(CartRepository carts, CatalogClient catalogClient) {
        this.carts = carts;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public CartDto getCart(Long customerId) {
        return toDto(getOrCreateCart(customerId));
    }

    @Transactional
    public CartDto addItem(Long customerId, AddItemRequest req) {
        Cart cart = getOrCreateCart(customerId);

        ProductInfo product = catalogClient.findProduct(req.productId())
                .orElseThrow(() -> new ProductNotFoundException(req.productId()));

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(product.id()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.quantity());
            // Refresh the price snapshot to the current selling price.
            existing.setUnitPriceSnapshot(product.sellingPrice());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(product.id());
            item.setQuantity(req.quantity());
            item.setUnitPriceSnapshot(product.sellingPrice());
            item.setProductNameSnapshot(product.name());
            cart.getItems().add(item);
        }
        return toDto(carts.save(cart));
    }

    @Transactional
    public CartDto updateItem(Long customerId, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = findItem(cart, itemId);
        item.setQuantity(quantity);
        return toDto(carts.save(cart));
    }

    @Transactional
    public CartDto removeItem(Long customerId, Long itemId) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = findItem(cart, itemId);
        cart.getItems().remove(item);
        return toDto(carts.save(cart));
    }

    @Transactional
    public CartDto clear(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().clear();
        return toDto(carts.save(cart));
    }

    private Cart getOrCreateCart(Long customerId) {
        carts.createActiveCartIfMissing(customerId);
        return carts.findByCustomerIdAndStatus(customerId, ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Active cart was not created"));
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    private CartDto toDto(Cart cart) {
        var items = cart.getItems().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(i -> new CartItemDto(
                        i.getId(),
                        i.getProductId(),
                        i.getProductNameSnapshot(),
                        i.getUnitPriceSnapshot(),
                        i.getQuantity(),
                        i.lineTotal()))
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemDto::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream().mapToInt(CartItemDto::quantity).sum();

        return new CartDto(cart.getId(), items, subtotal, totalItems);
    }
}
