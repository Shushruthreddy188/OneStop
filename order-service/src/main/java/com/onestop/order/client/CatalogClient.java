package com.onestop.order.client;

import com.onestop.order.client.dto.ClientDtos.CatalogProduct;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class CatalogClient {

    private final RestClient catalogRestClient;

    public CatalogClient(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    /** Authoritative product details for the order snapshot (sku, name, price). */
    public Optional<CatalogProduct> findProduct(Long productId) {
        try {
            return Optional.ofNullable(catalogRestClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(CatalogProduct.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
