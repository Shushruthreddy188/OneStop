package com.onestop.wishlist.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/** Reads product details from the Catalog Service to snapshot on add. */
@Component
public class CatalogClient {

    private final RestClient catalogRestClient;

    public CatalogClient(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductInfo(Long id, String name, BigDecimal sellingPrice, BigDecimal mrp) {
    }

    public Optional<ProductInfo> findProduct(Long productId) {
        try {
            return Optional.ofNullable(catalogRestClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductInfo.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Configuration
    static class RestClientConfig {
        @Bean
        RestClient catalogRestClient(
                @Value("${onestop.clients.catalog.base-url:http://localhost:8082}") String baseUrl) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(2));
            factory.setReadTimeout(Duration.ofSeconds(3));
            return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        }
    }
}
