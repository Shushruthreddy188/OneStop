package com.onestop.cart.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/** Reads product details from the Catalog Service (synchronous REST). */
@Component
public class CatalogClient {

    private final RestClient catalogRestClient;

    public CatalogClient(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    public Optional<ProductInfo> findProduct(Long productId) {
        try {
            ProductInfo info = catalogRestClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductInfo.class);
            return Optional.ofNullable(info);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
