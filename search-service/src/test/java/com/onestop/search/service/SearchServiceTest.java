package com.onestop.search.service;

import com.onestop.search.client.CatalogImportClient;
import com.onestop.search.client.CatalogImportClient.CatalogPage;
import com.onestop.search.client.CatalogImportClient.CatalogProduct;
import com.onestop.search.client.RecommendationSignalClient;
import com.onestop.search.es.EsSearchService;
import com.onestop.search.es.ProductIndexService;
import com.onestop.search.repo.SearchProductRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SearchServiceTest {
    @Test
    void reindexReplacesIndexAndImportsEveryCatalogPage() {
        SearchProductRepository repo = mock(SearchProductRepository.class);
        CatalogImportClient catalog = mock(CatalogImportClient.class);
        ProductIndexService productIndex = mock(ProductIndexService.class);
        EsSearchService esSearch = mock(EsSearchService.class);
        RecommendationSignalClient signals = mock(RecommendationSignalClient.class);
        // ES treated as unavailable so this stays a focused Postgres-reindex test.
        when(productIndex.isAvailable()).thenReturn(false);
        when(signals.fetchPopularity()).thenReturn(Map.of());
        var product = new CatalogProduct(7L, "Rice Basmati", "OneStop", "Grocery", "1 kg",
                new BigDecimal("120"), new BigDecimal("140"));
        when(catalog.fetchPage(0, 500)).thenReturn(new CatalogPage(List.of(product), 2, false));
        when(catalog.fetchPage(1, 500)).thenReturn(new CatalogPage(List.of(product), 2, true));

        long count = new SearchService(repo, catalog, productIndex, esSearch, signals, "postgres").reindex();

        assertThat(count).isEqualTo(2);
        verify(repo).deleteAllInBatch();
        verify(repo, times(2)).saveAll(anyList());
        verify(catalog).fetchPage(0, 500);
        verify(catalog).fetchPage(1, 500);
        verifyNoMoreInteractions(catalog);
    }
}
