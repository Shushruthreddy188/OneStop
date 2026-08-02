package com.onestop.search.service;

import com.onestop.search.client.CatalogImportClient;
import com.onestop.search.client.CatalogImportClient.CatalogPage;
import com.onestop.search.client.CatalogImportClient.CatalogProduct;
import com.onestop.search.client.RecommendationSignalClient;
import com.onestop.search.domain.SearchProduct;
import com.onestop.search.es.EsSearchService;
import com.onestop.search.es.ProductDoc;
import com.onestop.search.es.ProductIndexService;
import com.onestop.search.repo.SearchProductRepository;
import com.onestop.search.web.dto.SearchDtos.FacetedSearchResponse;
import com.onestop.search.web.dto.SearchDtos.SearchResultDto;
import com.onestop.search.web.dto.SearchDtos.SuggestionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Product search with two interchangeable engines. {@code onestop.search.engine}
 * selects Elasticsearch or Postgres; the reindex populates both, so switching is
 * instant. When Elasticsearch is the chosen engine but is unreachable, queries
 * transparently fall back to Postgres — Elasticsearch is an upgrade, never a
 * single point of failure.
 */
@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private static final int IMPORT_PAGE_SIZE = 500;

    private final SearchProductRepository repo;
    private final CatalogImportClient catalog;
    private final ProductIndexService productIndex;
    private final EsSearchService esSearch;
    private final RecommendationSignalClient signals;
    private final String engine;

    public SearchService(SearchProductRepository repo,
                         CatalogImportClient catalog,
                         ProductIndexService productIndex,
                         EsSearchService esSearch,
                         RecommendationSignalClient signals,
                         @Value("${onestop.search.engine:postgres}") String engine) {
        this.repo = repo;
        this.catalog = catalog;
        this.productIndex = productIndex;
        this.esSearch = esSearch;
        this.signals = signals;
        this.engine = engine;
    }

    private boolean elasticsearchSelected() {
        return "elasticsearch".equalsIgnoreCase(engine);
    }

    /** Rebuild both the Postgres and (when available) the Elasticsearch indexes. */
    @Transactional
    public long reindex() {
        repo.deleteAllInBatch();
        // Aggregated behavioural signals (closing the loop): validated per-product
        // popularity from recommendation-service, baked into the index as a
        // rank_feature. Empty map when unavailable — indexing proceeds regardless.
        Map<Long, Float> popularity = signals.fetchPopularity();
        List<ProductDoc> docs = new ArrayList<>();
        long count = 0;
        boolean last = false;
        int page = 0;
        while (!last) {
            CatalogPage p = catalog.fetchPage(page, IMPORT_PAGE_SIZE);
            if (p == null || p.content() == null || p.content().isEmpty()) {
                break;
            }
            repo.saveAll(p.content().stream().map(SearchService::toEntity).toList());
            p.content().forEach(c -> docs.add(toDoc(c, popularity.get(c.id()))));
            count += p.content().size();
            last = p.last();
            page++;
        }
        log.info("Postgres reindex complete: {} products", count);

        // Mirror into Elasticsearch. Best-effort: an ES outage must not fail the
        // catalog-authoritative Postgres rebuild.
        try {
            if (productIndex.isAvailable()) {
                long esCount = productIndex.reindex(docs);
                log.info("Elasticsearch reindex complete: {} products", esCount);
            } else {
                log.warn("Elasticsearch unavailable; skipped ES reindex (Postgres index is up to date)");
            }
        } catch (Exception e) {
            log.error("Elasticsearch reindex failed; Postgres index remains authoritative", e);
        }
        return count;
    }

    @Transactional(readOnly = true)
    public List<SuggestionDto> suggest(String q, int limit) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        String query = q.trim();
        if (elasticsearchSelected()) {
            try {
                return esSearch.suggest(query, limit);
            } catch (Exception e) {
                log.warn("ES suggest failed, falling back to Postgres: {}", e.getMessage());
            }
        }
        return repo.suggest(query, limit).stream()
                .map(v -> new SuggestionDto(v.getProductId(), v.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public FacetedSearchResponse search(String q, String category, String brand, int page, int size) {
        if (elasticsearchSelected()) {
            try {
                return esSearch.search(q, category, brand, page, size);
            } catch (Exception e) {
                log.warn("ES search failed, falling back to Postgres: {}", e.getMessage());
            }
        }
        return postgresSearch(q, category, brand, page, size);
    }

    private FacetedSearchResponse postgresSearch(String q, String category, String brand, int page, int size) {
        var result = repo.search(nz(q), nz(category), nz(brand), PageRequest.of(page, size));
        List<SearchResultDto> content = result.getContent().stream()
                .map(v -> new SearchResultDto(v.getProductId(), v.getName(), v.getBrandName(),
                        v.getCategoryName(), v.getPackageSize(), v.getSellingPrice(), v.getMrp()))
                .toList();
        // Postgres path does not compute facets; return empty lists (additive fields).
        return new FacetedSearchResponse(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast(),
                List.of(), List.of());
    }

    private static SearchProduct toEntity(CatalogProduct c) {
        SearchProduct sp = new SearchProduct();
        sp.setProductId(c.id());
        sp.setName(c.name());
        sp.setBrandName(c.brandName());
        sp.setCategoryName(c.categoryName());
        sp.setPackageSize(c.packageSize());
        sp.setSellingPrice(c.sellingPrice());
        sp.setMrp(c.mrp());
        return sp;
    }

    private static ProductDoc toDoc(CatalogProduct c, Float popularity) {
        // popularity is a rank_feature (must be > 0); null when the product has
        // no aggregated signal yet, so it is simply omitted from the document.
        return new ProductDoc(c.id(), c.name(), c.brandName(), c.categoryName(),
                c.packageSize(), c.sellingPrice(), c.mrp(), popularity);
    }

    private static String nz(String s) {
        return (s == null) ? "" : s.trim();
    }
}
