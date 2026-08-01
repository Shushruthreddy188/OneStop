package com.onestop.search.service;

import com.onestop.search.client.CatalogImportClient;
import com.onestop.search.client.CatalogImportClient.CatalogPage;
import com.onestop.search.client.CatalogImportClient.CatalogProduct;
import com.onestop.search.domain.SearchProduct;
import com.onestop.search.repo.SearchProductRepository;
import com.onestop.search.web.dto.SearchDtos.PagedResponse;
import com.onestop.search.web.dto.SearchDtos.SearchResultDto;
import com.onestop.search.web.dto.SearchDtos.SuggestionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private static final int IMPORT_PAGE_SIZE = 500;

    private final SearchProductRepository repo;
    private final CatalogImportClient catalog;

    public SearchService(SearchProductRepository repo, CatalogImportClient catalog) {
        this.repo = repo;
        this.catalog = catalog;
    }

    /** Rebuild the whole index from the catalog. */
    @Transactional
    public long reindex() {
        repo.deleteAllInBatch();
        long count = 0;
        boolean last = false;
        int page = 0;
        while (!last) {
            CatalogPage p = catalog.fetchPage(page, IMPORT_PAGE_SIZE);
            if (p == null || p.content() == null || p.content().isEmpty()) {
                break;
            }
            repo.saveAll(p.content().stream().map(SearchService::toEntity).toList());
            count += p.content().size();
            last = p.last();
            page++;
        }
        log.info("Reindex complete: {} products", count);
        return count;
    }

    @Transactional(readOnly = true)
    public List<SuggestionDto> suggest(String q, int limit) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return repo.suggest(q.trim(), limit).stream()
                .map(v -> new SuggestionDto(v.getProductId(), v.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<SearchResultDto> search(String q, String category, String brand, int page, int size) {
        var result = repo.search(nz(q), nz(category), nz(brand), PageRequest.of(page, size));
        List<SearchResultDto> content = result.getContent().stream()
                .map(v -> new SearchResultDto(v.getProductId(), v.getName(), v.getBrandName(),
                        v.getCategoryName(), v.getPackageSize(), v.getSellingPrice(), v.getMrp()))
                .toList();
        return new PagedResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
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

    private static String nz(String s) {
        return (s == null) ? "" : s.trim();
    }
}
