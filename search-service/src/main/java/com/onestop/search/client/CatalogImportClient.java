package com.onestop.search.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

/** Pulls product pages from the Catalog Service to (re)build the search index. */
@Component
public class CatalogImportClient {

    private final RestClient catalogRestClient;

    public CatalogImportClient(RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CatalogProduct(Long id, String name, String brandName, String categoryName,
                                 String packageSize, BigDecimal sellingPrice, BigDecimal mrp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CatalogPage(List<CatalogProduct> content, int totalPages, boolean last) {
    }

    public CatalogPage fetchPage(int page, int size) {
        return catalogRestClient.get()
                .uri(uri -> uri.path("/api/products").queryParam("page", page).queryParam("size", size).build())
                .retrieve()
                .body(new ParameterizedTypeReference<CatalogPage>() {
                });
    }
}
