package com.onestop.order.client;

import com.onestop.order.client.dto.ClientDtos.CartView;
import com.onestop.order.error.ApiExceptions.DependencyException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CartClient {

    private final RestClient cartRestClient;

    public CartClient(RestClient cartRestClient) {
        this.cartRestClient = cartRestClient;
    }

    /** Fetch the caller's cart, forwarding their bearer token. */
    public CartView getCart(String token) {
        try {
            return cartRestClient.get()
                    .uri("/api/cart")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(CartView.class);
        } catch (Exception e) {
            throw new DependencyException("Failed to load cart: " + e.getMessage());
        }
    }

    /** Empty the caller's cart after a successful order (best-effort). */
    public void clearCart(String token) {
        try {
            cartRestClient.delete()
                    .uri("/api/cart")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Non-fatal: the order is already placed.
        }
    }
}
